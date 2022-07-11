package com.example.android.uamp.media.model

data class NewsCategory(
    val id: String,
    val title: String,
    val url: String
)
enum class PrefixRoot {
    LOC,
    API, CRW,WAV
}

enum class NewsType {
    VN_EXPRESS,
    TUOI_TRE
}

object VnExpressConstant {
    const val TITLE_DETAIL = ".title-detail"
    const val DESCRIPTION = ".description"
    const val LOCAL_STAMP = ".location-stamp"
    const val FCK_DETAIL = ".fck_detail"
    const val NORMAL = ".Normal"
}

object TuoiTreConstant {
    const val TITLE_DETAIL = ".article-title"
    const val DESCRIPTION = "h2.sapo"
    const val MAIN_DETAIL_BODY = "#main-detail-body > p"
}

object ListNewsCategory {
    val vnExpressCategories = listOf(
        NewsCategory(
            "vne-tin-moi-nhat",
            "Trang chủ",
            "https://vnexpress.net/rss/tin-moi-nhat.rss"
        ),
        NewsCategory(
            "vne-the-gioi",
            "Thế giới",
            "https://vnexpress.net/rss/the-gioi.rss"
        ),
        NewsCategory(
            "vne-thoi-su",
            "Thời sự",
            "https://vnexpress.net/rss/thoi-su.rss"
        ),
        NewsCategory(
            "vne-phap-luat",
            "Pháp luật",
            "https://vnexpress.net/rss/phap-luat.rss"
        )
    )

    val tuoiTreCategories = listOf(
        NewsCategory(
            "tt-tin-moi-nhat",
            "Trang chủ",
            "https://tuoitre.vn/rss/tin-moi-nhat.rss"
        ),
        NewsCategory(
            "tt-the-gioi",
            "Thế giới",
            "https://tuoitre.vn/rss/the-gioi.rss"
        ),
        NewsCategory(
            "tt-thoi-su",
            "Thời sự",
            "https://tuoitre.vn/rss/thoi-su.rss"
        ),
        NewsCategory(
            "tt-the-thao",
            "Thể thao",
            "https://tuoitre.vn/rss/the-thao.rss"
        ),
        NewsCategory(
            "tt-phap-luat",
            "Pháp luật",
            "https://tuoitre.vn/rss/phap-luat.rss"
        )
    )
}