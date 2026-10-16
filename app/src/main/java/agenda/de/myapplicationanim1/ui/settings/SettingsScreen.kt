package agenda.de.myapplicationanim1.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import android.widget.Toast
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.rounded.Feedback
import agenda.de.myapplicationanim1.data.util.BackupManager
import agenda.de.myapplicationanim1.update.UpdateCheckResult
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.OutlinedButton
import agenda.de.myapplicationanim1.update.UpdateDialog
import agenda.de.myapplicationanim1.update.UpdateInfo
import agenda.de.myapplicationanim1.update.UpdateManager
import kotlinx.coroutines.launch
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import agenda.de.myapplicationanim1.ui.theme.ChewyFontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import agenda.de.myapplicationanim1.R
import agenda.de.myapplicationanim1.data.util.NotifiedAnime
import agenda.de.myapplicationanim1.data.util.SystemThemeMode
import agenda.de.myapplicationanim1.ui.components.ApiSourceFooter
import agenda.de.myapplicationanim1.ui.util.rememberNotificationPermissionState
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: AppThemeMode = AppThemeMode.BLACK,
    onThemeSelected: (AppThemeMode) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel? = null,
    onAnimeClick: (Int) -> Unit = {}
) {
    val permissionState = rememberNotificationPermissionState()
    val notifiedAnimes by (viewModel?.notifiedAnimes?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    var customHexInput by remember(viewModel?.backgroundColorHex) {
        mutableStateOf(viewModel?.backgroundColorHex ?: "#0B0E14")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "AG Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = "Configurações",
                            fontFamily = ChewyFontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Notificações Globais
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notificações Globais",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ativar ou desativar todos os alertas de episódios",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = viewModel?.isGlobalNotificationsEnabled ?: true,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    permissionState.requestPermissions {
                                        viewModel?.toggleGlobalNotifications(true)
                                    }
                                } else {
                                    viewModel?.toggleGlobalNotifications(false)
                                }
                            }
                        )
                    }
                }
            }

            // Atualização do Aplicativo (GitHub Releases)
            item {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
                var showUpdateDialog by remember { mutableStateOf(false) }
                var isChecking by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SystemUpdate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Atualização do App",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Versão atual: ${remember {
                                    try {
                                        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
                                    } catch (e: Exception) { "1.0" }
                                }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (!isChecking) {
                                    isChecking = true
                                    scope.launch {
                                        val manager = UpdateManager(context, "EvandroJr13", "Anime_Schedule")
                                        val result = manager.checkForUpdate()
                                        isChecking = false
                                        when (result) {
                                            is UpdateCheckResult.NewUpdate -> {
                                                updateInfo = result.info
                                                showUpdateDialog = true
                                            }
                                            is UpdateCheckResult.AlreadyLatest -> {
                                                Toast.makeText(
                                                    context,
                                                    "Seu aplicativo já está na versão mais recente!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            is UpdateCheckResult.Error -> {
                                                Toast.makeText(
                                                    context,
                                                    result.message,
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isChecking
                        ) {
                            Text(if (isChecking) "Verificando..." else "Verificar")
                        }
                    }
                }

                if (showUpdateDialog && updateInfo != null) {
                    UpdateDialog(
                        updateInfo = updateInfo!!,
                        onDismiss = { showUpdateDialog = false },
                        onConfirm = {
                            showUpdateDialog = false
                            val manager = UpdateManager(context, "EvandroJr13", "Anime_Schedule")
                            manager.downloadAndInstall(updateInfo!!.apkUrl)
                        }
                    )
                }
            }

            // Firebase Tester Feedback Button
            item {
                val context = LocalContext.current

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Feedback,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Feedback do Testador",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Enviar captura de tela e sugestão no Firebase",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                try {
                                    com.google.firebase.appdistribution.FirebaseAppDistribution.getInstance()
                                        .startFeedback("Feedback do Anime Schedule")
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Disponível para testadores no Firebase App Distribution.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        ) {
                            Text("Enviar")
                        }
                    }
                }
            }

            // Backup & Restore Data
            item {
                val context = LocalContext.current
                val backupManager = remember { BackupManager(context) }
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Backup e Restauração de Dados",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Exporte ou importe seus animes e episódios favoritos para nunca perder seus dados.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val json = backupManager.exportBackupJson()
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(json))
                                    Toast.makeText(context, "Backup copiado para a área de transferência!", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Exportar Backup")
                            }
                            OutlinedButton(
                                onClick = {
                                    val clip = clipboardManager.getText()?.text
                                    if (!clip.isNullOrBlank()) {
                                        val success = backupManager.importBackupJson(clip)
                                        if (success) {
                                            Toast.makeText(context, "Backup restaurado com sucesso!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Erro: texto de backup inválido.", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Área de transferência vazia.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Restaurar Backup")
                            }
                        }
                    }
                }
            }

            // 2. Modo do Tema (Sistema / Dark / Light)
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Modo do Tema",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SystemThemeMode.entries.forEach { mode ->
                            val selected = viewModel?.systemThemeMode == mode
                            Card(
                                onClick = { viewModel?.updateSystemThemeMode(mode) },
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { viewModel?.updateSystemThemeMode(mode) }
                                    )
                                    Text(
                                        text = mode.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Estilo e Cor de Fundo
            item {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Estilo e Cor de Fundo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Selecione o tema de cores e a cor de fundo do aplicativo:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(AppThemeMode.entries) { mode ->
                val activeTheme = viewModel?.themeMode ?: currentTheme
                val selected = mode == activeTheme
                Card(
                    onClick = {
                        viewModel?.updateThemeMode(mode) ?: onThemeSelected(mode)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = {
                                viewModel?.updateThemeMode(mode) ?: onThemeSelected(mode)
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        // Color Preview Circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    agenda.de.myapplicationanim1.ui.theme.parseHexColor(
                                        mode.defaultBgHex,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Cor de Fundo: ${mode.defaultBgHex}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Custom Background Color Input (HEX & RGB)
            item {
                val initialRgb = remember(viewModel?.backgroundColorHex) {
                    try {
                        val hex = viewModel?.backgroundColorHex ?: "#0B0E14"
                        val cleaned = hex.removePrefix("#")
                        Triple(
                            cleaned.substring(0, 2).toInt(16),
                            cleaned.substring(2, 4).toInt(16),
                            cleaned.substring(4, 6).toInt(16)
                        )
                    } catch (e: Exception) {
                        Triple(11, 14, 20)
                    }
                }

                var colorInputMode by remember { mutableStateOf("HEX") }
                var redInput by remember(initialRgb) { mutableStateOf(initialRgb.first.toString()) }
                var greenInput by remember(initialRgb) { mutableStateOf(initialRgb.second.toString()) }
                var blueInput by remember(initialRgb) { mutableStateOf(initialRgb.third.toString()) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.ColorLens,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cor de Fundo Personalizada (HEX / RGB)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = colorInputMode == "HEX",
                                onClick = { colorInputMode = "HEX" },
                                label = { Text("Formato HEX") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = colorInputMode == "RGB",
                                onClick = { colorInputMode = "RGB" },
                                label = { Text("Formato RGB") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (colorInputMode == "HEX") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customHexInput,
                                    onValueChange = { newValue ->
                                        customHexInput = newValue
                                        if (newValue.length == 7 && newValue.startsWith("#")) {
                                            viewModel?.updateBackgroundColor(newValue)
                                        }
                                    },
                                    label = { Text("Ex: #0B0E14, #000000, #0A1128") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            agenda.de.myapplicationanim1.ui.theme.parseHexColor(
                                                customHexInput,
                                                MaterialTheme.colorScheme.background
                                            )
                                        )
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = redInput,
                                    onValueChange = {
                                        redInput = it
                                        val r = it.toIntOrNull() ?: 0
                                        val g = greenInput.toIntOrNull() ?: 0
                                        val b = blueInput.toIntOrNull() ?: 0
                                        val hex = String.format("#%02X%02X%02X", r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
                                        viewModel?.updateBackgroundColor(hex)
                                    },
                                    label = { Text("R (0-255)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = greenInput,
                                    onValueChange = {
                                        greenInput = it
                                        val r = redInput.toIntOrNull() ?: 0
                                        val g = it.toIntOrNull() ?: 0
                                        val b = blueInput.toIntOrNull() ?: 0
                                        val hex = String.format("#%02X%02X%02X", r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
                                        viewModel?.updateBackgroundColor(hex)
                                    },
                                    label = { Text("G (0-255)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = blueInput,
                                    onValueChange = {
                                        blueInput = it
                                        val r = redInput.toIntOrNull() ?: 0
                                        val g = greenInput.toIntOrNull() ?: 0
                                        val b = it.toIntOrNull() ?: 0
                                        val hex = String.format("#%02X%02X%02X", r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
                                        viewModel?.updateBackgroundColor(hex)
                                    },
                                    label = { Text("B (0-255)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            agenda.de.myapplicationanim1.ui.theme.parseHexColor(
                                                viewModel?.backgroundColorHex ?: "#0B0E14",
                                                MaterialTheme.colorScheme.background
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // 4. Animes Notificados / Gerenciar Notificações
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Animes Notificados (${notifiedAnimes.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Animes marcados para receber notificação quando um novo episódio for lançado:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (notifiedAnimes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhum anime notificado",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Toque no ícone de sino nos cards de anime ou na tela de detalhes para receber alertas de episódios.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    items = notifiedAnimes,
                    key = { it.id }
                ) { anime ->
                    NotifiedAnimeRow(
                        anime = anime,
                        onRemoveClick = { viewModel?.removeNotifiedAnime(anime.id) },
                        onAnimeClick = { onAnimeClick(anime.id) }
                    )
                }
            }

            item {
                ApiSourceFooter(text = "Dados fornecidos por: AniList API")
            }
        }
    }
}

@Composable
fun NotifiedAnimeRow(
    anime: NotifiedAnime,
    onRemoveClick: () -> Unit,
    onAnimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onAnimeClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp, 72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(anime.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Movie,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: #${anime.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Remover notificação",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
