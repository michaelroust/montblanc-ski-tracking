package com.github.michaelroust.montblanc_ski_tracking.presentation

import android.graphics.fonts.FontStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.github.michaelroust.montblanc_ski_tracking.R
import com.github.michaelroust.montblanc_ski_tracking.presentation.theme.MontblancSkiTrackingTheme

@Composable
fun ExampleBox(shape: Shape){
    Column(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.Center)) {
        Box(
            modifier = Modifier.size(100.dp).clip(shape).background(Color.Red)
        )
    }
}



@Composable
fun CustomColumn(content: @Composable (ColumnScope.() -> Unit)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
        content = content
    )
}

@Composable
fun CustomText(text: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = text
    )
}

@Composable
fun CustomMiddleStatsText(text: String) {
    Text(
        textAlign = TextAlign.Center,
        color = Color(0xFFDDDDDD),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        text = text
    )
}

@Composable
fun CustomStatsText(text: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        fontSize = 12.sp,
        text = text
    )
}

@Composable
fun CustomStatsTopBottomText(text: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,

        color = Color(0xFFDDDDDD),
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        text = text
    )
}

@Composable
fun CustomStatsMiddleText(text: String) {
    Text(
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        text = text
    )
}

@Composable
fun CustomInfoText(text: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = Color(0xFFDDDDDD),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        text = text
    )
}

@Composable
fun CustomLapsText(text: String) {

    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        text = text
    )
}


@Composable
fun CustomChip(text: String, onClick: () -> Unit) {
    Chip(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        label = {
            Text(
                text = text,
                textAlign = TextAlign.Center
            )
        },
        onClick = onClick
    )
}

@Composable
fun CustomCompactChip(text: String, onClick: () -> Unit) {
    CompactChip(
        modifier = Modifier
            .padding(horizontal = 32.dp, vertical = 4.dp),
        label = {
            Text(
                text = text,
                textAlign = TextAlign.Center
            )
        },
        onClick = onClick
    )
}


@Composable
fun CustomCompactChipLite(text: String, onClick: () -> Unit) {
    CompactChip(
        modifier = Modifier
            .padding(horizontal = 32.dp, vertical = 4.dp),

        label = {
            Text(
                text = text,
                textAlign = TextAlign.Center
            )
        },
//        icon = {
//            Icon(
//                painter = painterResource(id = R.drawable.avatar),
//                contentDescription = "Mark Castle",
//                modifier = Modifier.size(ChipDefaults.LargeIconSize)
//                    .wrapContentSize(align = Alignment.Center)
//            )
//        }
        onClick = onClick
    )
}

@Composable
fun Counter() {
    val counterState = remember { mutableStateOf(0) }
    Button(onClick = { counterState.value++ }) {
        Text(text = "n_clicks: ${counterState.value}")
    }
}

@Composable
fun ChipCounter() {
    val counterState = rememberSaveable { mutableStateOf(0) }

    Chip(
        label = { Text("n_clicks: ${counterState.value}") },
        modifier = Modifier.padding(16.dp),
        onClick = {
            counterState.value++
        })
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun UIElementsPreview() {
    MontblancSkiTrackingTheme {

        CustomColumn {
            CustomText(text = "CustomTextTest")
            CustomChip(
                text = "CustomChipTest",
                onClick = {/* Do Nothing just for UI test*/ }
            )
        }
    }
}