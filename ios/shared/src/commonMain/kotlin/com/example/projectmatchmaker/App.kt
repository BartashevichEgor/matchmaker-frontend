package com.example.projectmatchmaker

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.projectmatchmaker.auth.AuthRepository
import com.example.projectmatchmaker.auth.TokenStorage
import com.example.projectmatchmaker.network.CardsApi

private const val DEFAULT_USE_BACKEND = false

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
    var profile by remember { mutableStateOf(ProfileData()) }
    var project by remember { mutableStateOf(ProjectData()) }
    var favorites by remember { mutableStateOf(emptySet<FavoriteItem>()) }
    var catalogProjects by remember { mutableStateOf(emptyList<CatalogProjectItem>()) }
    var catalogPeople by remember { mutableStateOf(emptyList<CatalogPersonItem>()) }
    var catalogLoading by remember { mutableStateOf(false) }
    var catalogError by remember { mutableStateOf<String?>(null) }
    var catalogReloadKey by remember { mutableStateOf(0) }
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

    androidx.compose.runtime.LaunchedEffect(screen, useBackend, catalogReloadKey) {
        val canUseBackend = useBackend && authRepository?.hasSavedSession() == true
        if (screen == AppScreen.CATALOG && canUseBackend && cardsApi != null) {
            catalogLoading = true
            catalogError = null
            runCatching { cardsApi.recommendedProjects() }
                .onSuccess { items ->
                    catalogProjects = items.map { item ->
                        CatalogProjectItem(
                            title = item.project.title,
                            description = item.project.description,
                            role = item.project.roleIds.firstOrNull()?.let { "Роль #$it" } ?: "Ищем участников",
                            skills = item.project.skills.map { "#${it.skillId}" },
                            score = (item.score * 100).toInt(),
                        )
                    }
                }
                .onFailure { catalogError = it.message ?: "Не удалось загрузить каталог" }
            catalogLoading = false
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
                    result.onSuccess { profile = profile.copy(email = email.trim()) }
                    result
                },
                onAuthenticated = {
                    screen = if (profileIsReady) AppScreen.CATALOG else AppScreen.CREATE_PROFILE
                },
                onNavigateToRegister = { screen = AppScreen.REGISTER },
                onForgotPassword = { screen = AppScreen.FORGOT_PASSWORD },
            )

            AppScreen.REGISTER -> RegisterScreen(
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
                onCodeSent = { screen = AppScreen.RESET_PASSWORD },
            )

            AppScreen.RESET_PASSWORD -> ResetPasswordScreen(
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
                onRoleSelected = { profile = profile.copy(role = it) },
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
                onCreate = { if (skillsAreValid) screen = AppScreen.PROFILE },
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
                hasProject = project.title.isNotBlank(),
                onCatalog = openCatalog,
                onMatches = openMatches,
                onChats = openChats,
            )

            AppScreen.CREATE_PROJECT -> CreateProjectScreen(
                project = project,
                onProjectChange = { project = it },
                onBack = { screen = AppScreen.PROFILE },
                onSave = { project = project.copy(published = true); screen = AppScreen.PROFILE },
                onCatalog = openCatalog,
                onMatches = openMatches,
                onChats = openChats,
            )

            AppScreen.MANAGE_PROJECT -> ManageProjectScreen(
                project = project,
                onProjectChange = { project = it },
                onBack = { screen = AppScreen.PROFILE },
                onSave = { screen = AppScreen.PROFILE },
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
            )

            AppScreen.MATCHES -> MatchesScreen(openCatalog, openChats, openProfile)
            AppScreen.CHATS -> ChatsScreen(openCatalog, openMatches, openProfile)
        }
    }
}

@Composable
@Preview
private fun AppPreview() {
    MaterialTheme { App(useBackend = false) }
}
