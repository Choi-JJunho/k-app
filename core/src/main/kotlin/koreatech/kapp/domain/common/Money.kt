package koreatech.kapp.domain.common

import java.math.BigDecimal

data class Money(
    val amount: BigDecimal,
    val currency: String = "KRW"
) {
    init {
        require(amount >= BigDecimal.ZERO) { "금액은 0 이상이어야 합니다." }
        require(currency.isNotBlank()) { "통화는 비어있을 수 없습니다." }
    }

    companion object {
        fun zero() = Money(BigDecimal.ZERO)

        fun of(amount: String): Money {
            return Money(BigDecimal(amount))
        }

        fun of(amount: Int): Money {
            return Money(BigDecimal(amount))
        }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "통화가 다릅니다." }
        return Money(amount + other.amount, currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "통화가 다릅니다." }
        return Money(amount - other.amount, currency)
    }

    fun isGreaterThan(other: Money): Boolean {
        require(currency == other.currency) { "통화가 다릅니다." }
        return amount > other.amount
    }

    override fun toString(): String = "${amount}${currency}"
}
