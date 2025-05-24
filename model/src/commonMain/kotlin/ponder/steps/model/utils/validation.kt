package ponder.steps.model.utils

import kabinet.model.SignUpRequest

val SignUpRequest.validSignUp: Boolean
    get() = password.validPassword && username.validUsername && email.validEmail

val String.validUsernameLength: Boolean
    get() = length in 3..20
val String.validUsernameChars: Boolean
    get() = all() { it.isLetterOrDigit() || it == '_' }
val String.validUsername: Boolean
    get() = validUsernameLength && validUsernameChars
val String?.validEmail: Boolean
    get() = this?.let { email ->
        email.contains("@") && email.all { it.isLetterOrDigit() || it == '@' || it == '.' }
    } ?: true // if email is null, it is valid
val String.validPasswordLength: Boolean
    get() = length >= 8

val String.strongPasswordLength: Boolean
    get() = length >= 12
val String.bestPasswordLength: Boolean
    get() = length >= 16
val String.passwordHasLetter: Boolean
    get() = any { it.isLetter() }

val String.passwordHasDigit: Boolean
    get() = any { it.isDigit() }
val String.passwordHasSpecial: Boolean
    get() = any { !it.isLetterOrDigit() }
val String.passwordHasUpper: Boolean
    get() = any { it.isUpperCase() }
val String.passwordHasLower: Boolean
    get() = any { it.isLowerCase() }
val String.validPassword: Boolean
    get() = passwordScore >= 4

val String.passwordScore: Int
    get() {
        var score = 0
        if (passwordHasLetter) score++
        if (passwordHasDigit) score++
        if (passwordHasSpecial) score++
        if (passwordHasUpper) score++
        if (passwordHasLower) score++
        if (validPasswordLength) score++
        if (strongPasswordLength) score++
        if (bestPasswordLength) score++
        return score
    }

val String.passwordStrength: PasswordStrength
    get() {
        return when (passwordScore) {
            0 -> PasswordStrength.NONE
            in 1..3 -> PasswordStrength.WEAKEST
            in 4..5 -> PasswordStrength.WEAK
            6 -> PasswordStrength.MEDIUM
            7 -> PasswordStrength.STRONG
            8 -> PasswordStrength.DIAMOND
            else -> PasswordStrength.NONE
        }
    }

enum class PasswordStrength(val label: String) {
    NONE("None"), WEAKEST("Weakest"), WEAK("Weak"), MEDIUM("Medium"), STRONG("Strong"), DIAMOND("Diamond")
}