package ponder.contemplate

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform