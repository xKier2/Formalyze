// The two physical cameras the app can use. The back camera is the default because it has the better sensor.
package res.formalyze.camera

import androidx.camera.core.CameraSelector

enum class CameraLens(val selector: CameraSelector) {
    BACK(CameraSelector.DEFAULT_BACK_CAMERA),
    FRONT(CameraSelector.DEFAULT_FRONT_CAMERA);

    fun opposite(): CameraLens = if (this == BACK) FRONT else BACK
}
