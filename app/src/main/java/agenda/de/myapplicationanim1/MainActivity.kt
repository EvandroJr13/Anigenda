package agenda.de.myapplicationanim1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import agenda.de.myapplicationanim1.ui.MainApp
import agenda.de.myapplicationanim1.ui.theme.AgendaDeAnimesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgendaDeAnimesTheme {
                MainApp()
            }
        }
    }
}
