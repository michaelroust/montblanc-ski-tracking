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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.github.michaelroust.montblanc_ski_tracking.presentation.theme.MontblancSkiTrackingTheme

class SafetyActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
//            SafetyApp2()
//            StatsAppGeneric()

            StatsAppGeneric {
                CustomText(text = "Hello")
            }
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
                .align(Alignment.Center),
            verticalArrangement = Arrangement.SpaceEvenly,
            content = columnContent
        )

        TimeText()
    }
}


@Composable
fun StatsAppGeneric(content: @Composable (ColumnScope.() -> Unit)) {
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
            columnContent = content
        )
        HorizontalPageIndicator(
            modifier = Modifier.padding(4.dp),
            pageIndicatorState = pageIndicatorState
        )
    }
}


@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun RunningPreview() {
    StatsAppGeneric {
        CustomText(text = "Hello")
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun StatsPrePreview() {
    StatsAppGeneric {
        CustomText(text = "Hello")
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun StatsPreview() {
    StatsAppGeneric {
        CustomText(text = "Hello")
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun FinalPreview() {
    StatsAppGeneric {
        CustomText(text = "Hello")
    }
}