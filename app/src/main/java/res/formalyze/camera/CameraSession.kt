// Owns one live camera preview: binds it to the screen's lifecycle, hands frames to the viewfinder,
// and applies user actions such as zoom, tap-to-focus and rotation changes.
// Binding stays active until the calling coroutine is cancelled, then the camera is released.
// If the phone can't combine stabilization with the requested settings, the preview retries without it.
// The front camera is mirrored by CameraX's default preview behavior, so no extra setting is needed.
package res.formalyze.camera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraSession(private val appContext: Context) {
    private val mutableSurfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = mutableSurfaceRequest.asStateFlow()

    private val mutableCanSwitchLens = MutableStateFlow(false)
    val canSwitchLens: StateFlow<Boolean> = mutableCanSwitchLens.asStateFlow()

    private val mutableNoCameraFound = MutableStateFlow(false)
    val noCameraFound: StateFlow<Boolean> = mutableNoCameraFound.asStateFlow()

    private var boundCamera: Camera? = null
    private var activePreview: Preview? = null
    private var currentZoomRatio = 1f

    suspend fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
        requestedLens: CameraLens,
        displayRotation: Int,
    ) {
        val cameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        val availableLenses = CameraLens.entries.filter { lens -> cameraProvider.hasCameraSafely(lens) }
        mutableCanSwitchLens.value = availableLenses.size > 1
        val lensToUse = if (requestedLens in availableLenses) requestedLens else availableLenses.firstOrNull()
        if (lensToUse == null) {
            mutableNoCameraFound.value = true
            return
        }

        cameraProvider.unbindAll()
        val preview = bindPreview(cameraProvider, lifecycleOwner, lensToUse, displayRotation)
        try {
            awaitCancellation()
        } finally {
            cameraProvider.unbind(preview)
            boundCamera = null
            activePreview = null
            mutableSurfaceRequest.value = null
        }
    }

    fun zoomBy(scaleFactor: Float) {
        val camera = boundCamera ?: return
        val zoomState = camera.cameraInfo.zoomState.value ?: return
        currentZoomRatio = (currentZoomRatio * scaleFactor)
            .coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)
        camera.cameraControl.setZoomRatio(currentZoomRatio)
    }

    fun focusAt(surfaceX: Float, surfaceY: Float): Boolean {
        val camera = boundCamera ?: return false
        val surfaceResolution = mutableSurfaceRequest.value?.resolution ?: return false
        val surfaceWidth = surfaceResolution.width.toFloat()
        val surfaceHeight = surfaceResolution.height.toFloat()
        if (surfaceX !in 0f..surfaceWidth || surfaceY !in 0f..surfaceHeight) return false

        val meteringPoint = SurfaceOrientedMeteringPointFactory(surfaceWidth, surfaceHeight)
            .createPoint(surfaceX, surfaceY)
        camera.cameraControl.startFocusAndMetering(FocusMeteringAction.Builder(meteringPoint).build())
        return true
    }

    fun updateTargetRotation(displayRotation: Int) {
        activePreview?.targetRotation = displayRotation
    }

    private fun bindPreview(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        lens: CameraLens,
        displayRotation: Int,
    ): Preview {
        val stabilizationSupported = Preview
            .getPreviewCapabilities(cameraProvider.getCameraInfo(lens.selector))
            .isStabilizationSupported
        val wantsStabilization = CameraSettings.usePreviewStabilizationWhenSupported && stabilizationSupported

        val preview = try {
            bindPreviewOnce(cameraProvider, lifecycleOwner, lens, displayRotation, wantsStabilization)
        } catch (unsupportedCombination: IllegalArgumentException) {
            if (!wantsStabilization) throw unsupportedCombination
            bindPreviewOnce(cameraProvider, lifecycleOwner, lens, displayRotation, stabilizationEnabled = false)
        }
        currentZoomRatio = boundCamera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 1f
        return preview
    }

    private fun bindPreviewOnce(
        cameraProvider: ProcessCameraProvider,
        lifecycleOwner: LifecycleOwner,
        lens: CameraLens,
        displayRotation: Int,
        stabilizationEnabled: Boolean,
    ): Preview {
        val preview = Preview.Builder()
            .setResolutionSelector(CameraSettings.previewResolutionSelector)
            .setTargetFrameRate(CameraSettings.targetFrameRateFps)
            .setPreviewStabilizationEnabled(stabilizationEnabled)
            .setTargetRotation(displayRotation)
            .build()
        preview.setSurfaceProvider { newSurfaceRequest -> mutableSurfaceRequest.value = newSurfaceRequest }
        boundCamera = cameraProvider.bindToLifecycle(lifecycleOwner, lens.selector, preview)
        activePreview = preview
        return preview
    }

    private fun ProcessCameraProvider.hasCameraSafely(lens: CameraLens): Boolean =
        runCatching { hasCamera(lens.selector) }.getOrDefault(false)
}
