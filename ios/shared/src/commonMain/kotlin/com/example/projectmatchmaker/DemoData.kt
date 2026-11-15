package com.example.projectmatchmaker

/** Данные для демонстрации экранов без подключения к серверу. */
data class DemoMatch(
    val id: String,
    val name: String,
    val role: String,
    val initials: String,
    val skills: List<String>,
    val description: String,
    val matchPercent: Int,
)

data class DemoMessage(
    val author: String,
    val text: String,
    val time: String,
    val isMine: Boolean = false,
)

data class DemoChat(
    val id: String,
    val name: String,
    val initials: String,
    val lastMessage: String,
    val messages: List<DemoMessage>,
)

val demoMatches = listOf(
    DemoMatch(
        id = "alexey",
        name = "Алексей Ковалёв",
        role = "Backend-разработчик",
        initials = "АК",
        skills = listOf("Python", "Django", "SQL"),
        description = "Люблю чистый код. Ищу команду для полезного проекта.",
        matchPercent = 87,
    ),
    DemoMatch(
        id = "teamflow",
        name = "TeamFlow",
        role = "Команда для учебного проекта",
        initials = "TF",
        skills = listOf("Figma", "UX", "UI"),
        description = "Создаём планировщик задач. Нужна помощь с интерфейсом и прототипом.",
        matchPercent = 91,
    ),
)

val demoIncomingMatch = DemoMatch(
    id = "codegarden",
    name = "CodeGarden",
    role = "Команда ищет UI/UX-дизайнера",
    initials = "CG",
    skills = listOf("Figma", "UX", "Kotlin"),
    description = "Команда посмотрела вашу анкету и хочет пригласить вас в проект.",
    matchPercent = 93,
)

val demoChats = listOf(
    DemoChat(
        id = "alexey",
        name = "Алексей Ковалёв",
        initials = "АК",
        lastMessage = "Давай обсудим идею проекта?",
        messages = listOf(
            DemoMessage("Алексей", "Привет! Кажется, у нас хорошее совпадение.", "18:42"),
            DemoMessage("Вы", "Привет! Да, я тоже так думаю.", "18:44", isMine = true),
            DemoMessage("Алексей", "Давай обсудим идею проекта?", "18:45"),
        ),
    ),
    DemoChat(
        id = "teamflow",
        name = "TeamFlow",
        initials = "TF",
        lastMessage = "Мы отправили тебе описание проекта.",
        messages = listOf(
            DemoMessage("TeamFlow", "Привет! Рады познакомиться.", "17:20"),
            DemoMessage("TeamFlow", "Мы отправили тебе описание проекта.", "17:22"),
        ),
    ),
)

val demoIncomingChat = DemoChat(
    id = "codegarden",
    name = "CodeGarden",
    initials = "CG",
    lastMessage = "Привет! Расскажем подробнее о проекте?",
    messages = listOf(
        DemoMessage("CodeGarden", "Привет! Нам понравилась твоя анкета. Хотим пригласить тебя в команду.", "сейчас"),
        DemoMessage("Вы", "Привет! Расскажите, пожалуйста, подробнее о проекте.", "сейчас", isMine = true),
    ),
)
