package agenda.de.myapplicationanim1.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import agenda.de.myapplicationanim1.R

@Composable
fun AdaptiveIconView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        // Foreground
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun AppIconPreview() {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .background(Color(0xFF0F172A)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AG Anime Guide App Icon",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Squircle / Rounded Square Adaptive Icon
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AdaptiveIconView(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Squircle", color = Color.Gray, fontSize = 12.sp)
            }

            // Circle / Round Adaptive Icon
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AdaptiveIconView(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF334155), CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Circle", color = Color.Gray, fontSize = 12.sp)
            }

            // Raw Vector (108dp Full Canvas)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AdaptiveIconView(
                    modifier = Modifier
                        .size(108.dp)
                        .border(1.dp, Color(0xFF334155))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Full Canvas", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
