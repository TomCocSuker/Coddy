package com.example.coddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coddy.ui.theme.CoddyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoddyTheme(dynamicColor = false) {
                CoddyApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoddyApp(vm: MainViewModel = viewModel()) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on status change
    LaunchedEffect(vm.statusMessage) {
        vm.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Coddy",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        vm.reset()
                    },
                    text = { Text(stringResource(R.string.tab_zero_width)) },
                    icon = { Icon(Icons.Default.TextFormat, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        vm.reset()
                    },
                    text = { Text(stringResource(R.string.tab_crypto)) },
                    icon = { Icon(Icons.Default.VpnKey, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        vm.reset()
                    },
                    text = { Text(stringResource(R.string.tab_encode)) },
                    icon = { Icon(Icons.Default.Lock, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        vm.reset()
                    },
                    text = { Text(stringResource(R.string.tab_pic_as_file)) },
                    icon = { Icon(Icons.Default.PhotoCamera, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = {
                        selectedTab = 4
                        vm.reset()
                    },
                    text = { Text(stringResource(R.string.tab_hybrid)) },
                    icon = { Icon(Icons.Default.Layers, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 5,
                    onClick = {
                        selectedTab = 5
                        vm.reset()
                    },
                    text = { Text(stringResource(R.string.tab_convert)) },
                    icon = { Icon(Icons.Default.MovieCreation, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 6,
                    onClick = {
                        selectedTab = 6
                        vm.reset()
                    },
                    text = { Text(stringResource(R.string.tab_merge)) },
                    icon = { Icon(Icons.Default.Merge, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> ZeroWidthScreen(vm)
                1 -> CryptoScreen(vm)
                2 -> EncodeScreen(vm)
                3 -> PicAsFileScreen(vm)
                4 -> HybridScreen(vm)
                5 -> MediaConverterScreen(vm)
                6 -> MergeScreen(vm)
            }
        }
    }
}

// ───────────────────────── ENCODE SCREEN ─────────────────────────

@Composable
fun EncodeScreen(vm: MainViewModel) {
    val clipboardManager = LocalClipboardManager.current
    var messageText by rememberSaveable { mutableStateOf("") }
    var isEncodeMode by rememberSaveable { mutableStateOf(true) }

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        vm.onImageSelected(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encode / Decode toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { isEncodeMode = true; vm.reset() },
                modifier = Modifier.weight(1f),
                enabled = !isEncodeMode
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.tab_encode))
            }
            FilledTonalButton(
                onClick = { isEncodeMode = false; vm.reset() },
                modifier = Modifier.weight(1f),
                enabled = isEncodeMode
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.tab_decode))
            }
        }

        // Pick image button
        FilledTonalButton(
            onClick = {
                pickMedia.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.pick_image))
        }

        // Image preview
        val bmp = if (isEncodeMode) (vm.resultBitmap ?: vm.selectedBitmap) else vm.selectedBitmap
        if (bmp != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }

            if (isEncodeMode && vm.resultBitmap == null) {
                Text(
                    text = stringResource(R.string.capacity_format, vm.capacityBytes),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.no_image_selected),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Progress
        AnimatedVisibility(
            visible = vm.isProcessing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }

        if (isEncodeMode) {
            // ── ENCODE MODE ──
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text(stringResource(R.string.enter_secret_message)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8,
                enabled = !vm.isProcessing
            )

            if (!vm.isProcessing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = { vm.encode(messageText) },
                        modifier = Modifier.weight(1f),
                        enabled = vm.selectedBitmap != null && messageText.isNotBlank()
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.encode))
                    }

                    FilledTonalButton(
                        onClick = { vm.saveToGallery() },
                        modifier = Modifier.weight(1f),
                        enabled = vm.resultBitmap != null
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.save_to_gallery))
                    }
                }
            }
        } else {
            // ── DECODE MODE ──
            if (!vm.isProcessing) {
                FilledTonalButton(
                    onClick = { vm.decode() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = vm.selectedBitmap != null
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.decode))
                }
            }

            vm.decodedText?.let { text ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.decoded_message),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(text))
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.copy),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionContainer {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ───────────────────────── MERGE SCREEN ─────────────────────────

@Composable
fun MergeScreen(vm: MainViewModel) {
    val pickContainer = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> vm.onImageSelected(uri) }

    val pickSecret = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> vm.onSecretImageSelected(uri) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Pick Container ──
        FilledTonalButton(
            onClick = {
                pickContainer.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.pick_container))
        }

        // Container preview
        vm.selectedBitmap?.let { bmp ->
            Text(
                text = stringResource(R.string.container_label) + " (${bmp.width}×${bmp.height})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Container",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // ── Pick Secret ──
        FilledTonalButton(
            onClick = {
                pickSecret.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.pick_secret_image))
        }

        // Secret preview
        vm.secretBitmap?.let { bmp ->
            Text(
                text = stringResource(R.string.secret_label) + " (${bmp.width}×${bmp.height})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Secret",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Progress
        AnimatedVisibility(
            visible = vm.isProcessing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }

        // Action buttons
        if (!vm.isProcessing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Merge button
                FilledTonalButton(
                    onClick = { vm.mergeImages() },
                    modifier = Modifier.weight(1f),
                    enabled = vm.selectedBitmap != null && vm.secretBitmap != null
                ) {
                    Icon(Icons.Default.Merge, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.merge_images))
                }

                // Extract button
                FilledTonalButton(
                    onClick = { vm.extractImage() },
                    modifier = Modifier.weight(1f),
                    enabled = vm.selectedBitmap != null
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.extract_image))
                }
            }
        }

        // Result preview
        vm.resultBitmap?.let { bmp ->
            Text(
                text = stringResource(R.string.result_label),
                style = MaterialTheme.typography.titleLarge
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Result",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }

            // Save button
            FilledTonalButton(
                onClick = { vm.saveToGallery() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.save_to_gallery))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ───────────────────── PIC AS FILE SCREEN ─────────────────────

@Composable
fun PicAsFileScreen(vm: MainViewModel) {
    val pickContainer = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> vm.onImageSelected(uri) }

    val pickSecret = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> vm.onSecretImageSelected(uri) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Pick Container ──
        FilledTonalButton(
            onClick = {
                pickContainer.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.pick_container))
        }

        // Container preview + capacity
        vm.selectedBitmap?.let { bmp ->
            Text(
                text = stringResource(R.string.container_label) + " (${bmp.width}×${bmp.height})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Container",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = stringResource(R.string.capacity_kb_format, vm.capacityBytes / 1024),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── Pick Secret Picture ──
        FilledTonalButton(
            onClick = {
                pickSecret.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.pick_secret_image))
        }

        // Secret preview
        vm.secretBitmap?.let { bmp ->
            Text(
                text = stringResource(R.string.secret_label) + " (${bmp.width}×${bmp.height})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Secret",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Progress
        AnimatedVisibility(
            visible = vm.isProcessing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }

        // Action buttons
        if (!vm.isProcessing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hide picture
                FilledTonalButton(
                    onClick = { vm.encodePicture() },
                    modifier = Modifier.weight(1f),
                    enabled = vm.selectedBitmap != null && vm.secretBitmap != null
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.hide_picture))
                }

                // Reveal picture
                FilledTonalButton(
                    onClick = { vm.decodePicture() },
                    modifier = Modifier.weight(1f),
                    enabled = vm.selectedBitmap != null
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.reveal_picture))
                }
            }
        }

        // Result preview
        val resultBmp = vm.decodedBitmap ?: vm.resultBitmap
        resultBmp?.let { bmp ->
            Text(
                text = stringResource(R.string.result_label),
                style = MaterialTheme.typography.titleLarge
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Result",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }

            // Save button
            FilledTonalButton(
                onClick = { vm.saveToGallery() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.save_to_gallery))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─────────────────── ZERO-WIDTH SCREEN ───────────────────

@Composable
fun ZeroWidthScreen(vm: MainViewModel) {
    val clipboardManager = LocalClipboardManager.current
    var coverText by rememberSaveable { mutableStateOf("") }
    var secretText by rememberSaveable { mutableStateOf("") }
    var decodeInput by rememberSaveable { mutableStateOf("") }
    var isEncodeMode by rememberSaveable { mutableStateOf(true) }

    // Count invisible chars in decode input (math operator chars)
    val hiddenCount = decodeInput.count {
        it == '\u2060' || it == '\u2062' || it == '\u2063' || it == '\u2064'
    }
    val visibleText = decodeInput.filter { char ->
        // Keep only printable characters: filter out Format (Cf), Control (Cc),
        // replacement char (U+FFFD), and other invisible categories
        char.category != CharCategory.FORMAT &&
        char.category != CharCategory.CONTROL &&
        char.category != CharCategory.SURROGATE &&
        char.category != CharCategory.PRIVATE_USE &&
        char.category != CharCategory.UNASSIGNED &&
        char != '\uFFFD' && char != '\uFEFF'
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = { isEncodeMode = true },
                modifier = Modifier.weight(1f),
                enabled = !isEncodeMode
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.zw_encode))
            }
            FilledTonalButton(
                onClick = { isEncodeMode = false },
                modifier = Modifier.weight(1f),
                enabled = isEncodeMode
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.zw_decode))
            }
        }

        if (isEncodeMode) {
            // ── Encode mode ──
            OutlinedTextField(
                value = coverText,
                onValueChange = { coverText = it },
                label = { Text(stringResource(R.string.enter_cover_text)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5
            )

            OutlinedTextField(
                value = secretText,
                onValueChange = { secretText = it },
                label = { Text(stringResource(R.string.enter_secret_text)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5
            )

            FilledTonalButton(
                onClick = { vm.encodeZeroWidth(coverText, secretText) },
                modifier = Modifier.fillMaxWidth(),
                enabled = coverText.isNotBlank() && secretText.isNotBlank()
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.zw_encode))
            }

            // Result
            vm.zwResultText?.let { result ->
                val stripped = ZeroWidthEngine.stripInvisible(result)
                val hiddenChars = result.length - stripped.length

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.result_text_label),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Show only visible text — no question marks
                        SelectionContainer {
                            Text(
                                text = stripped,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ $hiddenChars invisible chars embedded",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }

                // Copy button — copies the FULL text with invisible characters
                FilledTonalButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(result))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.copy))
                }
            }
        } else {
            // ── Decode mode ──
            // Paste button reads clipboard without rendering zero-width chars
            FilledTonalButton(
                onClick = {
                    val clip = clipboardManager.getText()
                    if (clip != null) {
                        decodeInput = clip.text
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.paste_from_clipboard))
            }

            // Show paste stats — never render the raw text to avoid question marks
            if (decodeInput.isNotEmpty()) {
                val totalChars = decodeInput.length
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hiddenCount > 0)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (hiddenCount > 0) {
                            Text(
                                text = "✓ Text pasted: $totalChars chars total, $hiddenCount hidden",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Text(
                                text = "⚠ Pasted $totalChars chars, but no hidden message detected",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            FilledTonalButton(
                onClick = { vm.decodeZeroWidth(decodeInput) },
                modifier = Modifier.fillMaxWidth(),
                enabled = decodeInput.isNotBlank()
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.zw_decode))
            }

            // Decoded result
            vm.zwDecodedText?.let { decoded ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.decoded_message),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(decoded))
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.copy),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionContainer {
                            Text(
                                text = decoded,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ───────────────────── HYBRID SCREEN ─────────────────────

@Composable
fun HybridScreen(vm: MainViewModel) {
    var useChunkMode by rememberSaveable { mutableStateOf(false) }
    var saveExtension by rememberSaveable { mutableStateOf("png") }

    val pickCover = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> vm.onCoverFilePicked(uri) }

    val pickSecret = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> vm.onSecretFilePicked(uri) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode toggle: Concat vs PNG Chunk
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = { useChunkMode = false },
                modifier = Modifier.weight(1f),
                enabled = useChunkMode
            ) {
                Text(stringResource(R.string.mode_concat))
            }
            FilledTonalButton(
                onClick = { useChunkMode = true },
                modifier = Modifier.weight(1f),
                enabled = !useChunkMode
            ) {
                Text(stringResource(R.string.mode_png_chunk))
            }
        }

        // Pick cover image
        FilledTonalButton(
            onClick = {
                pickCover.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.pick_cover_file))
        }

        // Cover info
        vm.coverBytes?.let { bytes ->
            Text(
                text = stringResource(R.string.cover_format_label, vm.coverFormat, bytes.size / 1024),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Pick secret file
        FilledTonalButton(
            onClick = { pickSecret.launch("*/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AttachFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.pick_secret_file))
        }

        // Secret info
        vm.secretFileBytes?.let { bytes ->
            Text(
                text = stringResource(R.string.secret_file_label, bytes.size / 1024),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Progress
        AnimatedVisibility(
            visible = vm.isProcessing,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }

        // Action buttons
        if (!vm.isProcessing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = { vm.hybridize(useChunkMode) },
                    modifier = Modifier.weight(1f),
                    enabled = vm.coverBytes != null && vm.secretFileBytes != null
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.hybridize))
                }

                FilledTonalButton(
                    onClick = { vm.extractHybrid(useChunkMode) },
                    modifier = Modifier.weight(1f),
                    enabled = vm.coverBytes != null
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.extract_file))
                }
            }
        }

        // Result + save
        vm.hybridResult?.let { result ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.result_label) + " (${result.size / 1024} KB)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Save extension input + button
            OutlinedTextField(
                value = saveExtension,
                onValueChange = { saveExtension = it },
                label = { Text("File extension") },
                modifier = Modifier.fillMaxWidth()
            )

            FilledTonalButton(
                onClick = { vm.saveHybridToDownloads(saveExtension) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.saved_to_downloads))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}


