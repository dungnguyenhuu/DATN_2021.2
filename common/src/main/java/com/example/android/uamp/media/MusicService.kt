/*
 * Copyright 2017 Google Inc. All rights reserved.
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

package com.example.android.uamp.media

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.MediaBrowserServiceCompat.BrowserRoot.EXTRA_RECENT
import com.example.android.uamp.common.Utils
import com.example.android.uamp.media.extensions.album
import com.example.android.uamp.media.extensions.albumArtUri
import com.example.android.uamp.media.extensions.artist
import com.example.android.uamp.media.extensions.date
import com.example.android.uamp.media.extensions.flag
import com.example.android.uamp.media.extensions.id
import com.example.android.uamp.media.extensions.mediaUri
import com.example.android.uamp.media.extensions.title
import com.example.android.uamp.media.extensions.toMediaItem
import com.example.android.uamp.media.extensions.trackNumber
import com.example.android.uamp.media.library.AbstractMusicSource
import com.example.android.uamp.media.library.BrowseTree
import com.example.android.uamp.media.library.JsonSource
import com.example.android.uamp.media.library.MEDIA_SEARCH_SUPPORTED
import com.example.android.uamp.media.library.MusicSource
import com.example.android.uamp.media.library.UAMP_BROWSABLE_ROOT
import com.example.android.uamp.media.library.UAMP_EMPTY_ROOT
import com.example.android.uamp.media.library.UAMP_RECENT_ROOT
import com.example.android.uamp.media.library.VN_EXPRESS_ROOT
import com.example.android.uamp.media.model.ItemNews
import com.example.android.uamp.media.model.NewsType
import com.example.android.uamp.media.model.PrefixRoot
import com.example.android.uamp.media.model.TuoiTreConstant
import com.example.android.uamp.media.model.VnExpressConstant
import com.example.android.uamp.media.network.MarsApi
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.EVENT_MEDIA_ITEM_TRANSITION
import com.google.android.exoplayer2.Player.EVENT_PLAY_WHEN_READY_CHANGED
import com.google.android.exoplayer2.Player.EVENT_POSITION_DISCONTINUITY
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.Util.constrainValue
import com.google.android.gms.cast.framework.CastContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import java.util.*

/**
 * This class is the entry point for browsing and playback commands from the APP's UI
 * and other apps that wish to play music via UAMP (for example, Android Auto or
 * the Google Assistant).
 *
 * Browsing begins with the method [MusicService.onGetRoot], and continues in
 * the callback [MusicService.onLoadChildren].
 *
 * For more information on implementing a MediaBrowserService,
 * visit [https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice.html](https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice.html).
 *
 * This class also handles playback for Cast sessions.
 * When a Cast session is active, playback commands are passed to a
 * [CastPlayer](https://exoplayer.dev/doc/reference/com/google/android/exoplayer2/ext/cast/CastPlayer.html),
 * otherwise they are passed to an ExoPlayer for local playback.
 */
open class MusicService : MediaBrowserServiceCompat(), TextToSpeech.OnInitListener {

    private lateinit var notificationManager: UampNotificationManager
    private lateinit var mediaSource: MusicSource
    private lateinit var packageValidator: PackageValidator

    // The current player will either be an ExoPlayer (for local playback) or a CastPlayer (for
    // remote playback through a Cast device).
    private lateinit var currentPlayer: Player
    private var tts: TextToSpeech? = null

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    protected lateinit var mediaSession: MediaSessionCompat
    protected lateinit var mediaSessionConnector: MediaSessionConnector
    private var currentPlaylistItems: List<MediaMetadataCompat> = emptyList()
    private var currentMediaItemIndex: Int = 0

    private lateinit var storage: PersistentStorage

    /**
     * This must be `by lazy` because the source won't initially be ready.
     * See [MusicService.onLoadChildren] to see where it's accessed (and first
     * constructed).
     */
    private val browseTree: BrowseTree by lazy {
        BrowseTree(applicationContext, mediaSource)
    }

    private var isForegroundService = false

