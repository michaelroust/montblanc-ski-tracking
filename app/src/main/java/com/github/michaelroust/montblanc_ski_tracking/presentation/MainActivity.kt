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
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
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
        CustomColumn {
            CustomChip(text = "Start skiing", onClick = openFeatureStats)
            CustomText(text = "Press to start skiing")
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview3() {
    MainApp({/* Do Nothing this is just for UI development */})
}