package com.github.tedblair2.muziki3.helpers

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.tedblair2.muziki3.features.home.ui.AlertDialogExample

@Composable
fun PermissionWrapper(
    modifier: Modifier=Modifier,
    onPermissionGranted: () -> Unit={},
    onPermissionDenied: () -> Unit={},
    content: @Composable () -> Unit,
) {
    val context= LocalContext.current
    val activity=context.getActivity()
    var showDialog by remember {
        mutableStateOf(false)
    }
    val readAudioPermission= remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
    }

    val permissionLauncher= rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted->
        if (isGranted) onPermissionGranted() else onPermissionDenied()
    }

    LaunchedEffect(key1 = true) {
        val notGranted=
            ContextCompat.checkSelfPermission(context,readAudioPermission)!= PackageManager.PERMISSION_GRANTED
        if (notGranted){
            val showRational= ActivityCompat.shouldShowRequestPermissionRationale(activity!!,readAudioPermission)
            if (showRational){
                showDialog=true
            }else{
                permissionLauncher.launch(readAudioPermission)
            }
        }else{
            onPermissionGranted()
        }
    }

    Box(modifier = modifier){
        content()
        if (showDialog) {
            AlertDialogExample(
                onDismissRequest ={
                    showDialog=false
                    onPermissionDenied()
                } ,
                onConfirmation = {
                    permissionLauncher.launch(readAudioPermission)
                    showDialog = false
                } ,
                dialogTitle =  "App Permissions",
                dialogText =  "In order to ensure a smooth experience while using this application, please grant the asked permissions.",
                icon = Icons.Default.Info
            )
        }
    }
}