// ──────────────────── MEDIA CONVERTER SCREEN ────────────────────

@Composable
fun MediaConverterScreen(vm: MainViewModel) {
    var outputFormat by rememberSaveable { mutableStateOf("mp4") }
    var videoMime by rememberSaveable { mutableStateOf("video/avc") }
    var audioMime by rememberSaveable { mutableStateOf("audio/mp4a-latm") }
    var width by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var videoBitrate by rememberSaveable { mutableStateOf("") }
    var imageWidth by rememberSaveable { mutableStateOf("") }
    var imageHeight by rememberSaveable { mutableStateOf("") }
    var imageQuality by rememberSaveable { mutableStateOf("85") }
    var removeAudio by rememberSaveable { mutableStateOf(false) }
    var mode by rememberSaveable { mutableStateOf(0) } // 0=video, 1=image, 2=audio

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> vm.onMediaFilePicked(uri, isImage = mode == 1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mode selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("🎬 Video" to 0, "🖼 Photo" to 1, "🎵 Audio" to 2).forEach { (label, idx) ->
                FilledTonalButton(
                    onClick = { mode = idx },
                    modifier = Modifier.weight(1f),
                    enabled = mode != idx
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Pick file
        FilledTonalButton(
            onClick = {
                val mime = if (mode == 1) "image/*" else "video/*"
                pickMedia.launch(mime)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AttachFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.pick_media_file))
        }

        // Media info
        vm.mediaInfo?.let { info ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Format: ${info.format}", style = MaterialTheme.typography.labelMedium)
                    Text("Resolution: ${info.resolution}", style = MaterialTheme.typography.labelMedium)
                    Text("Codec: ${info.codec}", style = MaterialTheme.typography.labelMedium)
                    if (info.duration != "N/A")
                        Text("Duration: ${info.duration}s", style = MaterialTheme.typography.labelMedium)
                    if (info.bitrate != "N/A")
                        Text("Bitrate: ${info.bitrate}", style = MaterialTheme.typography.labelMedium)
                    Text("Size: ${info.fileSize / 1024} KB", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        when (mode) {
            0 -> {
                // ── Video transcoding ──
                OutlinedTextField(
                    value = outputFormat, onValueChange = { outputFormat = it },
                    label = { Text("Output (mp4, webm)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Video Codec:", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("H.264" to "video/avc", "H.265" to "video/hevc").forEach { (label, mime) ->
                        FilledTonalButton(
                            onClick = { 
                                videoMime = mime
                                outputFormat = "mp4"
                            },
                            modifier = Modifier.weight(1f),
                            enabled = videoMime != mime
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Text("Audio Codec:", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("AAC" to "audio/mp4a-latm", "Opus" to "audio/opus").forEach { (label, mime) ->
                        FilledTonalButton(
                            onClick = { audioMime = mime },
                            modifier = Modifier.weight(1f),
                            enabled = audioMime != mime
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = width, onValueChange = { width = it },
                        label = { Text("Width") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = height, onValueChange = { height = it },
                        label = { Text("Height") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = videoBitrate, onValueChange = { videoBitrate = it },
                        label = { Text("Bitrate KB") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = removeAudio, onCheckedChange = { removeAudio = it })
                    Text("Remove audio track")
                }
            }
            1 -> {
                // ── Image conversion ──
                OutlinedTextField(
                    value = outputFormat, onValueChange = { outputFormat = it },
                    label = { Text("Output (jpg, png, webp)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = imageWidth, onValueChange = { imageWidth = it },
                        label = { Text("Width") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = imageHeight, onValueChange = { imageHeight = it },
                        label = { Text("Height") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = imageQuality, onValueChange = { imageQuality = it },
                        label = { Text("Quality") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            2 -> {
                // ── Audio extraction ──
                OutlinedTextField(
                    value = outputFormat, onValueChange = { outputFormat = it },
                    label = { Text("Output (m4a, mp3)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Extracts audio track from video as-is (no re-encoding)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Progress
        AnimatedVisibility(visible = vm.isProcessing, enter = fadeIn(), exit = fadeOut()) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }

        // Action button
        if (!vm.isProcessing) {
            FilledTonalButton(
                onClick = {
                    vm.convertMedia(
                        outputExtension = outputFormat.ifBlank { "mp4" },
                        videoMime = videoMime,
                        audioMime = audioMime,
                        width = width.toIntOrNull() ?: 0,
                        height = height.toIntOrNull() ?: 0,
                        videoBitrateKbps = videoBitrate.toIntOrNull() ?: 0,
                        imageWidth = imageWidth.toIntOrNull(),
                        imageHeight = imageHeight.toIntOrNull(),
                        imageQuality = imageQuality.toIntOrNull() ?: 85,
                        isImage = mode == 1,
                        extractAudioOnly = mode == 2,
                        removeAudio = removeAudio
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = vm.inputMediaUri != null
            ) {
                Icon(
                    when (mode) {
                        1 -> Icons.Default.Image
                        2 -> Icons.Default.MusicNote
                        else -> Icons.Default.MovieCreation
                    },
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.convert))
            }
        }

        // Done indicator
        if (vm.conversionDone) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✅ ${vm.statusMessage}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ───────────────────────── CRYPTO SCREEN ─────────────────────────

@Composable
fun CryptoScreen(vm: MainViewModel) {
    val clipboardManager = LocalClipboardManager.current
    var textInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Password
        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text(stringResource(R.string.crypto_password)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Text input
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text(stringResource(R.string.crypto_text)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { vm.encryptCrypto(textInput, passwordInput) },
                modifier = Modifier.weight(1f),
                enabled = !vm.isProcessing
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.crypto_encrypt))
            }
            FilledTonalButton(
                onClick = { vm.decryptCrypto(textInput, passwordInput) },
                modifier = Modifier.weight(1f),
                enabled = !vm.isProcessing
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.crypto_decrypt))
            }
        }

        // Progress
        AnimatedVisibility(visible = vm.isProcessing) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }

        // Result text
        if (vm.cryptoResultText != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.crypto_result),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    SelectionContainer {
                        Text(
                            text = vm.cryptoResultText ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = {
                            clipboardManager.setText(
                                androidx.compose.ui.text.AnnotatedString(vm.cryptoResultText ?: "")
                            )
                            vm.showStatusMessage("Copied to clipboard")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.copy))
                    }
                }
            }
        }

        // Status Message
        if (vm.statusMessage != null && vm.statusMessage != "Copied to clipboard") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✅ ${vm.statusMessage}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

