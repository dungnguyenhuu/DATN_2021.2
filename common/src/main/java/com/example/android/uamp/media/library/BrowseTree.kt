/*
 * Copyright 2019 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.uamp.media.library

import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaMetadataCompat
import com.example.android.uamp.media.MusicService
import com.example.android.uamp.media.R
import com.example.android.uamp.media.extensions.album
import com.example.android.uamp.media.extensions.albumArt
import com.example.android.uamp.media.extensions.albumArtUri
import com.example.android.uamp.media.extensions.artist
import com.example.android.uamp.media.extensions.flag
import com.example.android.uamp.media.extensions.id
import com.example.android.uamp.media.extensions.mediaUri
import com.example.android.uamp.media.extensions.title
import com.example.android.uamp.media.extensions.trackNumber
import com.example.android.uamp.media.extensions.urlEncoded
import com.example.android.uamp.media.model.ListNewsCategory
import com.example.android.uamp.media.model.PrefixRoot

/**
 * Represents a tree of media that's used by [MusicService.onLoadChildren].
 *
 * [BrowseTree] maps a media id (see: [MediaMetadataCompat.METADATA_KEY_MEDIA_ID]) to one (or
 * more) [MediaMetadataCompat] objects, which are children of that media id.
 *
 * For example, given the following conceptual tree:
 * root
 *  +-- Albums
 *  |    +-- Album_A
 *  |    |    +-- Song_1
 *  |    |    +-- Song_2
 *  ...
 *  +-- Artists
 *  ...
 *
 *  Requesting `browseTree["root"]` would return a list that included "Albums", "Artists", and
 *  any other direct children. Taking the media ID of "Albums" ("Albums" in this example),
 *  `browseTree["Albums"]` would return a single item list "Album_A", and, finally,
 *  `browseTree["Album_A"]` would return "Song_1" and "Song_2". Since those are leaf nodes,
 *  requesting `browseTree["Song_1"]` would return null (there aren't any children of it).
 */
