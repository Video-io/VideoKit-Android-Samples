package io.video.videokit.samples.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.viewpager2.widget.ViewPager2
import io.video.videokit.player.ui.rememberPlayersManager
import io.video.videokit.playlist.Playlist

@Composable
fun FeedView(playlist: Playlist, modifier: Modifier = Modifier) {
    val pm = rememberPlayersManager(playlist)
    val vm = remember(pm) { FeedViewModel(pm) }
    AndroidView(
        factory = {
            ViewPager2(it).apply {
                vm.initialize(this)
            }
        },
        modifier = modifier,
    )
}