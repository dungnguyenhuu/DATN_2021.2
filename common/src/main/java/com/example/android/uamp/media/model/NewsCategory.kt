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

object VnExpressConstant {
    const val TITLE_DETAIL = ".title-detail"
    const val DESCRIPTION = ".description"
    const val LOCAL_STAMP = ".location-stamp"
    const val FCK_DETAIL = ".fck_detail"
    const val NORMAL = ".Normal"
    const val TYPE = "vne"
    const val TYPE_NEWS = "VnExpress"
}

object TuoiTreConstant {
    const val TITLE_DETAIL = ".article-title"
    const val DESCRIPTION = "h2.sapo"
    const val MAIN_DETAIL_BODY = "#main-detail-body > p"
    const val TYPE = "tt"
    const val TYPE_NEWS = "Tuoi tre"
}

object ListNewsCategory {
    val vnExpressCategories = listOf(
        NewsCategory(
            "vne__tin-moi-nhat",
            "Trang chủ",
            "https://vnexpress.net/rss/tin-moi-nhat.rss"
        ),
        NewsCategory(
            "vne__the-gioi",
            "Thế giới",
            "https://vnexpress.net/rss/the-gioi.rss"
        ),
        NewsCategory(
            "vne__thoi-su",
            "Thời sự",
            "https://vnexpress.net/rss/thoi-su.rss"
        ),
        NewsCategory(
            "vne__phap-luat",
            "Pháp luật",
            "https://vnexpress.net/rss/phap-luat.rss"
        )
    )

    val tuoiTreCategories = listOf(
        NewsCategory(
            "tt__tin-moi-nhat",
            "Trang chủ",
            "https://tuoitre.vn/rss/tin-moi-nhat.rss"
        ),
        NewsCategory(
            "tt__the-gioi",
            "Thế giới",
            "https://tuoitre.vn/rss/the-gioi.rss"
        ),
        NewsCategory(
            "tt__thoi-su",
            "Thời sự",
            "https://tuoitre.vn/rss/thoi-su.rss"
        ),
        NewsCategory(
            "tt__the-thao",
            "Thể thao",
            "https://tuoitre.vn/rss/the-thao.rss"
        ),
        NewsCategory(
            "tt__phap-luat",
            "Pháp luật",
            "https://tuoitre.vn/rss/phap-luat.rss"
        )
    )
}