    private val remoteJsonSource: Uri =
        Uri.parse("https://storage.googleapis.com/uamp/catalog.json")

    private val uAmpAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    private val playerListener = PlayerEventListener()

    /**
     * Configure ExoPlayer to handle audio focus for us.
     * See [Player.AudioComponent.setAudioAttributes] for details.
     */
    private val exoPlayer: ExoPlayer by lazy {
        val extractorsFactory = DefaultExtractorsFactory().setConstantBitrateSeekingEnabled(true)
        SimpleExoPlayer.Builder(this, extractorsFactory).build().apply {
            setAudioAttributes(uAmpAudioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            addListener(playerListener)
        }
    }

    /**
     * If Cast is available, create a CastPlayer to handle communication with a Cast session.
     */
    private val castPlayer: CastPlayer? by lazy {
        println("AAA castPlayer")
        try {
            val castContext = CastContext.getSharedInstance(this)
            CastPlayer(castContext, CastMediaItemConverter()).apply {
                setSessionAvailabilityListener(UampCastSessionAvailabilityListener())
                addListener(playerListener)
            }
        } catch (e: Exception) {
            // We wouldn't normally catch the generic `Exception` however
            // calling `CastContext.getSharedInstance` can throw various exceptions, all of which
            // indicate that Cast is unavailable.
            // Related internal bug b/68009560.
            Log.i(
                TAG, "Cast is not available on this device. " +
                        "Exception thrown when attempting to obtain CastContext. " + e.message
            )
            null
        }
    }


    override fun onCreate() {
        super.onCreate()
        println("AAA onCreate service")

        // Build a PendingIntent that can be used to launch the UI.
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        // Create a new MediaSession.
        mediaSession = MediaSessionCompat(this, "MusicService")
            .apply {
                setSessionActivity(sessionActivityPendingIntent)
                isActive = true
            }
        /**
         * In order for [MediaBrowserCompat.ConnectionCallback.onConnected] to be called,
         * a [MediaSessionCompat.Token] needs to be set on the [MediaBrowserServiceCompat].
         *
         * It is possible to wait to set the session token, if required for a specific use-case.
         * However, the token *must* be set by the time [MediaBrowserServiceCompat.onGetRoot]
         * returns, or the connection will fail silently. (The system will not even call
         * [MediaBrowserCompat.ConnectionCallback.onConnectionFailed].)
         */
        sessionToken = mediaSession.sessionToken

        /**
         * The notification manager will use our player and media session to decide when to post
         * notifications. When notifications are posted or removed our listener will be called, this
         * allows us to promote the service to foreground (required so that we're not killed if
         * the main UI is not visible).
         */
        notificationManager = UampNotificationManager(
            this,
            mediaSession.sessionToken,
            PlayerNotificationListener()
        )

        // The media library is built from a remote JSON file. We'll create the source here,
        // and then use a suspend function to perform the download off the main thread.
        mediaSource = JsonSource(source = remoteJsonSource)
        serviceScope.launch {
            mediaSource.load()
        }

        // ExoPlayer will manage the MediaSession for us.
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(UampPlaybackPreparer())
        mediaSessionConnector.setQueueNavigator(UampQueueNavigator(mediaSession))

        switchToPlayer(
            previousPlayer = null,
            newPlayer = if (castPlayer?.isCastSessionAvailable == true) castPlayer!! else exoPlayer
        )
        notificationManager.showNotificationForPlayer(currentPlayer)

        packageValidator = PackageValidator(this, R.xml.allowed_media_browser_callers)

        storage = PersistentStorage.getInstance(applicationContext)
        tts = TextToSpeech(this, this)
    }

    /**
     * This is the code that causes UAMP to stop playing when swiping the activity away from
     * recents. The choice to do this is app specific. Some apps stop playback, while others allow
     * playback to continue and allow users to stop it with the notification.
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        saveRecentSongToStorage()
        super.onTaskRemoved(rootIntent)

        /**
         * By stopping playback, the player will transition to [Player.STATE_IDLE] triggering
         * [Player.EventListener.onPlayerStateChanged] to be called. This will cause the
         * notification to be hidden and trigger
         * [PlayerNotificationManager.NotificationListener.onNotificationCancelled] to be called.
         * The service will then remove itself as a foreground service, and will call
         * [stopSelf].
         */
        currentPlayer.stop(/* reset= */true)
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }

        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }

        // Cancel coroutines when the service is going away.
        serviceJob.cancel()

        // Free ExoPlayer resources.
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }

    /**
     * Returns the "root" media ID that the client should request to get the list of
     * [MediaItem]s to browse/play.
     */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {

        /*
         * By default, all known clients are permitted to search, but only tell unknown callers
         * about search if permitted by the [BrowseTree].
         */
        val isKnownCaller = packageValidator.isKnownCaller(clientPackageName, clientUid)
        val rootExtras = Bundle().apply {
            putBoolean(
                MEDIA_SEARCH_SUPPORTED,
                isKnownCaller || browseTree.searchableByUnknownCaller
            )
            putBoolean(CONTENT_STYLE_SUPPORTED, true)
            putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
            putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
        }

        return if (isKnownCaller) {
            /**
             * By default return the browsable root. Treat the EXTRA_RECENT flag as a special case
             * and return the recent root instead.
             */
            val isRecentRequest = rootHints?.getBoolean(EXTRA_RECENT) ?: false
            val browserRootPath = if (isRecentRequest) UAMP_RECENT_ROOT else UAMP_BROWSABLE_ROOT
            BrowserRoot(browserRootPath, rootExtras)
        } else {
            /**
             * Unknown caller. There are two main ways to handle this:
             * 1) Return a root without any content, which still allows the connecting client
             * to issue commands.
             * 2) Return `null`, which will cause the system to disconnect the app.
             *
             * UAMP takes the first approach for a variety of reasons, but both are valid
             * options.
             */
            BrowserRoot(UAMP_EMPTY_ROOT, rootExtras)
        }
    }

    private var listItemNews = listOf<ItemNews>()

    private var listMediaMetaData = mutableListOf<MediaMetadataCompat>()
    private var newsType = NewsType.VN_EXPRESS

    /**
     * Returns (via the [result] parameter) a list of [MediaItem]s that are child
     * items of the provided [parentMediaId]. See [BrowseTree] for more details on
     * how this is build/more details about the relationships.
     */
    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<List<MediaItem>>
    ) {
        when (parentMediaId.substring(0, 3)) {
            PrefixRoot.LOC.name -> {
                val children = browseTree[parentMediaId]?.map { item ->
                    MediaItem(item.description, item.flag)
                }
                if (parentMediaId == Utils.generalMediaItemId(PrefixRoot.LOC, VN_EXPRESS_ROOT)) {
                    newsType = NewsType.VN_EXPRESS
                } else {
                    newsType = NewsType.TUOI_TRE
                }
                result.sendResult(children)
            }
            PrefixRoot.API.name -> {
                val children = browseTree[parentMediaId]
                children?.get(0)?.let {
                    serviceScope.launch {
                        val rssObject = MarsApi.retrofitService.getAllFeeds(it.mediaUri.toString())
                        val mediaItems = mutableListOf<MediaItem>()
                        listItemNews = rssObject.items
                        rssObject.items.forEachIndexed { index, item ->
                            val mediaMetadata = MediaMetadataCompat.Builder().apply {
                                id = "${PrefixRoot.CRW.name}__$index"
                                title = item.title
                                albumArtUri = item.thumbnail
                                mediaUri = item.link
                                date = item.pubDate
                                flag = MediaItem.FLAG_BROWSABLE
                            }.build()
                            mediaItems.add(MediaItem(mediaMetadata.description, mediaMetadata.flag))
                        }
                        result.sendResult(mediaItems)
                    }
                }
                result.detach()
            }
            PrefixRoot.CRW.name -> {
                val index = parentMediaId.split("__")[1].toInt()
                val itemNews = listItemNews[index]
                serviceScope.launch(Dispatchers.IO) {
                    try {
//                        val document: Document = Jsoup.connect(itemNews.link).get()
//                        val contents = mutableListOf<String>()
//                        val titleDetail = document.select(VnExpressConstant.TITLE_DETAIL).text()
//                        val description = document.select(VnExpressConstant.DESCRIPTION).text()
//                        contents.add(titleDetail)
//                        contents.add(description)
//                        document.select(VnExpressConstant.NORMAL).forEach { element ->
//                            contents.add(element.text())
//                        }
                        val children = if (newsType == NewsType.VN_EXPRESS) {
                            crawAndRecordText(
                                itemNews,
                                VnExpressConstant.TITLE_DETAIL,
                                VnExpressConstant.DESCRIPTION,
                                VnExpressConstant.NORMAL
                            )
                        } else {
                            crawAndRecordText(
                                itemNews,
                                TuoiTreConstant.TITLE_DETAIL,
                                TuoiTreConstant.DESCRIPTION,
                                TuoiTreConstant.MAIN_DETAIL_BODY
                            )
                        }
                        if (children.size > 0) {
                            result.sendResult(children)
                        } else {
                            result.sendResult(null)
                        }
                    } catch (e: IOException) {
                        Log.d("AAA", e.message.toString())
                    }
                }
                result.detach()
            }
            else -> {
                result.sendResult(null)
            }
        }
    }

    private fun crawAndRecordText(
        itemNews: ItemNews,
        titleNews: String,
        descriptionNews: String,
        contentNews: String
    ): MutableList<MediaItem> {
        // craw data
        val document: Document = Jsoup.connect(itemNews.link).get()
        val contents = mutableListOf<String>()
        val titleDetail = document.select(titleNews).text()
        val description = document.select(descriptionNews).text()
        contents.add(titleDetail)
        contents.add(description)
        document.select(contentNews).forEach { element ->
            contents.add(element.text())
        }

        // record text
        val myBundleAlarm = Bundle()
        myBundleAlarm.putString(
            TextToSpeech.Engine.KEY_PARAM_STREAM,
            AudioManager.STREAM_ALARM.toString()
        )
        myBundleAlarm.putString(TextToSpeech.Engine.KEY_PARAM_VOLUME, "1")
        myBundleAlarm.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "dung_nh99")
        val mediaItems = mutableListOf<MediaItem>()
        val mediaMetadataCompats = mutableListOf<MediaMetadataCompat>()
        contents.forEachIndexed { index, content ->
            val fileName = this@MusicService.cacheDir.absolutePath + "tts_$index.wav"
            val soundFile = File(fileName)
            if (soundFile.exists()) soundFile.delete()
            if (tts!!.synthesizeToFile(
                    content,
                    myBundleAlarm,
                    File(fileName),
                    "dung_nh99"
                ) == TextToSpeech.SUCCESS
            ) {
                println("AAA Sound file created")
                val mediaMetadata = MediaMetadataCompat.Builder().apply {
                    id = "${PrefixRoot.WAV.name}__$index"
                    title = contents[0]
                    artist = "VnExpress"
                    albumArtUri = itemNews.thumbnail
                    mediaUri = "file:///$fileName"
                    flag = MediaItem.FLAG_PLAYABLE
                }.build()
                mediaMetadataCompats.add(mediaMetadata)
                mediaItems.add(MediaItem(mediaMetadata.description, mediaMetadata.flag))
            } else {
                println("AAA Oops! Sound file not created")
            }
        }
        listMediaMetaData = mediaMetadataCompats
        return mediaItems
    }


    /**
     * Returns a list of [MediaItem]s that match the given search query
     */
    override fun onSearch(
        query: String,
        extras: Bundle?,
        result: Result<List<MediaItem>>
    ) {

        val resultsSent = mediaSource.whenReady { successfullyInitialized ->
            if (successfullyInitialized) {
                val resultsList = mediaSource.search(query, extras ?: Bundle.EMPTY)
                    .map { mediaMetadata ->
                        MediaItem(mediaMetadata.description, mediaMetadata.flag)
                    }
                result.sendResult(resultsList)
            }
        }

        if (!resultsSent) {
            result.detach()
        }
    }

    /**
     * Load the supplied list of songs and the song to play into the current player.
     */
    private fun preparePlaylist(
        metadataList: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playWhenReady: Boolean,
        playbackStartPositionMs: Long
    ) {
        // Since the playlist was probably based on some ordering (such as tracks
        // on an album), find which window index to play first so that the song the
        // user actually wants to hear plays first.
        val initialWindowIndex = if (itemToPlay == null) 0 else metadataList.indexOf(itemToPlay)
        currentPlaylistItems = metadataList

        currentPlayer.playWhenReady = playWhenReady
        currentPlayer.stop()
        // Set playlist and prepare.
        currentPlayer.setMediaItems(
            metadataList.map { it.toMediaItem() }, initialWindowIndex, playbackStartPositionMs
        )
        currentPlayer.prepare()
    }

    private fun switchToPlayer(previousPlayer: Player?, newPlayer: Player) {
        if (previousPlayer == newPlayer) {
            return
        }
        currentPlayer = newPlayer
        if (previousPlayer != null) {
            val playbackState = previousPlayer.playbackState
            if (currentPlaylistItems.isEmpty()) {
                // We are joining a playback session. Loading the session from the new player is
                // not supported, so we stop playback.
                currentPlayer.clearMediaItems()
                currentPlayer.stop()
            } else if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
                preparePlaylist(
                    metadataList = currentPlaylistItems,
                    itemToPlay = currentPlaylistItems[currentMediaItemIndex],
                    playWhenReady = previousPlayer.playWhenReady,
                    playbackStartPositionMs = previousPlayer.currentPosition
                )
            }
        }
        mediaSessionConnector.setPlayer(newPlayer)
        previousPlayer?.stop(/* reset= */true)
    }

    private fun saveRecentSongToStorage() {

        // Obtain the current song details *before* saving them on a separate thread, otherwise
        // the current player may have been unloaded by the time the save routine runs.
        if (currentPlaylistItems.isEmpty()) {
            return
        }
        val description = currentPlaylistItems[currentMediaItemIndex].description
        val position = currentPlayer.currentPosition

        serviceScope.launch {
            storage.saveRecentSong(
                description,
                position
            )
        }
    }

    private inner class UampCastSessionAvailabilityListener : SessionAvailabilityListener {

        /**
         * Called when a Cast session has started and the user wishes to control playback on a
         * remote Cast receiver rather than play audio locally.
         */
        override fun onCastSessionAvailable() {
            switchToPlayer(currentPlayer, castPlayer!!)
        }

        /**
         * Called when a Cast session has ended and the user wishes to control playback locally.
         */
        override fun onCastSessionUnavailable() {
            switchToPlayer(currentPlayer, exoPlayer)
        }
    }

    private inner class UampQueueNavigator(
        mediaSession: MediaSessionCompat
    ) : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            if (windowIndex < currentPlaylistItems.size) {
                return currentPlaylistItems[windowIndex].description
            }
            return MediaDescriptionCompat.Builder().build()
        }
    }

    private inner class UampPlaybackPreparer : MediaSessionConnector.PlaybackPreparer {

        /**
         * UAMP supports preparing (and playing) from search, as well as media ID, so those
         * capabilities are declared here.
         *
         * TODO: Add support for ACTION_PREPARE and ACTION_PLAY, which mean "prepare/play something".
         */
        override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

        override fun onPrepare(playWhenReady: Boolean) {
            val recentSong = storage.loadRecentSong() ?: return
            onPrepareFromMediaId(
                recentSong.mediaId!!,
                playWhenReady,
                recentSong.description.extras
            )
        }


        override fun onPrepareFromMediaId(
            mediaId: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {
            val itemToPlay: MediaMetadataCompat? = listMediaMetaData.find { item ->
                item.id == mediaId
            }
            if (itemToPlay == null) {
                Log.w(TAG, "Content not found: MediaID=$mediaId")
                // TODO: Notify caller of the error.
            } else {

                val playbackStartPositionMs =
                    extras?.getLong(
                        MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS,
                        C.TIME_UNSET
                    )
                        ?: C.TIME_UNSET

                preparePlaylist(
                    listMediaMetaData,
                    itemToPlay,
                    playWhenReady,
                    playbackStartPositionMs
                )
            }

        }

        /**
         * This method is used by the Google Assistant to respond to requests such as:
         * - Play Geisha from Wake Up on UAMP
         * - Play electronic music on UAMP
         * - Play music on UAMP
         *
         * For details on how search is handled, see [AbstractMusicSource.search].
         */
        override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) {
            mediaSource.whenReady {
                val metadataList = mediaSource.search(query, extras ?: Bundle.EMPTY)
                if (metadataList.isNotEmpty()) {
                    preparePlaylist(
                        metadataList,
                        metadataList[0],
                        playWhenReady,
                        playbackStartPositionMs = C.TIME_UNSET
                    )
                }
            }
        }

        override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit

        override fun onCommand(
            player: Player,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ) = false


        /**
         * Builds a playlist based on a [MediaMetadataCompat].
         *
         * TODO: Support building a playlist by artist, genre, etc...
         *
         * @param item Item to base the playlist on.
         * @return a [List] of [MediaMetadataCompat] objects representing a playlist.
         */
        private fun buildPlaylist(item: MediaMetadataCompat): List<MediaMetadataCompat> =
            mediaSource.filter { it.album == item.album }.sortedBy { it.trackNumber }
    }

    /**
     * Listen for notification events.
     */
    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, this@MusicService.javaClass)
                )

                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    /**
     * Listen for events from ExoPlayer.
     */
    private inner class PlayerEventListener : Player.Listener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING,
                Player.STATE_READY -> {

                    notificationManager.showNotificationForPlayer(currentPlayer)
                    if (playbackState == Player.STATE_READY) {

                        // When playing/paused save the current media item in persistent
                        // storage so that playback can be resumed between device reboots.
                        // Search for "media resumption" for more information.
                        saveRecentSongToStorage()

                        if (!playWhenReady) {
                            // If playback is paused we remove the foreground state which allows the
                            // notification to be dismissed. An alternative would be to provide a
                            // "close" button in the notification which stops playback and clears
                            // the notification.
                            stopForeground(false)
                            isForegroundService = false
                        }
                    }
                }
                else -> {
                    println("AAA onPlayerStateChanged other")
                    notificationManager.hideNotification()
                }
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (events.contains(EVENT_POSITION_DISCONTINUITY)
                || events.contains(EVENT_MEDIA_ITEM_TRANSITION)
                || events.contains(EVENT_PLAY_WHEN_READY_CHANGED)
            ) {
                currentMediaItemIndex = if (currentPlaylistItems.isNotEmpty()) {
                    constrainValue(
                        player.currentMediaItemIndex,
                        /* min= */ 0,
                        /* max= */ currentPlaylistItems.size - 1
                    )
                } else 0
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            var message = R.string.generic_error;
            Log.e(TAG, "Player error: " + error.errorCodeName + " (" + error.errorCode + ")");
            if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
                || error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
            ) {
                message = R.string.error_media_not_found;
            }
            Toast.makeText(
                applicationContext,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale("vi_VN"))

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            }
            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {
                    println("AAA onStart")
                }

                override fun onDone(utteranceId: String) {
                    println("AAA ondone")
                }

                override fun onError(utteranceId: String) {
                    println("AAA onError")
                }
            })

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }
}

/*
 * (Media) Session events
 */
const val NETWORK_FAILURE = "com.example.android.uamp.media.session.NETWORK_FAILURE"

/** Content styling constants */
private const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
private const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
private const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
private const val CONTENT_STYLE_LIST = 1
private const val CONTENT_STYLE_GRID = 2

private const val UAMP_USER_AGENT = "uamp.next"

val MEDIA_DESCRIPTION_EXTRAS_START_PLAYBACK_POSITION_MS = "playback_start_position_ms"

private const val TAG = "MusicService"