class BrowseTree(
    val context: Context,
    musicSource: MusicSource,
    val recentMediaId: String? = null
) {
    private val mediaIdToChildren = mutableMapOf<String, MutableList<MediaMetadataCompat>>()

    /**
     * Whether to allow clients which are unknown (not on the allowed list) to use search on this
     * [BrowseTree].
     */
    val searchableByUnknownCaller = true

    /**
     * In this example, there's a single root node (identified by the constant
     * [UAMP_BROWSABLE_ROOT]). The root's children are each album included in the
     * [MusicSource], and the children of each album are the songs on that album.
     * (See [BrowseTree.buildAlbumRoot] for more details.)
     *
     * TODO: Expand to allow more browsing types.
     */
    init {
        val rootList = mediaIdToChildren[UAMP_BROWSABLE_ROOT] ?: mutableListOf()

        val recommendedMetadata = MediaMetadataCompat.Builder().apply {
            id = generalMediaItemId(PrefixRoot.LOC, VN_EXPRESS_ROOT)
            title = context.getString(R.string.recommended_title)
            albumArtUri = RESOURCE_ROOT_URI +
                    context.resources.getResourceEntryName(R.drawable.ic_vn_express)
            flag = MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        }.build()

        val albumsMetadata = MediaMetadataCompat.Builder().apply {
            id = generalMediaItemId(PrefixRoot.LOC, TUOI_TRE_ROOT)
            title = context.getString(R.string.albums_title)
            albumArtUri = RESOURCE_ROOT_URI +
                    context.resources.getResourceEntryName(R.drawable.ic_album)
            flag = MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        }.build()

        rootList += recommendedMetadata
        rootList += albumsMetadata
        mediaIdToChildren[UAMP_BROWSABLE_ROOT] = rootList

        setVnExpressChildren()
        setTTChildren()
//        setBrowseTreeMusicSource(musicSource)
    }

    private fun setVnExpressChildren() {
        val vnExpressCategories = mutableListOf<MediaMetadataCompat>()
        ListNewsCategory.vnExpressCategories.forEach { category ->
            val itemId = generalMediaItemId(PrefixRoot.API, category.id)
            val item = MediaMetadataCompat.Builder().apply {
                id = itemId
                title = category.title
                albumArtUri = RESOURCE_ROOT_URI +
                        context.resources.getResourceEntryName(R.drawable.ic_album)
                mediaUri = category.url
                flag = MediaItem.FLAG_BROWSABLE
            }.build()
            vnExpressCategories += item
            mediaIdToChildren[itemId] = mutableListOf(item)
        }

        mediaIdToChildren[generalMediaItemId(PrefixRoot.LOC, VN_EXPRESS_ROOT)] = vnExpressCategories
    }

    private fun setTTChildren() {
        val vnExpressCategories = mutableListOf<MediaMetadataCompat>()
        ListNewsCategory.tuoiTreCategories.forEach { category ->
            val itemId = generalMediaItemId(PrefixRoot.API, category.id)
            val item = MediaMetadataCompat.Builder().apply {
                id = itemId
                title = category.title
                albumArtUri = RESOURCE_ROOT_URI +
                        context.resources.getResourceEntryName(R.drawable.ic_album)
                mediaUri = category.url
                flag = MediaItem.FLAG_BROWSABLE
            }.build()
            vnExpressCategories += item
            mediaIdToChildren[itemId] = mutableListOf(item)
        }
        mediaIdToChildren[generalMediaItemId(PrefixRoot.LOC, TUOI_TRE_ROOT)] = vnExpressCategories
    }

    private fun generalMediaItemId(prefix: PrefixRoot, s: String): String {
        return "${prefix.name}__$s"
    }

    private fun setBrowseTreeMusicSource(musicSource: MusicSource) {
        musicSource.forEach { mediaItem ->
            val albumMediaId = mediaItem.album.urlEncoded
            val albumChildren = mediaIdToChildren[albumMediaId] ?: buildAlbumRoot(mediaItem)
            albumChildren += mediaItem

            // Add the first track of each album to the 'Recommended' category
            if (mediaItem.trackNumber == 1L) {
                val recommendedChildren = mediaIdToChildren[VN_EXPRESS_ROOT]
                    ?: mutableListOf()
                recommendedChildren += mediaItem
                mediaIdToChildren[VN_EXPRESS_ROOT] = recommendedChildren
            }

            // If this was recently played, add it to the recent root.
            if (mediaItem.id == recentMediaId) {
                mediaIdToChildren[UAMP_RECENT_ROOT] = mutableListOf(mediaItem)
            }
        }
    }

    /**
     * Provide access to the list of children with the `get` operator.
     * i.e.: `browseTree\[UAMP_BROWSABLE_ROOT\]`
     */
    operator fun get(mediaId: String) = mediaIdToChildren[mediaId]

    /**
     * Builds a node, under the root, that represents an album, given
     * a [MediaMetadataCompat] object that's one of the songs on that album,
     * marking the item as [MediaItem.FLAG_BROWSABLE], since it will have child
     * node(s) AKA at least 1 song.
     */
    private fun buildAlbumRoot(mediaItem: MediaMetadataCompat): MutableList<MediaMetadataCompat> {
        val albumMetadata = MediaMetadataCompat.Builder().apply {
            id = mediaItem.album.urlEncoded
            title = mediaItem.album
            artist = mediaItem.artist
            albumArt = mediaItem.albumArt
            albumArtUri = mediaItem.albumArtUri.toString()
            flag = MediaItem.FLAG_BROWSABLE
        }.build()

        // Adds this album to the 'Albums' category.
        val rootList = mediaIdToChildren[TUOI_TRE_ROOT] ?: mutableListOf()
        rootList += albumMetadata
        mediaIdToChildren[TUOI_TRE_ROOT] = rootList

        // Insert the album's root with an empty list for its children, and return the list.
        return mutableListOf<MediaMetadataCompat>().also {
            mediaIdToChildren[albumMetadata.id!!] = it
        }
    }
}

const val UAMP_BROWSABLE_ROOT = "LOC__/"
const val UAMP_EMPTY_ROOT = "@empty@"
const val VN_EXPRESS_ROOT = "__VNEXPRESS__"
const val TUOI_TRE_ROOT = "__TUOI_TRE__"
const val UAMP_RECENT_ROOT = "__RECENT__"

const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"

const val RESOURCE_ROOT_URI = "android.resource://vn.app.news/drawable/"
