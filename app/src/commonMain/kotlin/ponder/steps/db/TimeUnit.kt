package ponder.steps.db

enum class TimeUnit {
    Minute,
    Hour,
    Day,
    Week,
    Month,
    Year;

    fun toRepeatFormat(value: Int) = when {
        value == 1 -> this.toString().lowercase()
        else -> "$value ${this.toString().lowercase()}s"
    }
}