package com.biniam.foodscanner


import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap



@Composable
fun FoodScannerScreen(
    foodScannerViewModel: FoodScannerViewModel = viewModel()
) {
    var image by remember { mutableStateOf<Bitmap?>(null) }
    val placeholderResult = stringResource(R.string.results_placeholder)
    var conditions by rememberSaveable { mutableStateOf<String>("") }
    var result by rememberSaveable { mutableStateOf(placeholderResult) }
    val uiState by foodScannerViewModel.uiState.collectAsState()
    val halfPrompt = R.string.half_prompt
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val capturedImage = result.data?.extras?.get("data") as? Bitmap
            if (capturedImage != null) {
                image = capturedImage
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.app_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        image?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = stringResource(R.string.camera_description),
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .requiredSize(200.dp)
                    .clickable {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        launcher.launch(intent)
                    }
                    .align(Alignment.CenterHorizontally)

            )
        } ?: run {
            Image(
                painter = painterResource(R.drawable.camera_icon),
                contentDescription = stringResource(R.string.camera_description),
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .requiredSize(400.dp)
                    .clickable {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        launcher.launch(intent)
                    }
                    .align(Alignment.CenterHorizontally)
            )
        }


        Row(
            modifier = Modifier.padding(all = 16.dp)
        ) {
            TextField(
                value = conditions,
                label = { Text(stringResource(R.string.enter_conditions)) },
                onValueChange = { conditions = it },
                modifier = Modifier
                    .weight(0.8f)
                    .padding(end = 16.dp)
                    .align(Alignment.CenterVertically)
            )

            val prompt = "$halfPrompt $conditions"

            Button(
                onClick = {
                    image?.let {
                        foodScannerViewModel.sendPrompt(it, prompt)
                    }
                },
                enabled = conditions != "" && image != null,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(text = stringResource(R.string.scan_action))
            }
        }

        if (uiState is UiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            var textColor = MaterialTheme.colorScheme.onSurface
            if (uiState is UiState.Error) {
                textColor = MaterialTheme.colorScheme.error
                result = (uiState as UiState.Error).errorMessage
            } else if (uiState is UiState.Success) {
                textColor = MaterialTheme.colorScheme.onSurface
                result = (uiState as UiState.Success).outputText
            }
            val scrollState = rememberScrollState()
            Text(
                text = result,
                textAlign = TextAlign.Start,
                color = textColor,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            )
        }
    }
}