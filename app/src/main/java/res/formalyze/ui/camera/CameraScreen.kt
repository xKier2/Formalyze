// Full-screen live camera view with tap-to-focus, pinch-to-zoom and a button to switch lenses.
// The chosen lens survives rotation because it is saved state; the camera rebinds whenever the
// lens or the screen changes. A display listener keeps the picture upright on 180° flips,
// which Android does not report as a configuration change.
package res.formalyze.ui.camera

import android.hardware.display.DisplayManager
import android.view.Surface
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import res.formalyze.R
import res.formalyze.camera.CameraSession
import res.formalyze.camera.CameraSettings
import kotlin.math.roundToInt

@Composable
fun CameraScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val hostView = LocalView.current
    val cameraSession = remember { CameraSession(context.applicationContext) }
    var selectedLens by rememberSaveable { mutableStateOf(CameraSettings.defaultLens) }

    val surfaceRequest by cameraSession.surfaceRequest.collectAsStateWithLifecycle()
    val canSwitchLens by cameraSession.canSwitchLens.collectAsStateWithLifecycle()
    val noCameraFound by cameraSession.noCameraFound.collectAsStateWithLifecycle()

    LaunchedEffect(lifecycleOwner, selectedLens) {
        cameraSession.bindToLifecycle(
            lifecycleOwner = lifecycleOwner,
            requestedLens = selectedLens,
            displayRotation = hostView.display?.rotation ?: Surface.ROTATION_0,
        )
    }
    DisplayRotationEffect(onRotationChanged = cameraSession::updateTargetRotation)

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        surfaceRequest?.let { activeSurfaceRequest ->
            CameraViewfinder(surfaceRequest = activeSurfaceRequest, cameraSession = cameraSession)
        }
        if (noCameraFound) {
            Text(
                text = stringResource(R.string.camera_not_found),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
            )
        }
        if (canSwitchLens) {
            FilledTonalButton(
                onClick = { selectedLens = selectedLens.opposite() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding()
                    .padding(bottom = 24.dp),
            ) {
                Text(stringResource(R.string.camera_switch_lens))
            }
        }
    }
}

// Shows the camera picture and turns touch gestures into focus and zoom commands.
// Tap positions are converted from screen space to camera-frame space before focusing.
@Composable
private fun CameraViewfinder(surfaceRequest: SurfaceRequest, cameraSession: CameraSession) {
    val coordinateTransformer = remember { MutableCoordinateTransformer() }
    var focusIndicatorPosition by remember { mutableStateOf<Offset?>(null) }
    val pinchZoomState = rememberTransformableState { zoomChange, _, _ ->
        cameraSession.zoomBy(zoomChange)
    }

    LaunchedEffect(focusIndicatorPosition) {
        if (focusIndicatorPosition != null) {
            delay(CameraSettings.focusIndicatorVisibleMillis)
            focusIndicatorPosition = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraXViewfinder(
            surfaceRequest = surfaceRequest,
            coordinateTransformer = coordinateTransformer,
            contentScale = CameraSettings.previewContentScale,
            modifier = Modifier
                .fillMaxSize()
                .transformable(pinchZoomState)
                .pointerInput(cameraSession) {
                    detectTapGestures { tapPosition ->
                        val surfacePosition = with(coordinateTransformer) { tapPosition.transform() }
                        if (cameraSession.focusAt(surfacePosition.x, surfacePosition.y)) {
                            focusIndicatorPosition = tapPosition
                        }
                    }
                },
        )
        focusIndicatorPosition?.let { tapPosition -> FocusIndicator(centerPosition = tapPosition) }
    }
}

// A white ring drawn briefly where the user tapped, so they know focus was requested there.
@Composable
private fun FocusIndicator(centerPosition: Offset) {
    val indicatorDiameter = 64.dp
    val indicatorRadiusPx = with(LocalDensity.current) { indicatorDiameter.toPx() / 2 }
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (centerPosition.x - indicatorRadiusPx).roundToInt(),
                    y = (centerPosition.y - indicatorRadiusPx).roundToInt(),
                )
            }
            .size(indicatorDiameter)
            .border(width = 2.dp, color = Color.White, shape = CircleShape),
    )
}

// Reports the screen's rotation whenever the display changes, including upside-down flips.
@Composable
private fun DisplayRotationEffect(onRotationChanged: (Int) -> Unit) {
    val context = LocalContext.current
    val hostView = LocalView.current
    val latestOnRotationChanged by rememberUpdatedState(onRotationChanged)

    DisposableEffect(hostView) {
        val displayManager = context.getSystemService(DisplayManager::class.java)
        val displayListener = object : DisplayManager.DisplayListener {
            override fun onDisplayChanged(displayId: Int) {
                val hostDisplay = hostView.display ?: return
                if (hostDisplay.displayId == displayId) latestOnRotationChanged(hostDisplay.rotation)
            }

            override fun onDisplayAdded(displayId: Int) = Unit

            override fun onDisplayRemoved(displayId: Int) = Unit
        }
        displayManager.registerDisplayListener(displayListener, null)
        onDispose { displayManager.unregisterDisplayListener(displayListener) }
    }
}
