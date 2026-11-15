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
    var incomingLikeAccepted by remember { mutableStateOf(false) }
    var incomingLikeDismissed by remember { mutableStateOf(false) }
    var roleCatalog by remember { mutableStateOf(emptyList<CatalogItemDto>()) }
    var skillCatalog by remember { mutableStateOf(emptyList<CatalogItemDto>()) }
    var ownedProjectId by remember { mutableStateOf<Int?>(null) }
    var backendError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember(useBackend) { if (useBackend) AuthRepository() else null }
    val cardsApi = remember(useBackend) {
        if (useBackend) CardsApi(accessToken = { TokenStorage.getToken() }) else null
    }

    DisposableEffect(authRepository, cardsApi) {
        onDispose {
            authRepository?.close()
            cardsApi?.close()
        }
    }

    androidx.compose.runtime.LaunchedEffect(profile) {
        if (profile != ProfileData()) ProfileStorage.save(profile)
    }

    androidx.compose.runtime.LaunchedEffect(useBackend) {
        if (useBackend && cardsApi != null) {
            runCatching { cardsApi.roles() }.onSuccess { roleCatalog = it }
            runCatching { cardsApi.skills() }.onSuccess { skillCatalog = it }

            // A still-valid saved session restores the profile as soon as the app opens.
            // Keep locally saved optional fields that are not part of the current API yet.
            if (authRepository?.hasSavedSession() == true) {
                val cachedProfile = profile
                runCatching { cardsApi.myProfile() }.onSuccess { remote ->
                    val serverProfile = remote.toProfile(roleCatalog, skillCatalog, cachedProfile.email)
                    profile = serverProfile.copy(
                        registrationMode = cachedProfile.registrationMode,
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
                        if (owned != null) profile = profile.copy(registrationMode = "project")
                    }
                    screen = if (profile.isReadyForMainApp()) AppScreen.CATALOG else AppScreen.CREATE_PROFILE
                }.onFailure {
                    // If the API is temporarily offline, retain a complete cached profile.
                    if (profile.isReadyForMainApp()) screen = AppScreen.CATALOG
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
                runCatching {
                    val owned = cardsApi.projects(mine = true).firstOrNull()
                    ownedProjectId = owned?.id
                    val projects = cardsApi.recommendedProjects()
                    val candidates = owned?.let { cardsApi.recommendedCandidates(it.id) }.orEmpty()
                    projects to candidates
                }.onSuccess { (projects, candidates) ->
                    catalogProjects = projects.map { item ->
                        CatalogProjectItem(
                            title = item.project.title,
                            description = item.project.description,
                            role = item.project.roleIds.firstOrNull()?.let { id -> roleCatalog.firstOrNull { it.id == id }?.name } ?: "Ищем участников",
                            skills = item.project.skills.mapNotNull { skill -> skillCatalog.firstOrNull { it.id == skill.skillId }?.name },
                            score = (item.score * 100).toInt(),
                            id = item.project.id,
                            workFormat = item.project.workFormat.toCatalogWorkFormat(),
                        )
                    }
                    catalogPeople = candidates.map { it.toCatalogPerson(roleCatalog, skillCatalog) }
                }.onFailure {
                    catalogError = it.message ?: "Не удалось загрузить каталог"
                    backendError = catalogError
                }
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
                            // Load the server profile before deciding whether onboarding is needed.
                            runCatching {
                                roleCatalog = cardsApi.roles()
                                skillCatalog = cardsApi.skills()
                                cardsApi.myProfile()
                            }.onSuccess { remote ->
                                profile = remote.toProfile(roleCatalog, skillCatalog, email.trim())
                                val owned = cardsApi.projects(mine = true).firstOrNull()
                                ownedProjectId = owned?.id
                                if (owned != null) profile = profile.copy(registrationMode = "project")
                            }
                        }
                    }
                    result
                },
                onAuthenticated = {
                    sessionRevision += 1
                    screen = when {
                        !profile.isReadyForMainApp() -> AppScreen.CREATE_PROFILE
                        profile.registrationMode == "project" && ownedProjectId == null -> AppScreen.CREATE_PROJECT
                        else -> AppScreen.CATALOG
                    }
                },
                onNavigateToRegister = { screen = AppScreen.REGISTER },
                onForgotPassword = { screen = AppScreen.FORGOT_PASSWORD },
            )

            AppScreen.REGISTER -> RegisterScreen(
                initialMode = profile.registrationMode,
                onModeSelected = { profile = profile.copy(registrationMode = it) },
                onRegister = { username, email, password ->
                    val result = authRepository?.register(username, email, password) ?: Result.success(Unit)
                    result.onSuccess {
                        profile = profile.copy(username = username.trim(), email = email.trim())
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
                                screen = if (profile.registrationMode == "project") AppScreen.CREATE_PROJECT else AppScreen.PROFILE
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
                        result.onSuccess { backendError = null; screen = AppScreen.PROFILE; sessionRevision += 1 }
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
                onFavoriteToggle = { item ->
                    favorites = if (item in favorites) favorites - item else favorites + item
                },
                onBackToProfile = { screen = AppScreen.PROFILE },
                onCatalog = openCatalog,
                onMatches = openMatches,
                onChats = openChats,
            )

            AppScreen.CATALOG -> CatalogScreen(
                favorites = favorites,
                showProjectsInitially = profile.registrationMode == "developer",
                onFavoriteToggle = { item ->
                    favorites = if (item in favorites) favorites - item else favorites + item
                },
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
                incomingLikeAccepted = incomingLikeAccepted,
                incomingLikeDismissed = incomingLikeDismissed,
                onAcceptIncomingLike = { incomingLikeAccepted = true; incomingLikeDismissed = false },
                onDismissIncomingLike = { incomingLikeDismissed = true },
                onOpenIncomingChat = openChats,
            )
            AppScreen.CHATS -> ChatsScreen(
                onCatalog = openCatalog,
                onMatches = openMatches,
                onProfile = openProfile,
                showIncomingMatchChat = incomingLikeAccepted,
            )
        }
    }
}

@Composable
@Preview
private fun AppPreview() {
    MaterialTheme { App(useBackend = false) }
}
