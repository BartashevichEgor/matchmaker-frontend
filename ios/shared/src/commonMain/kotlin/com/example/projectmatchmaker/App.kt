package com.example.projectmatchmaker

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.projectmatchmaker.auth.AuthRepository
import com.example.projectmatchmaker.auth.TokenStorage
import com.example.projectmatchmaker.auth.ProfileStorage
import com.example.projectmatchmaker.network.CardsApi
import com.example.projectmatchmaker.network.ChatApi
import com.example.projectmatchmaker.network.CatalogItemDto
import com.example.projectmatchmaker.network.CandidateScoreDto
import com.example.projectmatchmaker.network.ProfileReadDto
import com.example.projectmatchmaker.network.ProfileUpdateDto
import com.example.projectmatchmaker.network.ProfileSkillsRequestDto
import com.example.projectmatchmaker.network.ProjectWriteDto
import com.example.projectmatchmaker.network.ProjectSkillDto
import com.example.projectmatchmaker.network.ProjectDto
import com.example.projectmatchmaker.network.SkillLevelDto
import com.example.projectmatchmaker.network.MatchDto
import com.example.projectmatchmaker.network.ApiException
import com.example.projectmatchmaker.network.ChatMessageDto

private const val DEFAULT_USE_BACKEND = true

private enum class AppScreen {
    LOGIN,
    REGISTER,
    VERIFY_EMAIL,
    FORGOT_PASSWORD,
    CREATE_PROFILE,
    ROLE_SELECTION,
    ADD_SKILLS,
    EDUCATION_LEVEL,
    CREATE_PROJECT,
    MANAGE_PROJECT,
    RESET_PASSWORD,
    PROFILE,
    FAVORITES,
    CATALOG,
    MATCHES,
    CHATS,
}

private fun ProfileReadDto.toProfile(roles: List<CatalogItemDto>, skills: List<CatalogItemDto>, email: String) = ProfileData(
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    role = roleId?.let { id -> roles.firstOrNull { it.id == id }?.name }.orEmpty(),
    roleId = roleId,
    about = bio,
    skills = this.skills.mapNotNull { skill -> skills.firstOrNull { it.id == skill.skillId }?.name },
    experience = experience,
    github = githubUrl.orEmpty(),
    backendUserId = userId,
    backendProfileId = id,
)

private fun ProfileData.isReadyForMainApp(): Boolean =
    firstName.trim().length >= 2 &&
        lastName.trim().length >= 2 &&
        role.isNotBlank() &&
        skills.isNotEmpty()

private fun String?.toCatalogWorkFormat(): String? = when (this?.trim()?.lowercase()) {
    "remote", "remotely", "удаленно", "удалённо" -> "Удалённо"
    "hybrid", "гибрид", "гибридный" -> "Гибридный"
    "office", "onsite", "в офисе", "офис" -> "В офисе"
    else -> this?.trim()?.takeIf(String::isNotBlank)
}

