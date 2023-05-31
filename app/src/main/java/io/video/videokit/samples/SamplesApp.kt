package io.video.videokit.samples

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.video.videokit.VideoKit
import io.video.videokit.playlist.FilteredPlaylistSpec
import io.video.videokit.playlist.Playlist
import io.video.videokit.samples.player.FeedView
import io.video.videokit.samples.player.SingleVideoView
import io.video.videokit.samples.recorder.RecorderView
import io.video.videokit.samples.ui.theme.VideoKitSamplesTheme
import io.video.videokit.video.FilteredVideosRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.UUID

val VideoKitAppToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2lkIjoiMW81cFBiVzMwNG44SXVwR2JVSm8iLCJyb2xlIjoiYXBwIiwiaWF0IjoxNjEyMTY2MzkwLCJpc3MiOiJ2aWRlby5pbyIsImp0aSI6ImZQN290S3dFb2V5U2tGNVNzQVBmLXdkaDU1In0.ebYY3nXYCyc9b8NuQZ742ejLEKsqxh0lZiK7FjtmKBM"
val VideoKitUserId = UUID.randomUUID().toString()

class SamplesAppViewModel(val scope: CoroutineScope) {
    init {
        require(VideoKitAppToken.isNotEmpty()) { "Please set the VideoKitAppToken variable." }
        VideoKit.sessions().start(
            appToken = VideoKitAppToken,
            identity = VideoKitUserId
        )
    }

    var sample by mutableStateOf<Sample?>(null)
    var message by mutableStateOf<String?>(null)

    fun close() {
        sample = null
    }

    fun openRecorder() {
        message = null
        sample = Sample.Recorder
    }

    fun openVideoPlayer() {
        message = "Loading latest video..."
        val req = FilteredVideosRequest(limit = 1)
        VideoKit.videos().getList(req).onResult {
            val video = it.dataOrNull()?.firstOrNull()
            if (video != null) {
                message = null
                sample = Sample.PlayVideo(video)
            } else {
                message = "Could not find any playable video for the given VideoKit app token. Err: ${it.exceptionOrNull()}"
            }
        }
    }

    fun openVideoPlaylist() {
        message = "Loading playlist with latest videos..."
        val spec = FilteredPlaylistSpec()
        VideoKit.videos().loadPlaylist(scope, spec).onResult {
            val playlist = it.dataOrNull()?.takeIf { it.size > 0 }
            if (playlist != null) {
                message = null
                sample = Sample.PlayFeed(playlist)
            } else {
                message = "Could not find any playable video for the given VideoKit app token. Err: ${it.exceptionOrNull()}"
            }
        }
    }
}

class SamplesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoKitSamplesTheme {
                val scope = rememberCoroutineScope()
                val vm = remember { SamplesAppViewModel(scope) }
                SamplesAppContent(vm)
            }
        }
    }
}

@Composable
private fun SamplesAppContent(vm: SamplesAppViewModel) {
    when (val sample = vm.sample) {
        null -> Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(16.dp, Alignment.CenterVertically), Alignment.CenterHorizontally) {
                OutlinedButton(vm::openRecorder) { Text("Recorder") }
                OutlinedButton(vm::openVideoPlayer) { Text("Player (Single video)") }
                OutlinedButton(vm::openVideoPlaylist) { Text("Player (TikTok-like feed)") }
            }
            val message = vm.message
            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(16.dp).align(Alignment.BottomCenter)
                )
            }
        }
        is Sample.Recorder -> {
            RecorderView(Modifier.fillMaxSize())
        }
        is Sample.PlayVideo -> {
            SingleVideoView(sample.video, Modifier.fillMaxSize())
        }
        is Sample.PlayFeed -> {
            FeedView(sample.playlist, Modifier.fillMaxSize())
        }
    }
    if (vm.sample != null) {
        BackHandler { vm.close() }
    }
}