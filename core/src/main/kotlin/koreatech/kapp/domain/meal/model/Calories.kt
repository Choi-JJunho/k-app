package koreatech.kapp.domain.meal.model

data class Calories(val value: Int) {
    init {
        require(value >= 0) { "칼로리는 0 이상이어야 합니다." }
        require(value <= 9999) { "칼로리는 9999 이하여야 합니다." }
    }

    operator fun plus(other: Calories): Calories = Calories(value + other.value)
    operator fun minus(other: Calories): Calories = Calories(value - other.value)

    fun isHighCalorie(): Boolean = value >= 800
    fun isLowCalorie(): Boolean = value <= 300

    override fun toString(): String = "${value}kcal"
}
