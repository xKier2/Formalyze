// Central place for camera quality choices, so tuning never means hunting through the code.
// The preview asks for the sharpest 16:9 picture the phone offers at a steady 30 fps.
// Fit scaling shows the whole camera frame, so what the user sees is what the camera captures.
package res.formalyze.camera

import android.util.Range
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.compose.ui.layout.ContentScale

object CameraSettings {
    val previewResolutionSelector: ResolutionSelector = ResolutionSelector.Builder()
        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
        .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
        .build()

    val targetFrameRateFps: Range<Int> = Range(15, 30)

    const val usePreviewStabilizationWhenSupported = true

    val previewContentScale: ContentScale = ContentScale.Fit

    val defaultLens: CameraLens = CameraLens.BACK

    const val focusIndicatorVisibleMillis = 1_000L
}
