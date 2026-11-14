package com.gymflow.feature.member.nutrition

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.gymflow.core.network.OpenFoodProduct
import com.gymflow.core.theme.GymFlowBlack
import com.gymflow.core.theme.GymFlowGreen
import com.gymflow.core.theme.GymFlowGreenLight
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.core.theme.GymFlowWhite
import com.gymflow.domain.model.AiFoodVisionResult
import java.util.concurrent.Executors

@Composable
fun NutritionScannerScreen(
    viewModel: NutritionScannerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Gallery image picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = loadBitmapFromUri(context, it)
            if (bitmap != null) {
                viewModel.onAnalyzePhoto(bitmap)
            }
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onClearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onClearMessages()
        }
    }

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Camera Preview or Permission Placeholder
            if (hasCameraPermission) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraExecutor = Executors.newSingleThreadExecutor()
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener(Runnable {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .build()
                            imageCapture = capture

                            val barcodeScanner = BarcodeScanning.getClient()
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && uiState.scanMode == ScanMode.BARCODE && !uiState.isAnalyzing) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    barcodeScanner.process(image)
                                        .addOnSuccessListener { barcodes ->
                                            for (barcode in barcodes) {
                                                val rawValue = barcode.rawValue
                                                if (!rawValue.isNullOrBlank()) {
                                                    viewModel.onBarcodeDetected(rawValue)
                                                    break
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    capture,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                Log.e("NutritionScanner", "Use case binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(GymFlowTheme.colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = null,
                            tint = GymFlowGreen,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Permission Required",
                            style = GymFlowTheme.typography.Heading1.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "GymFlow needs camera access to scan food barcodes and analyze meal photos with AI.",
                            style = GymFlowTheme.typography.BodyRegular.copy(color = GymFlowTheme.colors.textSecondary),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = GymFlowGreen)
                        ) {
                            Text("Grant Camera Access", color = GymFlowBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Top Control Bar Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GymFlowWhite
                        )
                    }

                    // Mode Switcher (Barcode vs AI Meal Vision)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(4.dp)
                    ) {
                        ScannerModePill(
                            title = "║▌ Barcode",
                            isSelected = uiState.scanMode == ScanMode.BARCODE,
                            onClick = { viewModel.onSwitchScanMode(ScanMode.BARCODE) }
                        )
                        ScannerModePill(
                            title = "📸 AI Meal",
                            isSelected = uiState.scanMode == ScanMode.AI_PHOTO,
                            onClick = { viewModel.onSwitchScanMode(ScanMode.AI_PHOTO) }
                        )
                    }

                    // Flashlight Toggle
                    IconButton(
                        onClick = { viewModel.onToggleFlash() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = if (uiState.isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flashlight",
                            tint = if (uiState.isFlashEnabled) GymFlowGreen else GymFlowWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Daily Calories Tracking Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = GymFlowGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.todayCalories} / ${uiState.todayMacros.targetCaloriesKcal} kcal today",
                            style = GymFlowTheme.typography.Caption.copy(color = GymFlowWhite, fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "P: ${uiState.todayMacros.totalProteinGrams.toInt()}g | C: ${uiState.todayMacros.totalCarbsGrams.toInt()}g | F: ${uiState.todayMacros.totalFatGrams.toInt()}g",
                        style = GymFlowTheme.typography.Caption.copy(color = Color.LightGray, fontSize = 11.sp)
                    )
                }
            }

            // Center Viewfinder Reticle (when in Barcode mode)
            if (uiState.scanMode == ScanMode.BARCODE && uiState.scannedProduct == null && !uiState.isAnalyzing) {
                Box(
                    modifier = Modifier
                        .size(260.dp, 160.dp)
                        .align(Alignment.Center)
                        .border(2.dp, GymFlowGreen, RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = "Align barcode inside frame",
                        style = GymFlowTheme.typography.Caption.copy(
                            color = GymFlowWhite,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }

            // Center Plate Guide (when in AI Photo mode)
            if (uiState.scanMode == ScanMode.AI_PHOTO && uiState.aiVisionResult == null && !uiState.isAnalyzing) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .align(Alignment.Center)
                        .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Center meal in circle",
                        style = GymFlowTheme.typography.Caption.copy(
                            color = GymFlowWhite,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Bottom Floating Controls (Shutter Button & Gallery Picker for AI Photo mode)
            if (uiState.scanMode == ScanMode.AI_PHOTO && uiState.aiVisionResult == null && !uiState.isAnalyzing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery Picker Button
                    IconButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Pick Photo",
                            tint = GymFlowWhite
                        )
                    }

                    // Main AI Snap Photo Shutter Button
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(GymFlowGreen)
                            .border(4.dp, Color.White, CircleShape)
                            .clickable {
                                imageCapture?.let { capture ->
                                    val executor = ContextCompat.getMainExecutor(context)
                                    capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: ImageProxy) {
                                            val bitmap = imageProxyToBitmap(image)
                                            image.close()
                                            if (bitmap != null) {
                                                viewModel.onAnalyzePhoto(bitmap)
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            Log.e("NutritionScanner", "Photo capture failed", exception)
                                        }
                                    })
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = "Capture Meal",
                            tint = GymFlowBlack,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Placeholder spacer for symmetrical balance
                    Spacer(modifier = Modifier.size(50.dp))
                }
            }

            // Loading / Analyzing Spinner Overlay
            if (uiState.isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GymFlowGreen, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.scanMode == ScanMode.BARCODE) "Looking up Open Food Facts..." else "AI Vision analyzing calories & macros...",
                            style = GymFlowTheme.typography.BodyMedium.copy(color = GymFlowWhite, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Scanned Product Result Overlay Card (Open Food Facts)
            AnimatedVisibility(
                visible = uiState.scannedProduct != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                uiState.scannedProduct?.let { product ->
                    ScannedProductBottomCard(
                        product = product,
                        servingGrams = uiState.servingWeightGrams,
                        selectedMealType = uiState.selectedMealType,
                        onGramsChange = { viewModel.onUpdateServingWeight(it) },
                        onMealTypeSelect = { viewModel.onSelectMealType(it) },
                        onDismiss = { viewModel.onDismissCard() },
                        onSave = { viewModel.onSaveScannedProduct() }
                    )
                }
            }

            // AI Vision Meal Result Overlay Card (Gemini Vision)
            AnimatedVisibility(
                visible = uiState.aiVisionResult != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                uiState.aiVisionResult?.let { vision ->
                    AiVisionMealBottomCard(
                        result = vision,
                        selectedMealType = uiState.selectedMealType,
                        onMealTypeSelect = { viewModel.onSelectMealType(it) },
                        onDismiss = { viewModel.onDismissCard() },
                        onSave = { viewModel.onSaveAiVisionMeal() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScannerModePill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) GymFlowGreen else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = GymFlowTheme.typography.Caption.copy(
                color = if (isSelected) GymFlowBlack else GymFlowWhite,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
private fun ScannedProductBottomCard(
    product: OpenFoodProduct,
    servingGrams: Float,
    selectedMealType: String,
    onGramsChange: (Float) -> Unit,
    onMealTypeSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val ratio = servingGrams / 100f
    val nutriments = product.nutriments
    val kcal = ((nutriments?.energyKcal100g ?: nutriments?.energyKcal ?: 0.0) * ratio).toInt()
    val protein = ((nutriments?.proteins100g ?: 0.0) * ratio).toFloat()
    val carbs = ((nutriments?.carbohydrates100g ?: 0.0) * ratio).toFloat()
    val fat = ((nutriments?.fat100g ?: 0.0) * ratio).toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(GymFlowTheme.colors.surface)
            .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "OPEN FOOD FACTS VERIFIED",
                    style = GymFlowTheme.typography.Caption.copy(
                        color = GymFlowGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Text(
                    text = product.productName ?: "Packaged Food Item",
                    style = GymFlowTheme.typography.Heading1.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2
                )
                if (!product.brands.isNullOrBlank()) {
                    Text(
                        text = product.brands,
                        style = GymFlowTheme.typography.Caption.copy(color = GymFlowTheme.colors.textSecondary)
                    )
                }
            }

            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = GymFlowTheme.colors.textSecondary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Serving size adjuster
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GymFlowTheme.colors.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Serving Weight:", style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onGramsChange(servingGrams - 25f) }, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = GymFlowTheme.colors.textPrimary)
                }
                Text(
                    text = "${servingGrams.toInt()} g",
                    style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                IconButton(onClick = { onGramsChange(servingGrams + 25f) }, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = GymFlowTheme.colors.textPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Macros Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MacroMetricTile(title = "Calories", value = "$kcal kcal", modifier = Modifier.weight(1f))
            MacroMetricTile(title = "Protein", value = "%.1fg".format(protein), modifier = Modifier.weight(1f))
            MacroMetricTile(title = "Carbs", value = "%.1fg".format(carbs), modifier = Modifier.weight(1f))
            MacroMetricTile(title = "Fat", value = "%.1fg".format(fat), modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Meal Type Selector
        Text(text = "Assign to Meal:", style = GymFlowTheme.typography.Caption.copy(color = GymFlowTheme.colors.textSecondary))
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK").forEach { type ->
                val isSelected = selectedMealType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GymFlowGreen else GymFlowTheme.colors.surfaceVariant)
                        .clickable { onMealTypeSelect(type) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.take(4),
                        style = GymFlowTheme.typography.Caption.copy(
                            color = if (isSelected) GymFlowBlack else GymFlowTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GymFlowGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = GymFlowBlack)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "LOG TO DIARY ($kcal KCAL)", color = GymFlowBlack, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AiVisionMealBottomCard(
    result: AiFoodVisionResult,
    selectedMealType: String,
    onMealTypeSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(GymFlowTheme.colors.surface)
            .border(1.dp, GymFlowTheme.colors.border, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI VISION DETECTED MEAL",
                    style = GymFlowTheme.typography.Caption.copy(
                        color = GymFlowGreen,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                )
                Text(
                    text = result.mealTitle,
                    style = GymFlowTheme.typography.Heading1.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = GymFlowTheme.colors.textSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Detected Food Items breakdown
        Text(text = "Detected Items:", style = GymFlowTheme.typography.Caption.copy(color = GymFlowTheme.colors.textSecondary))
        Spacer(modifier = Modifier.height(6.dp))
        result.items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "• ${item.name} (${item.estimatedGrams.toInt()}g)", style = GymFlowTheme.typography.Caption)
                Text(
                    text = "${item.caloriesKcal} kcal | P: ${item.proteinGrams.toInt()}g",
                    style = GymFlowTheme.typography.Caption.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Total Macros Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MacroMetricTile(title = "Total Kcal", value = "${result.totalCaloriesKcal}", modifier = Modifier.weight(1f))
            MacroMetricTile(title = "Protein", value = "%.0fg".format(result.totalProteinGrams), modifier = Modifier.weight(1f))
            MacroMetricTile(title = "Carbs", value = "%.0fg".format(result.totalCarbsGrams), modifier = Modifier.weight(1f))
            MacroMetricTile(title = "Fat", value = "%.0fg".format(result.totalFatGrams), modifier = Modifier.weight(1f))
        }

        if (result.healthAdvice.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GymFlowGreen.copy(alpha = 0.1f))
                    .padding(10.dp)
            ) {
                Text(
                    text = "💡 ${result.healthAdvice}",
                    style = GymFlowTheme.typography.Caption.copy(color = GymFlowTheme.colors.textSecondary, lineHeight = 16.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Meal Type Selector
        Text(text = "Assign to Meal:", style = GymFlowTheme.typography.Caption.copy(color = GymFlowTheme.colors.textSecondary))
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK").forEach { type ->
                val isSelected = selectedMealType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GymFlowGreen else GymFlowTheme.colors.surfaceVariant)
                        .clickable { onMealTypeSelect(type) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.take(4),
                        style = GymFlowTheme.typography.Caption.copy(
                            color = if (isSelected) GymFlowBlack else GymFlowTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GymFlowGreen),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = GymFlowBlack)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "LOG MEAL (${result.totalCaloriesKcal} KCAL)", color = GymFlowBlack, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MacroMetricTile(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GymFlowTheme.colors.surfaceVariant)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = GymFlowTheme.typography.Caption.copy(color = GymFlowTheme.colors.textSecondary, fontSize = 10.sp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = GymFlowTheme.typography.BodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp))
        }
    }
}

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        Log.e("NutritionScanner", "Failed to load bitmap from uri", e)
        null
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
    val planeProxy = image.planes[0]
    val buffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
