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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.github.michaelroust.montblanc_ski_tracking.presentation.theme.MontblancSkiTrackingTheme

class SafetyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
//            SafetyApp2()
//            StatsAppGeneric()

//            RunningPreview()
//            StatsAppGeneric {
//                CustomText(text = "Hello")
//            }
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
    columnContent: @Composable (ColumnScope.() -> Unit)
) {
    val sideButtonAlpha = 0f

    val topAndBottomMargin = 22.dp
    val sideButtonsWidth = 70.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
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
        ) {
            if (selectedPage == 0 || selectedPage == 1) {
                var distanceTraveledText = "0.0m"
                var avgSpeedText = "0.0"
                var topSpeedText = "0.0"
                var elevText = "0.0m"

                if (selectedPage == 0) {
                    distanceTraveledText = "0.0m"
                    avgSpeedText = "0.0"
                    topSpeedText = "0.0"
                    elevText = "0.0m"
                } else if (selectedPage == 1) {
                    distanceTraveledText = "2.0m"
                    avgSpeedText = "2.0"
                    topSpeedText = "2.0"
                    elevText = "2.0m"
                }

                CustomStatsTopBottomText(distanceTraveledText)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(CenterHorizontally),
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
            } else if (selectedPage == 2) {
                Text("Hello")
            }
        }
        HorizontalPageIndicator(
            modifier = Modifier.padding(4.dp),
            pageIndicatorState = pageIndicatorState
        )
    }
}


// @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
// @Composable
// fun RunningPreview() {
// }

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