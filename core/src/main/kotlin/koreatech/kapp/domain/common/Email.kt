package koreatech.kapp.domain.common

data class Email(val value: String) {
    init {
        require(isValidEmail(value)) { "유효하지 않은 이메일 형식입니다: $value" }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return email.matches(emailRegex)
    }

    override fun toString(): String = value
}