private fun CandidateScoreDto.toCatalogPerson(roles: List<CatalogItemDto>, skills: List<CatalogItemDto>) = CatalogPersonItem(
    name = listOf(profile.firstName, profile.lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { profile.username },
    role = profile.roleId?.let { id -> roles.firstOrNull { it.id == id }?.name }.orEmpty().ifBlank { "Специалист" },
    bio = profile.bio,
    skills = profile.skills.mapNotNull { skill -> skills.firstOrNull { it.id == skill.skillId }?.name },
    score = (score * 100).toInt(),
    id = profile.userId,
    workFormat = profile.workFormat.toCatalogWorkFormat(),
)

private fun ProjectDto.toProject(roles: List<CatalogItemDto>, skills: List<CatalogItemDto>) = ProjectData(
    id = id,
    title = title,
    description = description,
    role = roleIds.firstOrNull()?.let { id -> roles.firstOrNull { it.id == id }?.name }.orEmpty(),
    skills = this.skills.mapNotNull { skill -> skills.firstOrNull { it.id == skill.skillId }?.name }.joinToString(", "),
    published = status == "open",
    visible = status != "blocked",
)

private fun ProjectData.toWriteDto(roles: List<CatalogItemDto>, skills: List<CatalogItemDto>) = ProjectWriteDto(
    title = title.trim(),
    description = description.trim(),
    skills = this.skills.split(',').map(String::trim).filter(String::isNotBlank).mapNotNull { name ->
        skills.firstOrNull { it.name.equals(name, ignoreCase = true) }?.let { ProjectSkillDto(it.id, 3) }
    },
    roleIds = roles.firstOrNull { it.name.equals(role.trim(), ignoreCase = true) }?.let { listOf(it.id) }.orEmpty(),
)

/**
 * Навигация приложения. Обычный запуск использует backend, а preview передаёт
 * useBackend=false, чтобы каждый экран можно было просматривать без сервера.
 */
@Composable
/**
 * Локальный режим включён по умолчанию, чтобы UI можно было показывать без
 * запущенного backend. Для интеграционной проверки вызови App(useBackend = true).
 */
fun App(useBackend: Boolean = DEFAULT_USE_BACKEND) {
    var screen by remember { mutableStateOf(AppScreen.LOGIN) }
    var profile by remember { mutableStateOf(ProfileStorage.load() ?: ProfileData()) }
    var project by remember { mutableStateOf(ProjectData()) }
    var favorites by remember { mutableStateOf(emptySet<FavoriteItem>()) }
    var catalogProjects by remember { mutableStateOf(emptyList<CatalogProjectItem>()) }
    var catalogPeople by remember { mutableStateOf(emptyList<CatalogPersonItem>()) }
    var catalogLoading by remember { mutableStateOf(false) }
    var catalogError by remember { mutableStateOf<String?>(null) }
    var catalogReloadKey by remember { mutableStateOf(0) }
    var sessionRevision by remember { mutableStateOf(0) }
    var matchReloadKey by remember { mutableStateOf(0) }
    var matchItems by remember { mutableStateOf<List<MatchDto>?>(null) }
    var demoChatRequest by remember { mutableStateOf(0) }
    var demoChatMessages by remember { mutableStateOf(emptyList<ChatMessageDto>()) }
    var incomingLikeAccepted by remember { mutableStateOf(false) }
    var incomingLikeDismissed by remember { mutableStateOf(false) }
    var roleCatalog by remember { mutableStateOf(emptyList<CatalogItemDto>()) }
    var skillCatalog by remember { mutableStateOf(emptyList<CatalogItemDto>()) }
    var ownedProjectId by remember { mutableStateOf<Int?>(null) }
    var developerCardComplete by remember { mutableStateOf(false) }
    var projectCardComplete by remember { mutableStateOf(false) }
    var onboardingMode by remember { mutableStateOf("developer") }
    var nextSetup by remember { mutableStateOf("profile") }
    var backendError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember(useBackend) { if (useBackend) AuthRepository() else null }
    val cardsApi = remember(useBackend) {
        if (useBackend) CardsApi(accessToken = { TokenStorage.getToken() }) else null
    }
    val chatApi = remember(useBackend) {
        if (useBackend) ChatApi(accessToken = { TokenStorage.getToken() }) else null
    }

    suspend fun refreshOnboarding(api: CardsApi, applyMode: Boolean = false) {
        val state = api.onboarding()
        if (applyMode) {
            onboardingMode = state.mode
            profile = profile.copy(registrationMode = state.mode)
        }
        developerCardComplete = state.profile.complete
        projectCardComplete = state.project.complete
        nextSetup = state.nextSetup
    }

    suspend fun refreshFavorites(api: CardsApi) {
        val people = api.favoriteProfiles().map { item ->
            FavoriteItem(
                type = FavoriteType.PERSON,
                id = item.userId.toString(),
                title = listOf(item.firstName, item.lastName).filter(String::isNotBlank).joinToString(" ").ifBlank { item.username },
                subtitle = item.roleId?.let { id -> roleCatalog.firstOrNull { it.id == id }?.name }.orEmpty(),
                skills = item.skills.mapNotNull { skill -> skillCatalog.firstOrNull { it.id == skill.skillId }?.name },
            )
        }
        val projects = api.favoriteProjects().map { item ->
            FavoriteItem(
                type = FavoriteType.PROJECT,
                id = item.id.toString(),
                title = item.title,
                subtitle = item.roleIds.firstOrNull()?.let { id -> roleCatalog.firstOrNull { it.id == id }?.name }.orEmpty(),
                skills = item.skills.mapNotNull { skill -> skillCatalog.firstOrNull { it.id == skill.skillId }?.name },
            )
        }
        favorites = (people + projects).toSet() + favorites.filter { it.id.startsWith("demo:") }
    }

    fun toggleFavorite(item: FavoriteItem) {
        val saved = item !in favorites
        if (item.id.startsWith("demo:")) {
            favorites = if (saved) favorites + item else favorites - item
            return
        }
        val targetId = item.id.toIntOrNull() ?: return
        coroutineScope.launch {
            runCatching {
                cardsApi?.setFavorite(if (item.type == FavoriteType.PERSON) "profile" else "project", targetId, saved)
            }.onSuccess {
                favorites = if (saved) favorites + item else favorites - item
            }.onFailure { backendError = it.message ?: "Не удалось обновить избранное" }
        }
    }

    DisposableEffect(authRepository, cardsApi, chatApi) {
        onDispose {
            authRepository?.close()
            cardsApi?.close()
            chatApi?.close()
        }
    }

    androidx.compose.runtime.LaunchedEffect(profile) {
        if (profile != ProfileData()) ProfileStorage.save(profile)
    }

    androidx.compose.runtime.LaunchedEffect(useBackend) {
        if (useBackend && cardsApi != null) {
            roleCatalog = runCatching { cardsApi.roles() }.getOrDefault(emptyList())
            skillCatalog = runCatching { cardsApi.skills() }.getOrDefault(emptyList())

            // A still-valid saved session restores the profile as soon as the app opens.
            // Keep locally saved optional fields that are not part of the current API yet.
            if (authRepository?.hasSavedSession() == true) {
                val cachedProfile = profile
                var onboardingResult = runCatching { cardsApi.onboarding() }
                if ((onboardingResult.exceptionOrNull() as? ApiException)?.statusCode == 401) {
                    if (authRepository.refreshSession().isSuccess) {
                        onboardingResult = runCatching { cardsApi.onboarding() }
                    } else {
                        authRepository.logout()
                        backendError = "Сессия истекла. Войдите снова, чтобы открыть сохранённый профиль."
                        screen = AppScreen.LOGIN
                        return@LaunchedEffect
                    }
                }
                val onboarding = onboardingResult.getOrNull()
                onboarding?.let { state ->
                    onboardingMode = state.mode
                    profile = profile.copy(registrationMode = state.mode)
                    developerCardComplete = state.profile.complete
                    projectCardComplete = state.project.complete
                    nextSetup = state.nextSetup
                }
                runCatching { cardsApi.myProfile() }.onSuccess { remote ->
                    val serverProfile = remote.toProfile(roleCatalog, skillCatalog, cachedProfile.email)
                    profile = serverProfile.copy(
                        registrationMode = onboarding?.mode ?: cachedProfile.registrationMode,
                        role = serverProfile.role.ifBlank { cachedProfile.role },
                        roleId = serverProfile.roleId ?: cachedProfile.roleId,
                        photoBase64 = cachedProfile.photoBase64,
                        about = serverProfile.about.ifBlank { cachedProfile.about },
                        skills = serverProfile.skills.ifEmpty { cachedProfile.skills },
                        experience = serverProfile.experience.ifBlank { cachedProfile.experience },
                        education = cachedProfile.education,
                        courses = cachedProfile.courses,
                        github = serverProfile.github.ifBlank { cachedProfile.github },
                    )
                    runCatching { cardsApi.projects(mine = true).firstOrNull() }.onSuccess { owned ->
                        ownedProjectId = owned?.id
                        project = owned?.toProject(roleCatalog, skillCatalog) ?: ProjectData()
                    }
                }.onFailure {
                    // If the API is temporarily offline, retain a complete cached profile.
                    if (profile.isReadyForMainApp()) screen = AppScreen.CATALOG
                }
                runCatching { refreshFavorites(cardsApi) }
                screen = if (onboarding == null) {
                    if (profile.isReadyForMainApp()) AppScreen.CATALOG else AppScreen.CREATE_PROFILE
                } else when (nextSetup) {
                    "profile" -> AppScreen.CREATE_PROFILE
                    "project" -> AppScreen.CREATE_PROJECT
                    else -> AppScreen.CATALOG
                }
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(screen, useBackend, sessionRevision, catalogReloadKey, matchReloadKey) {
        val canUseBackend = useBackend && authRepository?.hasSavedSession() == true
        if (canUseBackend && cardsApi != null) {
            if (screen == AppScreen.CATALOG) {
                catalogLoading = true
                catalogError = null
                backendError = null
                val errors = mutableListOf<Throwable>()
                var ownedId = ownedProjectId
                runCatching { cardsApi.projects(mine = true).firstOrNull() }
                    .onSuccess { owned ->
                        ownedId = owned?.id
                        ownedProjectId = ownedId
                    }
                    .onFailure { errors += it }

                if (developerCardComplete) {
                    runCatching { cardsApi.recommendedProjects() }
                        .onSuccess { projects -> catalogProjects = projects.map { item ->
                            CatalogProjectItem(
                                title = item.project.title,
                                description = item.project.description,
                                role = item.project.roleIds.firstOrNull()?.let { id -> roleCatalog.firstOrNull { it.id == id }?.name } ?: "Ищем участников",
                                skills = item.project.skills.mapNotNull { skill -> skillCatalog.firstOrNull { it.id == skill.skillId }?.name },
                                score = (item.score * 100).toInt(),
                                id = item.project.id,
                                workFormat = item.project.workFormat.toCatalogWorkFormat(),
                            )
                        } }
                        .onFailure { errors += it }
                } else catalogProjects = emptyList()

                if (projectCardComplete && ownedId != null) {
                    runCatching { cardsApi.recommendedCandidates(ownedId) }
                        .onSuccess { candidates -> catalogPeople = candidates.map { it.toCatalogPerson(roleCatalog, skillCatalog) } }
                        .onFailure { errors += it }
                } else if (!projectCardComplete || ownedId == null) catalogPeople = emptyList()

                val unauthorized = errors.any { (it as? ApiException)?.statusCode == 401 }
                if (unauthorized) {
                    val refreshed = authRepository.refreshSession().isSuccess
                    if (refreshed) {
                        catalogLoading = false
                        catalogReloadKey += 1
                        return@LaunchedEffect
                    }
                    authRepository.logout()
                    backendError = "Сессия истекла. Войдите снова, чтобы загрузить каталог."
                    catalogError = backendError
                    catalogLoading = false
                    screen = AppScreen.LOGIN
                    return@LaunchedEffect
                }
                catalogError = errors.mapNotNull { it.message?.takeIf(String::isNotBlank) }.distinct().joinToString("\n").ifBlank { null }
                backendError = catalogError
                catalogLoading = false
            }
            if (screen == AppScreen.PROFILE || screen == AppScreen.CATALOG || screen == AppScreen.MATCHES) {
                runCatching { cardsApi.projects(mine = true).firstOrNull() }.onSuccess { owned ->
                    ownedProjectId = owned?.id
                    project = owned?.toProject(roleCatalog, skillCatalog) ?: ProjectData()
                }
            }
            if (screen == AppScreen.MATCHES) {
                runCatching { cardsApi.matches() }.onSuccess { matchItems = it }.onFailure { backendError = it.message }
            }
            if (screen == AppScreen.FAVORITES) runCatching { refreshFavorites(cardsApi) }
        }
    }

    val basicProfileIsValid =
        profile.firstName.trim().length >= 2 &&
            profile.lastName.trim().length >= 2 &&
            profile.role.isNotBlank()
    val skillsAreValid = profile.skills.isNotEmpty()
    val profileIsReady = basicProfileIsValid && skillsAreValid

    val openCatalog = { screen = AppScreen.CATALOG }
    val openMatches = { screen = AppScreen.MATCHES }
    val openChats = { screen = AppScreen.CHATS }
    val openDemoChat = { demoChatRequest += 1; screen = AppScreen.CHATS }
    val openProfile = {
        screen = if (profileIsReady) AppScreen.PROFILE else AppScreen.CREATE_PROFILE
    }

    MaterialTheme {
        when (screen) {
            AppScreen.LOGIN -> LoginScreen(
                onLogin = { email, password ->
                    val result = authRepository?.login(email, password) ?: Result.success(Unit)
                    result.onSuccess {
                        profile = profile.copy(email = email.trim())
                        if (useBackend && cardsApi != null) {
                            runCatching {
                                roleCatalog = cardsApi.roles()
                                skillCatalog = cardsApi.skills()
                                val state = cardsApi.onboarding()
                                onboardingMode = state.mode
                                developerCardComplete = state.profile.complete
                                projectCardComplete = state.project.complete
                                nextSetup = state.nextSetup
                                profile = profile.copy(registrationMode = state.mode)
                                if (state.profile.exists) {
                                    val remote = cardsApi.myProfile()
                                    profile = remote.toProfile(roleCatalog, skillCatalog, email.trim()).copy(
                                        registrationMode = state.mode,
                                        photoBase64 = profile.photoBase64,
                                        education = profile.education,
                                        courses = profile.courses,
                                    )
                                }
                                val owned = cardsApi.projects(mine = true).firstOrNull()
                                ownedProjectId = owned?.id
                                project = owned?.toProject(roleCatalog, skillCatalog) ?: ProjectData()
                                refreshFavorites(cardsApi)
                            }
                        }
                    }
                    result
                },
                onAuthenticated = {
                    sessionRevision += 1
                    screen = when (nextSetup) {
                        "profile" -> AppScreen.CREATE_PROFILE
                        "project" -> AppScreen.CREATE_PROJECT
                        else -> AppScreen.CATALOG
                    }
                },
                onNavigateToRegister = { screen = AppScreen.REGISTER },
                onForgotPassword = { screen = AppScreen.FORGOT_PASSWORD },
            )

            AppScreen.REGISTER -> RegisterScreen(
                initialMode = profile.registrationMode,
                onModeSelected = { profile = profile.copy(registrationMode = it) },
                onRegister = { username, email, password, mode ->
                    val result = authRepository?.register(username, email, password, mode) ?: Result.success(Unit)
                    result.onSuccess {
                        onboardingMode = mode
                        nextSetup = if (mode == "developer") "profile" else "project"
                        profile = profile.copy(username = username.trim(), email = email.trim(), registrationMode = mode)
                    }
                    result
                },
                onAuthenticated = { screen = AppScreen.VERIFY_EMAIL },
                onNavigateToLogin = { screen = AppScreen.LOGIN },
            )

            AppScreen.VERIFY_EMAIL -> VerifyEmailScreen(
                email = profile.email,
                onVerify = { code ->
                    authRepository?.verifyEmail(profile.email, code) ?: Result.success(Unit)
                },
                onVerified = { screen = AppScreen.LOGIN },
                onResend = {
                    authRepository?.resendVerification(profile.email) ?: Result.success(Unit)
                },
                onBack = { screen = AppScreen.REGISTER },
            )

            AppScreen.FORGOT_PASSWORD -> ForgotPasswordScreen(
                onBack = { screen = AppScreen.LOGIN },
                onSendCode = { email ->
                    val result = authRepository?.forgotPassword(email) ?: Result.success(Unit)
                    result.onSuccess { profile = profile.copy(email = email.trim()); screen = AppScreen.RESET_PASSWORD }
                    result
                },
            )

            AppScreen.RESET_PASSWORD -> ResetPasswordScreen(
                email = profile.email,
                onReset = { code, password ->
                    val result = authRepository?.resetPassword(profile.email, code, password) ?: Result.success(Unit)
                    result
                },
                onBack = { screen = AppScreen.LOGIN },
                onDone = { screen = AppScreen.LOGIN },
            )

            AppScreen.CREATE_PROFILE -> CreateProfileScreen(
                profile = profile,
                onProfileChange = { profile = it },
                canContinue = basicProfileIsValid,
                onNext = { if (basicProfileIsValid) screen = AppScreen.ADD_SKILLS },
                onRoleSelect = { screen = AppScreen.ROLE_SELECTION },
                onCatalog = openCatalog,
                onMatches = openMatches,
                onChats = openChats,
            )

            AppScreen.ROLE_SELECTION -> RoleSelectionScreen(
                selectedRole = profile.role,
                onRoleSelected = { role ->
                    profile = profile.copy(
                        role = role,
                        roleId = roleCatalog.firstOrNull { it.name.equals(role, ignoreCase = true) }?.id,
                    )
                },
                onDone = { screen = AppScreen.CREATE_PROFILE },
                onCatalog = openCatalog,
                onMatches = openMatches,
                onChats = openChats,
                onProfile = openProfile,
            )

            AppScreen.ADD_SKILLS -> SkillsScreen(
                profile = profile,
                onProfileChange = { profile = it },
                canCreate = skillsAreValid,
                onBack = { screen = AppScreen.CREATE_PROFILE },
                onCreate = {
                    if (skillsAreValid) {
                        coroutineScope.launch {
                            val result = runCatching {
                                if (useBackend && authRepository?.hasSavedSession() == true && cardsApi != null) {
                                    val resolvedRoleId = profile.roleId ?: roleCatalog.firstOrNull { it.name.equals(profile.role, true) }?.id
                                    val saved = cardsApi.updateProfile(
                                        ProfileUpdateDto(
                                            firstName = profile.firstName.trim(),
                                            lastName = profile.lastName.trim(),
                                            bio = profile.about,
                                            githubUrl = profile.github.takeIf { it.isNotBlank() },
                                            experience = profile.experience,
                                            interests = "",
                                            roleId = resolvedRoleId,
                                            visibility = "published",
                                        ),
                                    )
                                    val selectedSkills = profile.skills.mapNotNull { name -> skillCatalog.firstOrNull { it.name.equals(name, true) }?.id }
                                    val withSkills = if (selectedSkills.isNotEmpty()) {
                                        cardsApi.updateProfileSkills(selectedSkills.map { SkillLevelDto(it, 3) })
                                    } else saved
                                    profile = withSkills.toProfile(roleCatalog, skillCatalog, profile.email)
                                        .copy(
                                            courses = profile.courses,
                                            photoBase64 = profile.photoBase64,
                                            role = profile.role,
                                            roleId = resolvedRoleId,
                                        )
                                }
                            }
                            result.onSuccess {
                                backendError = null
                                runCatching { cardsApi?.let { refreshOnboarding(it) } }
                                screen = if (onboardingMode == "project" || profile.registrationMode == "project") AppScreen.CREATE_PROJECT else AppScreen.PROFILE
                                sessionRevision += 1
                            }
                                .onFailure { backendError = it.message ?: "Не удалось сохранить профиль" }
                        }
                    }
                },
                onEducationSelect = { screen = AppScreen.EDUCATION_LEVEL },
                onCatalog = openCatalog,
                onMatches = openMatches,
                onChats = openChats,
            )

            AppScreen.EDUCATION_LEVEL -> EducationLevelScreen(
                selected = profile.education,
                onSelect = { profile = profile.copy(education = it) },
                onBack = { screen = AppScreen.ADD_SKILLS },
                onCatalog = openCatalog,
                onMatches = openMatches,
                onChats = openChats,
                onProfile = openProfile,
            )

            AppScreen.PROFILE -> ProfileScreen(
                profile = profile,
                onEdit = { screen = AppScreen.CREATE_PROFILE },
                onFavorites = { screen = AppScreen.FAVORITES },
                onCreateProject = { screen = AppScreen.CREATE_PROJECT },
                onManageProject = { screen = AppScreen.MANAGE_PROJECT },
                hasProject = project.title.isNotBlank() || ownedProjectId != null,
                onModeChange = { mode -> profile = profile.copy(registrationMode = mode) },
                projectPublished = project.published,
                onCatalog = openCatalog,
                onMatches = openMatches,
                onChats = openChats,
            )

            AppScreen.CREATE_PROJECT -> CreateProjectScreen(
                project = project,
                onProjectChange = { project = it },
                onBack = { screen = AppScreen.PROFILE },
                onSave = {
                    coroutineScope.launch {
                        val result = runCatching {
                            if (useBackend && cardsApi != null && authRepository?.hasSavedSession() == true) {
                                val payload = project.toWriteDto(roleCatalog, skillCatalog)
                                val existingProjectId = project.id
                                val saved = if (existingProjectId == null) cardsApi.createProject(payload) else cardsApi.updateProject(existingProjectId, payload)
                                val published = if (!project.published && saved.status != "open") cardsApi.updateProjectStatus(saved.id, "open") else saved
                                project = published.toProject(roleCatalog, skillCatalog)
                                ownedProjectId = published.id
                            } else project = project.copy(published = true)
                        }
                        result.onSuccess {
                            backendError = null
                            runCatching { cardsApi?.let { refreshOnboarding(it) } }
                            screen = AppScreen.PROFILE
                            sessionRevision += 1
                        }
                            .onFailure { backendError = it.message ?: "Не удалось сохранить проект" }
                    }
                },
                onCatalog = openCatalog,
                onMatches = openMatches,
                onChats = openChats,
            )

            AppScreen.MANAGE_PROJECT -> ManageProjectScreen(
                project = project,
                onProjectChange = { project = it },
                onBack = { screen = AppScreen.PROFILE },
                onSave = {
                    coroutineScope.launch {
                        val result = runCatching {
                            val existingProjectId = project.id
                            if (useBackend && cardsApi != null && authRepository?.hasSavedSession() == true && existingProjectId != null) {
                                val saved = cardsApi.updateProject(existingProjectId, project.toWriteDto(roleCatalog, skillCatalog))
                                val wantedStatus = if (project.published) "open" else "closed"
                                val final = if (saved.status != wantedStatus) cardsApi.updateProjectStatus(saved.id, wantedStatus) else saved
                                project = final.toProject(roleCatalog, skillCatalog)
                            }
                        }
                        result.onSuccess { backendError = null; screen = AppScreen.PROFILE; sessionRevision += 1 }
                            .onFailure { backendError = it.message ?: "Не удалось сохранить проект" }
                    }
                },
                onCatalog = openCatalog,
                onMatches = openMatches,
                onChats = openChats,
            )

            AppScreen.FAVORITES -> FavoritesScreen(
                favorites = favorites,
                onFavoriteToggle = ::toggleFavorite,
                onBackToProfile = { screen = AppScreen.PROFILE },
                onCatalog = openCatalog,
                onMatches = openMatches,
                onChats = openChats,
            )

            AppScreen.CATALOG -> CatalogScreen(
                favorites = favorites,
                showProjectsInitially = profile.registrationMode == "developer",
                showProjectsTab = developerCardComplete || !useBackend,
                showPeopleTab = projectCardComplete || !useBackend,
                onFavoriteToggle = ::toggleFavorite,
                onCatalog = openCatalog,
                onProfile = openProfile,
                onMatches = openMatches,
                onChats = openChats,
                projects = catalogProjects,
                people = catalogPeople,
                backendEnabled = useBackend && authRepository?.hasSavedSession() == true,
                isLoading = catalogLoading,
                errorMessage = catalogError,
                onRetry = { catalogReloadKey += 1 },
                onSwipeProject = { projectId, decision ->
                    coroutineScope.launch {
                        runCatching { cardsApi?.swipeProject(projectId, decision) }.onFailure { catalogError = it.message }
                        catalogReloadKey += 1
                    }
                },
                onSwipePerson = { userId, decision ->
                    coroutineScope.launch {
                        val ownerProjectId = ownedProjectId
                        if (ownerProjectId != null) runCatching { cardsApi?.swipeCandidate(ownerProjectId, userId, decision) }
                            .onFailure { catalogError = it.message }
                        catalogReloadKey += 1
                    }
                },
            )

            AppScreen.MATCHES -> MatchesScreen(
                onCatalog = openCatalog,
                onChats = openChats,
                onProfile = openProfile,
                backendMatches = matchItems,
                backendEnabled = useBackend && authRepository?.hasSavedSession() == true,
                incomingLikeAccepted = incomingLikeAccepted,
                incomingLikeDismissed = incomingLikeDismissed,
                onAcceptIncomingLike = { incomingLikeAccepted = true; incomingLikeDismissed = false },
                onDismissIncomingLike = { incomingLikeDismissed = true },
                onOpenIncomingChat = openDemoChat,
                onOpenDemoChat = openDemoChat,
            )
            AppScreen.CHATS -> ChatsScreen(
                onCatalog = openCatalog,
                onMatches = openMatches,
                onProfile = openProfile,
                api = chatApi,
                accessToken = TokenStorage.getToken(),
                currentUserId = profile.backendUserId,
                initialDemoChatRequest = demoChatRequest,
                demoMessages = demoChatMessages,
                onDemoMessagesChange = { demoChatMessages = it },
                onSessionUnauthorized = {
                    coroutineScope.launch {
                        if (authRepository?.refreshSession()?.isSuccess == true) sessionRevision += 1
                        else {
                            authRepository?.logout()
                            backendError = "Сессия истекла. Войдите снова, чтобы открыть чаты."
                            screen = AppScreen.LOGIN
                        }
                    }
                },
            )
        }
    }
}

@Composable
@Preview
private fun AppPreview() {
    MaterialTheme { App(useBackend = false) }
}
