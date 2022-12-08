/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.github.michaelroust.montblanc_ski_tracking.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.github.michaelroust.montblanc_ski_tracking.R
import com.github.michaelroust.montblanc_ski_tracking.presentation.theme.MontblancSkiTrackingTheme

class SafetyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StatsApp()
        }

        //---------------------------------------------------------------------------------------
        // Scrolling

        // Keep screen on. See: https://developer.android.com/training/scheduling/wakelock
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}


@OptIn(ExperimentalWearMaterialApi::class)
@Composable
fun CustomColumnWithSideButtons (
    leftButtonOnClick: () -> Unit,
    rightButtonOnClick: () -> Unit,
    pageIndicatorState: PageIndicatorState,
    mountainBackground: Boolean=false,
    columnContent: @Composable (ColumnScope.() -> Unit)
) {
    val sideButtonAlpha = 0f

    val topAndBottomMargin = 22.dp
    val sideButtonsWidth = 70.dp

    var tempModif = Modifier
        .fillMaxSize()

    if (mountainBackground) {
        Image(
            painter = painterResource(id = R.drawable.mountain_round),
            contentDescription = stringResource(id = R.string.dog_content_description)
        )
    } else {
        tempModif = tempModif.background(MaterialTheme.colors.background)
    }

    BoxWithConstraints(
        modifier = tempModif
    ) {
        Button(
            modifier = Modifier
                .alpha(sideButtonAlpha)
                .fillMaxHeight()
                .width(sideButtonsWidth)
                .align(Alignment.CenterStart),
            onClick = leftButtonOnClick) {}

        Button(
            modifier = Modifier
                .alpha(sideButtonAlpha)
                .fillMaxHeight()
                .width(sideButtonsWidth)
                .align(Alignment.CenterEnd),
            onClick = rightButtonOnClick) {}

        Column(
            modifier = Modifier
                .height(this.maxHeight - topAndBottomMargin * 2)
//                .width(this.maxWidth - sideButtonsWidth * 2)
                .fillMaxWidth()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.SpaceEvenly,
            content = columnContent
        )

        TimeText()
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun StatsApp() {
    val maxPages = 3
    var selectedPage by remember { mutableStateOf(0) }
    var finalValue by remember { mutableStateOf(0) }

    val animatedSelectedPage by animateFloatAsState(
        targetValue = selectedPage.toFloat(),
    ) {
        finalValue = it.toInt()
    }

    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = animatedSelectedPage - finalValue
            override val selectedPage: Int
                get() = finalValue
            override val pageCount: Int
                get() = maxPages
        }
    }

    fun swipeLeft() {
        if (selectedPage > 0)
            selectedPage--
    }

    fun swipeRight() {
        if (selectedPage < (maxPages - 1))
            selectedPage++
    }

    MontblancSkiTrackingTheme {
        CustomColumnWithSideButtons(
            leftButtonOnClick = { swipeLeft() },
            rightButtonOnClick = { swipeRight() },
            pageIndicatorState = pageIndicatorState,
            selectedPage == 2
        ) {

            if (selectedPage == 0)
                StatsStuff(0.0, distTraveled = 0.0, avgSkiingSpeed = 10.0, topSpeed = 20.0, deltaElevDown = 30.0)
            else if (selectedPage == 1) {
                if (true) { // TODO replace with whatever is needed
                    LapsStatsStuff(nLaps = 0)
                } else {
                    StatsStuff(0.0, distTraveled = 100.0, avgSkiingSpeed = 10.0, topSpeed = 50.0, deltaElevDown = 100.0)
                }
            } else if (selectedPage == 2) {
                CustomColumnLite {
                    val startStopText = if (false) "Resume" else "Pause"
                    CustomCompactChipLite(text = "$startStopText skiing") {

                    }
                    CustomCompactChip("Stop skiing") {

                    }
                }
            }
        }
        HorizontalPageIndicator(
            modifier = Modifier.padding(4.dp),
            pageIndicatorState = pageIndicatorState
        )
    }
}

private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)


@Composable
fun LapsStatsStuff(nLaps:Int) {
    CustomColumn {

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = Color(0xFFDDDDDD),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            text = "Next page for stats over all"
        )

        Row(
            modifier = Modifier
                .align(CenterHorizontally)
            ,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Rounded.Replay,
                "Localized description",
                modifier = Modifier.align(CenterVertically)
            )
            Spacer(Modifier.width(4.dp))
            CustomLapsText(text = "$nLaps laps")
        }
    }
}


@Composable
fun StatsStuff(activeTime:Double, distTraveled:Double, avgSkiingSpeed:Double, topSpeed:Double, deltaElevDown:Double) {

    val distanceTraveledText = "${distTraveled.format(1)} m"
    val avgSpeedText = avgSkiingSpeed.format(1)
    val topSpeedText = topSpeed.format(1)
    val elevText = "${deltaElevDown.format(1)} m"

    val hours = activeTime.toInt() / 3600
    val minutes = (activeTime.toInt() % 3600) / 60
    val seconds = (activeTime.toInt()) % 60

    Spacer(
        Modifier
            .fillMaxWidth()
            .height(0.dp))

    CustomStatsText(text = String.format("%02dº:%02d'':%02d'", hours, minutes, seconds))

    CustomStatsTopBottomText(distanceTraveledText)

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                modifier = Modifier
                    .align(End),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Text(
                    textAlign = TextAlign.Right,
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    text = avgSpeedText
                )
            }

            CustomInfoText(text = "AVG km/h")
        }

        Column {
            Row(
                modifier = Modifier
                    .align(End),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    textAlign = TextAlign.Right,
                    fontSize = 18.sp,
                    color = MaterialTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    text = topSpeedText
                )
            }

            CustomInfoText(text = "TOP km/h")
        }
    }

    CustomStatsTopBottomText(elevText)
}


// @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
// @Composable
// fun StatsPrePreview() {
//     StatsAppGeneric {
//         CustomText(text = "Hello")
//     }
// }

// @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
// @Composable
// fun StatsPreview() {
//     StatsAppGeneric {
//         CustomText(text = "Hello")
//     }
// }

// @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
// @Composable
// fun FinalPreview() {
//     StatsAppGeneric {
//         CustomText(text = "Hello")
//     }
// }