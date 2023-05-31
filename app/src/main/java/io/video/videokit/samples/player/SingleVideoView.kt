package io.video.videokit.samples.player

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import io.video.videokit.player.AspectMode
import io.video.videokit.player.LoopMode
import io.video.videokit.player.ui.Player
import io.video.videokit.player.ui.rememberPlayer
import io.video.videokit.video.Video

@Composable
fun SingleVideoView(video: Video, modifier: Modifier = Modifier) {
    val player = rememberPlayer {
        loopMode = LoopMode.Repeat
        aspectMode = AspectMode.Crop
    }
    LaunchedEffect(video) {
        player.set(video, play = true)
    }
    Player(player, modifier.clickable {
        player.toggle()
    })
}