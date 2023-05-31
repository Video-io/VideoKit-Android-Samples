package io.video.videokit.samples

import io.video.videokit.playlist.Playlist
import io.video.videokit.video.Video

sealed interface Sample {
    object Recorder : Sample
    class PlayVideo(val video: Video) : Sample
    class PlayFeed(val playlist: Playlist) : Sample
}