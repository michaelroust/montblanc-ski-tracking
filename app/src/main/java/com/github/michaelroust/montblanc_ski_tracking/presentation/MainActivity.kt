/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.github.michaelroust.montblanc_ski_tracking.presentation

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import com.github.michaelroust.montblanc_ski_tracking.R
import com.github.michaelroust.montblanc_ski_tracking.presentation.theme.MontblancSkiTrackingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Code to directly start SafetyActivity
//        val intent = Intent(this, SafetyActivity::class.java)
//        startActivity(intent)

        setContent {
            MainApp(
                openFeatureStats = {
                    val intent = Intent(this, StatsActivity::class.java)
                    startActivity(intent)
                }
            )
        }
        // Keep screen on. See: https://developer.android.com/training/scheduling/wakelock
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}


@Composable
fun MainApp(openFeatureStats: () -> Unit) {
    MontblancSkiTrackingTheme {
        Image(
            painter = painterResource(id = R.drawable.mountain_round),
            contentDescription = stringResource(id = R.string.dog_content_description)
        )
        CustomColumnLite {
            Button(
                modifier = Modifier
                    .size(60.dp)
                    .align(CenterHorizontally),
                onClick = openFeatureStats) {
                Icon(
                    Icons.Outlined.PlayArrow,
                    modifier = Modifier.size(50.dp),
                    contentDescription = "Localized description")
            }
            Spacer(modifier = Modifier.size(10.dp))

            CustomText(text = "Press to start skiing")
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview3() {
    MainApp({/* Do Nothing this is just for UI development */})
}