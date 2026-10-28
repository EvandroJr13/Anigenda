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
import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import agenda.de.myapplicationanim1.update.UpdateDialog
import agenda.de.myapplicationanim1.update.UpdateInfo
import agenda.de.myapplicationanim1.update.UpdateManager
import kotlinx.coroutines.launch
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import agenda.de.myapplicationanim1.ui.components.GlobalNotificationAction
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
    val context = LocalContext.current
    var themeUnlocked by remember {
        mutableStateOf(
            context.getSharedPreferences("theme_ad_prefs", Context.MODE_PRIVATE)
                .getBoolean("unlocked", false)
        )
    }
    val notifiedAnimes by (viewModel?.notifiedAnimes?.collectAsState() ?: remember { mutableStateOf(emptyList()) })
    var customHexInput by remember(viewModel?.backgroundColorHex) {
        mutableStateOf(viewModel?.backgroundColorHex ?: "#0B0E14")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val blackShadow = Shadow(color = Color.Black, offset = Offset(0f, 0f), blurRadius = 6f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ani",
                            style = TextStyle(
                                fontFamily = ChewyFontFamily,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                shadow = blackShadow
                            )
                        )
                        Text(
                            text = "genda",
                            style = TextStyle(
                                fontFamily = ChewyFontFamily,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                shadow = blackShadow
                            )
                        )
                    }
                },
                actions = {
                    GlobalNotificationAction()
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
                                        val manager = UpdateManager(context, "EvandroJr13", "Anigenda")
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
                            val manager = UpdateManager(context, "EvandroJr13", "Anigenda")
                            manager.downloadAndInstall(updateInfo!!.apkUrl)
                        }
                    )
                }
            }

            // Email Feedback Button ("Enviar-feedback")
            item {
                val context = LocalContext.current
                var showEmailChooserDialog by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEmailChooserDialog = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
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
                        Text(
                            text = "Enviar-feedback",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { showEmailChooserDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Enviar")
                        }
                    }
                }

                if (showEmailChooserDialog) {
                    AlertDialog(
                        onDismissRequest = { showEmailChooserDialog = false },
                        title = { Text("Escolher onde abrir") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Escolha onde deseja abrir o e-mail para evandro.asjunior13@gmail.com:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // 1. Navegador
                                OutlinedButton(
                                    onClick = {
                                        showEmailChooserDialog = false
                                        try {
                                            val url = "https://mail.google.com/mail/?view=cm&fs=1&to=evandro.asjunior13@gmail.com&su=Feedback%20Anime%20Schedule&body=Olá%20Evandro,%0A%0AFeedback%20sobre%20o%20app%20Anime%20Schedule:%0A"
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Erro ao abrir navegador: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("🌐 Navegador")
                                }

                                // 2. App do Gmail
                                OutlinedButton(
                                    onClick = {
                                        showEmailChooserDialog = false
                                        try {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                                data = android.net.Uri.parse("mailto:evandro.asjunior13@gmail.com")
                                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback Anime Schedule")
                                                putExtra(android.content.Intent.EXTRA_TEXT, "Olá Evandro,\n\nFeedback sobre o app Anime Schedule:\n")
                                                setPackage("com.google.android.gm")
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                                    data = android.net.Uri.parse("mailto:evandro.asjunior13@gmail.com")
                                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback Anime Schedule")
                                                    putExtra(android.content.Intent.EXTRA_TEXT, "Olá Evandro,\n\nFeedback sobre o app Anime Schedule:\n")
                                                }
                                                context.startActivity(android.content.Intent.createChooser(intent, "Abrir com o Gmail"))
                                            } catch (ex: Exception) {
                                                Toast.makeText(context, "Gmail não encontrado: ${ex.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("✉️ App do Gmail")
                                }

                                // 3. Outlook
                                OutlinedButton(
                                    onClick = {
                                        showEmailChooserDialog = false
                                        try {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                                data = android.net.Uri.parse("mailto:evandro.asjunior13@gmail.com")
                                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback Anime Schedule")
                                                putExtra(android.content.Intent.EXTRA_TEXT, "Olá Evandro,\n\nFeedback sobre o app Anime Schedule:\n")
                                                setPackage("com.microsoft.office.outlook")
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            try {
                                                val url = "https://outlook.live.com/owa/?path=/mail/action/compose&to=evandro.asjunior13@gmail.com&subject=Feedback%20Anime%20Schedule"
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                                context.startActivity(intent)
                                            } catch (ex: Exception) {
                                                Toast.makeText(context, "Outlook não encontrado: ${ex.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("📨 Outlook")
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showEmailChooserDialog = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
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

            // Rewarded Ad block to unlock/support theme change
            item {
                var isAdLoading by remember { mutableStateOf(false) }

                fun loadRewardedAd(useTestId: Boolean = false) {
                    isAdLoading = true
                    Toast.makeText(context, "Carregando anúncio premiado...", Toast.LENGTH_SHORT).show()
                    val adUnitIdToUse = if (useTestId) "ca-app-pub-3940256099942544/5224354917" else "ca-app-pub-9672048369965769/1840427717"
                    val adRequest = com.google.android.gms.ads.AdRequest.Builder().build()
                    com.google.android.gms.ads.rewarded.RewardedAd.load(
                        context,
                        adUnitIdToUse,
                        adRequest,
                        object : com.google.android.gms.ads.rewarded.RewardedAdLoadCallback() {
                            override fun onAdLoaded(ad: com.google.android.gms.ads.rewarded.RewardedAd) {
                                isAdLoading = false
                                val activity = context as? android.app.Activity ?: run {
                                    themeUnlocked = true
                                    context.getSharedPreferences("theme_ad_prefs", Context.MODE_PRIVATE).edit().putBoolean("unlocked", true).apply()
                                    Toast.makeText(context, "🎉 Tema desbloqueado com sucesso!", Toast.LENGTH_LONG).show()
                                    return
                                }
                                ad.show(activity) { _ ->
                                    themeUnlocked = true
                                    context.getSharedPreferences("theme_ad_prefs", Context.MODE_PRIVATE)
                                        .edit()
                                        .putBoolean("unlocked", true)
                                        .apply()
                                    Toast.makeText(context, "🎉 Parabéns! Anúncio assistido e temas desbloqueados!", Toast.LENGTH_LONG).show()
                                }
                            }
                            override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                                isAdLoading = false
                                if (!useTestId) {
                                    // Fallback to Google test rewarded ad unit ID if real ID fails to load
                                    loadRewardedAd(useTestId = true)
                                } else {
                                    // If test ID also fails (e.g. offline), allow unlock as fallback
                                    themeUnlocked = true
                                    context.getSharedPreferences("theme_ad_prefs", Context.MODE_PRIVATE)
                                        .edit()
                                        .putBoolean("unlocked", true)
                                        .apply()
                                    Toast.makeText(context, "🎉 Temas liberados (Modo offline/fallback)!", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }

                Card(
                    onClick = {
                        if (!themeUnlocked) {
                            loadRewardedAd(false)
                        } else {
                            Toast.makeText(context, "Temas já desbloqueados e liberados!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (themeUnlocked) MaterialTheme.colorScheme.surfaceContainerHigh
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Movie,
                            contentDescription = null,
                            tint = if (themeUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (themeUnlocked) "🎨 Temas Personalizados Liberados" else "🎬 Assistir Anúncio para Desbloquear Temas",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (themeUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (themeUnlocked) "Você pode alternar entre os temas do app livremente." else "Assista a um vídeo premiado para apoiar o app e liberar a troca de tema.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (themeUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        if (isAdLoading) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(AppThemeMode.entries) { mode ->
                val activeTheme = viewModel?.themeMode ?: currentTheme
                val selected = mode == activeTheme
                Card(
                    onClick = {
                        if (themeUnlocked) {
                            viewModel?.updateThemeMode(mode) ?: onThemeSelected(mode)
                        } else {
                            Toast.makeText(context, "🔒 Assista ao anúncio premiado acima para desbloquear e trocar de tema!", Toast.LENGTH_LONG).show()
                        }
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
                                if (themeUnlocked) {
                                    viewModel?.updateThemeMode(mode) ?: onThemeSelected(mode)
                                } else {
                                    Toast.makeText(context, "🔒 Assista ao anúncio premiado acima para desbloquear e trocar de tema!", Toast.LENGTH_LONG).show()
                                }
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
