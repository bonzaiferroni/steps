package ponder.steps

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform