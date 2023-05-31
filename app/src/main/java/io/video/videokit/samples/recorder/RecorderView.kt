package io.video.videokit.samples.recorder

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import io.video.videokit.recorder.Recorder
import io.video.videokit.recorder.RecorderState
import io.video.videokit.recorder.ui.Recorder
import io.video.videokit.recorder.ui.rememberRecorder
import io.video.videokit.samples.R
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun RecorderView(modifier: Modifier = Modifier) {
    val recorder = rememberRecorder()
    Box(modifier) {
        Recorder(recorder, Modifier.fillMaxSize())
        RecorderControls(recorder, Modifier.fillMaxSize())
    }
}


@Composable
private fun RecorderControls(recorder: Recorder, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val scope = rememberCoroutineScope()
        val durations = remember(recorder) {
            recorder.durationFlow(scope)
                .debounce(100)
                .map { millis ->
                    val minutes = (millis / 1000) / 60
                    val seconds = (millis / 1000) % 60
                    String.format("%02d:%02d", minutes, seconds)
                }
        }
        val duration by durations.collectAsState("")
        val state by remember(recorder) {
            recorder.stateFlow.debounce { if (it == RecorderState.Busy) 400L else 0L }
        }.collectAsState(RecorderState.Busy)
        val recording by recorder.recordingFlow.collectAsState(null)

        val shouldShowTimer = state != RecorderState.Preview && recording != null

        if (shouldShowTimer) {
            val isRecording = state == RecorderState.Recording
            val bgColor = when {
                isRecording -> Color(red = 1.0F, green = 19F / 255, blue = 0F)
                else -> Color(red = 14F / 255F, green = 14F / 255, blue = 14F / 255F)
            }
            Row(
                modifier = Modifier
                    .background(bgColor, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (isRecording) {
                    Image(
                        painter = painterResource(R.drawable.videokit_recorder_controls_timer),
                        contentDescription = null
                    )
                }
                BasicText(
                    text = duration,
                    style = TextStyle.Default.copy(
                        color = Color.White,
                        fontSize = TextUnit(12F, TextUnitType.Sp),
                        fontWeight = FontWeight.Normal
                    ),
                )
            }
        }

        Spacer(Modifier.weight(1F))

        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val canDelete = recording != null
            Image(
                painter = painterResource(R.drawable.videokit_recorder_controls_delete),
                contentDescription = "Delete",
                modifier = Modifier
                    .alpha(if (canDelete) 1F else 0F)
                    .clickable(canDelete) {
                        if (state == RecorderState.Preview) recorder.exitPreview()
                        else recorder.reset()
                    }
            )

            val canRestart = recording != null
            Image(
                painter = painterResource(R.drawable.videokit_recorder_controls_restart),
                contentDescription = "Restart",
                modifier = Modifier
                    .alpha(if (canRestart) 1F else 0F)
                    .clickable(canRestart) {
                        scope.launch {
                            runCatching {
                                recorder.resetSuspending()
                                recorder.startSuspending()
                            }
                        }
                    }
            )

            when (state) {
                RecorderState.Idle -> {
                    Image(
                        painter = painterResource(R.drawable.videokit_recorder_controls_record),
                        contentDescription = "Record",
                        modifier = Modifier.clickable { recorder.start() }
                    )
                }

                RecorderState.Preview, RecorderState.Busy -> {
                    Image(
                        painter = painterResource(R.drawable.videokit_recorder_controls_record),
                        contentDescription = null,
                        modifier = Modifier.alpha(0F)
                    )
                }

                RecorderState.Recording -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.clickable { recorder.pause() }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.videokit_recorder_controls_pause),
                            contentDescription = "Pause"
                        )
                        val rotationTransition = rememberInfiniteTransition()
                        val rotation by rotationTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = keyframes { durationMillis = 800 }
                            )
                        )
                        Image(
                            painter = painterResource(R.drawable.videokit_recorder_controls_pause_indicator),
                            contentDescription = null,
                            modifier = Modifier.graphicsLayer { rotationZ = rotation }
                        )
                    }
                }
            }

            val canToggle = state != RecorderState.Busy && state != RecorderState.Preview
            Image(
                painter = painterResource(R.drawable.videokit_recorder_controls_toggle),
                contentDescription = "Toggle camera",
                modifier = Modifier
                    .alpha(if (canToggle) 1F else 0F)
                    .clickable(canToggle) { recorder.camera.toggleDirection() }
            )

            val canProceed = state != RecorderState.Busy && recording != null
            Image(
                painter = painterResource(R.drawable.videokit_recorder_controls_send),
                contentDescription = if (state == RecorderState.Preview) "Confirm" else "Preview",
                modifier = Modifier
                    .alpha(if (canProceed) 1F else 0.5F)
                    .clickable(canProceed) {
                        if (state == RecorderState.Preview) recorder.stop()
                        else recorder.enterPreview()
                    }
            )
        }
    }
}