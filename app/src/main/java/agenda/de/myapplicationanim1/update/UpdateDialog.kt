package agenda.de.myapplicationanim1.update

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova atualização disponível (${updateInfo.versionName})") },
        text = {
            Text(
                updateInfo.releaseNotes?.takeIf { it.isNotBlank() }
                    ?: "Uma nova versão do aplicativo está disponível para download."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Baixar e Atualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Agora não")
            }
        }
    )
}
