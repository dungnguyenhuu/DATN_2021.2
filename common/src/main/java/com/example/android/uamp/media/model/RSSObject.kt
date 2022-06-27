package com.example.android.uamp.media.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class Feed(
    var url: String,
    var title: String,
    var link: String,
    var author: String,
    var description: String,
    var image: String
)

@Parcelize
data class ItemNews(
    var title: String,
    var pubDate: String,
    var link: String,
    var guid: String,
    var author: String,
    var thumbnail: String,
    var description: String,
    var content: String,
    var enclosure: Enclosure,
    var categories: List<String>
): Parcelable

@Parcelize
class Enclosure: Parcelable {}

data class RSSObject(
    var status: String,
    var feed: Feed,
    var items: List<ItemNews>
)