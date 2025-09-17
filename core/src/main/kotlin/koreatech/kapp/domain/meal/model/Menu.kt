package koreatech.kapp.domain.meal.model

data class Menu(val items: List<String>) {
    init {
        require(items.isNotEmpty()) { "메뉴는 최소 1개 이상이어야 합니다." }
        require(items.all { it.isNotBlank() }) { "메뉴 항목은 비어있을 수 없습니다." }
    }

    fun size(): Int = items.size

    fun contains(item: String): Boolean = items.contains(item)

    fun hasVegetarianOptions(): Boolean {
        val vegetarianKeywords = listOf("나물", "샐러드", "두부", "콩", "버섯")
        return items.any { menuItem ->
            vegetarianKeywords.any { keyword -> menuItem.contains(keyword) }
        }
    }

    fun hasSpicyItems(): Boolean {
        val spicyKeywords = listOf("매운", "김치", "고추", "매콤", "불고기")
        return items.any { menuItem ->
            spicyKeywords.any { keyword -> menuItem.contains(keyword) }
        }
    }

    override fun toString(): String = items.joinToString(", ")
}
