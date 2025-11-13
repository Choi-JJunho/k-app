package koreatech.kapp.global

/**
 * 페이지네이션 요청 파라미터
 */
data class PageRequest(
    val page: Int = 0,
    val size: Int = 20,
    val sort: String? = null,
    val direction: SortDirection = SortDirection.DESC
) {
    init {
        require(page >= 0) { "페이지 번호는 0 이상이어야 합니다." }
        require(size in 1..100) { "페이지 크기는 1에서 100 사이여야 합니다." }
    }

    fun offset(): Int = page * size
}

enum class SortDirection {
    ASC, DESC
}

/**
 * 페이지네이션 응답
 */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
) {
    companion object {
        fun <T> of(
            content: List<T>,
            pageRequest: PageRequest,
            totalElements: Long
        ): PageResponse<T> {
            val totalPages = if (totalElements == 0L) 1 else ((totalElements - 1) / pageRequest.size + 1).toInt()
            return PageResponse(
                content = content,
                page = pageRequest.page,
                size = pageRequest.size,
                totalElements = totalElements,
                totalPages = totalPages,
                first = pageRequest.page == 0,
                last = pageRequest.page >= totalPages - 1
            )
        }
    }
}
