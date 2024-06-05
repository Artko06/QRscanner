package com.example.qr_scaner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.qr_scaner.ui.theme.QRscanerTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val scannedCode = mutableStateOf("Тут должен быть QR-code :)")
    private val showDialog = mutableStateOf(false)
    private val imageBitmap = mutableStateOf<Bitmap?>(null)

    @OptIn(DelicateCoroutinesApi::class)
    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if(result.contents != null){
            scannedCode.value = result.contents

            Toast.makeText(this, "Scan data: ${scannedCode.value}",
                Toast.LENGTH_SHORT).show()

            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val file = HttpClient.send(this@MainActivity, "http://185.158.216.63:5050",
                        scannedCode.value)
                    Log.d("HttpClient", "Request sent to server, response received.")
                    withContext(Dispatchers.Main) {
                        val bitmap = BitmapFactory.decodeFile(file?.absolutePath)
                        imageBitmap.value = bitmap
                        showDialog.value = true
                    }
                } catch (e: Exception) {
                    Log.d("HttpClient", "Error sending request to server: ${e.message}")
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRscanerTheme {
                imageBitmap.value?.let {
                    if (showDialog.value) {
                        Dialog(onDismissRequest = { showDialog.value = false }) {
                            Image(bitmap = it.asImageBitmap(), contentDescription = "Scanned Image",
                                modifier = Modifier.fillMaxSize())
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center)
                {
                    Button(onClick = { scan() },
                        modifier = Modifier.size(225.dp)) {
                        Text(text = "Scanning a QR-Code", style = TextStyle(fontSize = 28.sp),
                            textAlign = TextAlign.Center)
                    }
                }
                TextField(value = scannedCode.value, onValueChange = { scannedCode.value = it },
                    modifier = Modifier.fillMaxWidth())
            }
        }
    }

    private fun scan() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
       scanLauncher.launch(options)
    }
}
