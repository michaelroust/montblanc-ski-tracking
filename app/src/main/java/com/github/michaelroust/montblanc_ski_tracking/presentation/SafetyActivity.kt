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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.github.michaelroust.montblanc_ski_tracking.presentation.theme.MontblancSkiTrackingTheme

class SafetyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Composable
        fun SafetyApp2() {
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
                CustomColumn {

                    if (selectedPage != 0) {
                        CustomChip(text = "Left") {
                            swipeLeft()
                        }
                    }

                    if (selectedPage != maxPages - 1) {
                        CustomChip(text = "Right") {
                            swipeRight()
                        }
                    }
                }
                HorizontalPageIndicator(
                    pageIndicatorState = pageIndicatorState
                )
            }
        }

        setContent {
            SafetyApp2()
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
    columnContent: @Composable (ColumnScope.() -> Unit)
) {
    val sideButtonsWidth = 16.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {

        Button(
            modifier = Modifier
                // .alpha(0f)
                .fillMaxHeight()
                .width(sideButtonsWidth)
                .align(Alignment.CenterStart),
            onClick = leftButtonOnClick) {}

        Button(
            modifier = Modifier
                // .alpha(0f)
                .fillMaxHeight()
                .width(sideButtonsWidth)
                .align(Alignment.CenterEnd),
            onClick = rightButtonOnClick) {}

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(this.maxWidth - sideButtonsWidth * 2)
                .background(MaterialTheme.colors.background)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            content = columnContent
        )
        TimeText()
    }
}


@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun SafetyApp2() {
    MontblancSkiTrackingTheme {
        CustomColumnWithSideButtons(
            leftButtonOnClick = { /*TODO*/ },
            rightButtonOnClick = { /*TODO*/ }) {
            CustomText(text = "Hello")
        }
    }
}

@Composable
fun SafetyApp() {
    MontblancSkiTrackingTheme {
        CustomColumn {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
//                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = "haha"
                )

                Text(
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary,
                    text = "hoho"
                )

                CompactButton(onClick = { /*TODO*/ }) {
//
                }

                Button(
                    modifier = Modifier.width(16.dp),
                    onClick = { /*TODO*/ }) {

                }

//                CustomText(text = "haha")
//                CustomText(text = "hoho")
//                ChipCounter()
//                ChipCounter()
            }



//            Button(onClick = { /*TODO*/ }) {
//
//            }
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    SafetyApp()
}