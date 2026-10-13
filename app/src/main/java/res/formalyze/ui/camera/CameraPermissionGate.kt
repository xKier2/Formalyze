// Shows the camera content only after the user grants camera permission.
// Before asking, it explains why the camera is needed. If the user says no, it offers to ask again
// or to open the app's system settings, because Android stops showing the prompt after repeated refusals.
package res.formalyze.ui.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import res.formalyze.R

@Composable
fun CameraPermissionGate(
    modifier: Modifier = Modifier,
    cameraContent: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(isCameraPermissionGranted(context)) }
    var userDeclined by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        userDeclined = !granted
    }
    val appSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        permissionGranted = isCameraPermissionGranted(context)
    }

    if (permissionGranted) {
        cameraContent()
    } else {
        PermissionExplanation(
            userDeclined = userDeclined,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            onOpenAppSettings = { appSettingsLauncher.launch(appSettingsIntent(context)) },
            modifier = modifier,
        )
    }
}

@Composable
private fun PermissionExplanation(
    userDeclined: Boolean,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.camera_permission_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(
                    if (userDeclined) R.string.camera_permission_declined_message
                    else R.string.camera_permission_message
                ),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRequestPermission) {
                Text(
                    stringResource(
                        if (userDeclined) R.string.camera_permission_try_again
                        else R.string.camera_permission_allow
                    )
                )
            }
            if (userDeclined) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onOpenAppSettings) {
                    Text(stringResource(R.string.camera_permission_open_settings))
                }
            }
        }
    }
}

private fun isCameraPermissionGranted(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

private fun appSettingsIntent(context: Context): Intent =
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null),
    )
