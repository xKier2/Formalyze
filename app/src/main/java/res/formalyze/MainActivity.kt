// App entry point. For now it opens straight into the live camera, after the permission check.
package res.formalyze

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import res.formalyze.ui.camera.CameraPermissionGate
import res.formalyze.ui.camera.CameraScreen
import res.formalyze.ui.theme.FormalyzeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FormalyzeTheme {
                CameraPermissionGate(modifier = Modifier.fillMaxSize()) {
                    CameraScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
