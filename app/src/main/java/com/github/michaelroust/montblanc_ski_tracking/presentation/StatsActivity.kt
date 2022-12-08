/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.github.michaelroust.montblanc_ski_tracking.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.app.ActivityCompat
import androidx.wear.compose.material.*
import com.github.michaelroust.montblanc_ski_tracking.R
import com.github.michaelroust.montblanc_ski_tracking.presentation.theme.MontblancSkiTrackingTheme
import com.github.michaelroust.montblanc_ski_tracking.presentation.utilities.Globals.LOG_TAG
import com.github.michaelroust.montblanc_ski_tracking.presentation.utilities.Ticker
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import java.lang.Double.max

class StatsActivity : ComponentActivity() {
    companion object {
        const val GPS_LOCATION_INTERVAL_MILLIS = 1000L
    }

    private val isSkiing = mutableStateOf(false)
    private val activeTime = mutableStateOf(0.0)
    private val nRuns = mutableStateOf(0)
    private val distTraveled = mutableStateOf(0.0)
    private val deltaElevDown = mutableStateOf(0.0)
    private val curSpeed = mutableStateOf(0.0)
    private val avgSkiingSpeed = mutableStateOf(0.0)
    private val topSpeed = mutableStateOf(0.0)

    private val totalActiveTime = mutableStateOf(0.0)
    private val totalDistTraveled = mutableStateOf(0.0)
    private val totalDeltaElevDown = mutableStateOf(0.0)
    private val totalAvgSkiingSpeed = mutableStateOf(0.0)
    private val totalTopSpeed = mutableStateOf(0.0)

    lateinit var activeTimeTicker: Ticker
    lateinit var locationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //-----------------------------------------------------------------------------------
        // Active time setup

        // Simple way to increment activeTime every second
        activeTimeTicker = Ticker(
            { activeTime.value++ },
            1000
        )

        //-----------------------------------------------------------------------------------
        // Sensors setup

        // TODO If needed...

        //-----------------------------------------------------------------------------------
        // Location setup

        isGooglePlayServicesAvailable()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        enableLocation()
        startLocationUpdates(GPS_LOCATION_INTERVAL_MILLIS, locationListener)

        //-----------------------------------------------------------------------------------

        // Keep screen on. See: https://developer.android.com/training/scheduling/wakelock
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            StatsApp()
        }
    }

    //---------------------------------------------------------------------------------------
    // General functions

    private fun toggleSkiing() {
        if (!isSkiing.value)
            startSkiing()
        else
            stopSkiing()
    }

    private fun startSkiing() {
        if (!isSkiing.value) {
            Log.d(LOG_TAG, "Started skiing")

            isSkiing.value = true

            activeTime.value = 0.0
            distTraveled.value = 0.0
            deltaElevDown.value = 0.0
            curSpeed.value = 0.0
            avgSkiingSpeed.value = 0.0
            topSpeed.value = 0.0

            activeTimeTicker.start()
        }
    }

    private fun stopSkiing() {
        if (isSkiing.value) {
            Log.d(LOG_TAG, "Stopped skiing")

            isSkiing.value = false
            activeTimeTicker.stop()
            nRuns.value++

            totalActiveTime.value += activeTime.value
            totalDistTraveled.value += distTraveled.value
            totalDeltaElevDown.value += deltaElevDown.value

            totalSpeedTimeSum += speedTimeSum
            totalTimeSum += timeSum
            totalAvgSkiingSpeed.value = totalSpeedTimeSum / totalTimeSum
            totalTopSpeed.value = max(totalTopSpeed.value, topSpeed.value)
        }
    }

    //---------------------------------------------------------------------------------------
    // Location listener code
    //      All code here is related to updating all statistics related to Location data.
    //      The locationListener is meant to be called at regular intervals with new Location data.

    var prevLocationBuffer: Location? = null

    var speedTimeSum: Double = 0.0
    var timeSum: Double = 0.0

    var totalSpeedTimeSum: Double = 0.0
    var totalTimeSum: Double = 0.0

    private val locationListener = LocationListener { location ->

        Log.d(LOG_TAG, "isSkiing: $isSkiing \tLocation Listener: $location")

        // Update current speed
        curSpeed.value = location.speed * 3.6

        if (!isSkiing.value) {
            // Set Previous Location to null. Needed due to design of average speed computation.
            if (curSpeed.value >= 3) {
                startSkiing()
            }

            prevLocationBuffer = null
        } else {

            if (curSpeed.value < 2) {
                stopSkiing()
            }
        }
        val prevLocation = prevLocationBuffer

        // Update top speed as needed
        if (curSpeed.value > topSpeed.value)
            topSpeed.value = curSpeed.value

        if (isSkiing.value) {
            //--------------------------------------------------------------------
            // Average speed calculations

            // Average speed calculated according to following formula:
            //   (v1*t1 + v2*t2 + ... vn*tn) / (t1 + t2 + ... + tn)
            // speedTimeSum stores the sum of all vi*ti's
            // timeSum stores sum of ti's

            val timeSinceLastLocation =
                if (prevLocation == null) {
                    GPS_LOCATION_INTERVAL_MILLIS / 1000.0
                } else {
                    ((location.time - prevLocation.time) / 1000.0)
                }

            speedTimeSum += curSpeed.value * timeSinceLastLocation
            timeSum += timeSinceLastLocation

            avgSkiingSpeed.value = speedTimeSum / timeSum

            //--------------------------------------------------------------------
            // Distance traveled calculations

            if (prevLocation != null) {
                distTraveled.value += prevLocation.distanceTo(location)
            }

            //--------------------------------------------------------------------
            // Elevation calculations

            if (prevLocation != null && prevLocation.altitude > location.altitude) {
                deltaElevDown.value += location.altitude - prevLocation.altitude
            }

            //--------------------------------------------------------------------

            prevLocationBuffer = location
        }
    }

    //----------------------------------------------------------------------------------------
    // UI

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

            // TODO Comment this debug button out.
//            Button(
//                modifier = Modifier
//                    .alpha(sideButtonAlpha)
//                    .fillMaxHeight()
//                    .width(30.dp)
//                    .align(Alignment.Center),
//                onClick = {
//                    toggleSkiing()
//                }) {}

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
                    StatsStuff(false)
                else if (selectedPage == 1) {
                    LapsStatsStuff()
//                    if (true) {
//                        LapsStatsStuff(nLaps = nRuns.value)
//                    } else {
//                        StatsStuff(activeTime.value, distTraveled = 100.0, avgSkiingSpeed = 10.0, topSpeed = 50.0, deltaElevDown = 100.0)
//                    }
                } else if (selectedPage == 2) {
                    StatsStuff(true)
//                    CustomColumnLite {
//                        val startStopText = if (false) "Resume" else "Pause"
//                        CustomCompactChipLite(text = "$startStopText skiing") {
//
//                        }
//                        CustomCompactChip("Stop skiing") {
//
//                        }
//                    }
                }
            }
            HorizontalPageIndicator(
                modifier = Modifier.padding(4.dp),
                pageIndicatorState = pageIndicatorState
            )
        }
    }

//    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)


    @Composable
    fun LapsStatsStuff() {
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
                CustomLapsText(text = "${nRuns.value} laps")
            }
        }
    }

    private fun formatTime(time:Int): String {
        val hours = time / 3600
        val minutes = (time % 3600) / 60
        val seconds = time % 60

        return String.format("%02dº%02d'%02d''", hours, minutes, seconds)
    }

    @Composable
    fun StatsStuff(showTotals: Boolean) {

        val activeTimeText = formatTime((if (!showTotals) activeTime else totalActiveTime).value.toInt())
        val distTraveledText = "${(if (!showTotals) distTraveled else totalDistTraveled).value.format(1)} m"
        val avgSpeedText = (if (!showTotals) avgSkiingSpeed else totalAvgSkiingSpeed).value.format(1)
        val topSpeedText = (if (!showTotals) topSpeed else totalTopSpeed).value.format(1)
        val elevText = "${(if (!showTotals) deltaElevDown else totalDeltaElevDown).value.format(1)} m"

        Text(
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            fontSize = 16.sp,
            text = activeTimeText
        )

        CustomStatsTopBottomText(distTraveledText)

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

    //----------------------------------------------------------------------------------------
    // Old UI

    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)


    /*@Composable
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


    @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
    @Composable
    fun OneLapStats2() {
//        CustomText(text = "Hello")
        StatsAppGeneric {
            CustomText(text = "Hello")

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
//                CustomColumnLite {
//
//                }
            }

        }
    }


    @OptIn(ExperimentalWearMaterialApi::class)
    @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
    @Composable
    fun OneLapStats() {
        MontblancSkiTrackingTheme {
            CustomColumn {

                CustomStatsTopBottomText(text = "${distTraveled.value.format(1)} m")
                CustomStatsMiddleText(text = "${avgSkiingSpeed.value.format(1)}")
                CustomInfoText(text = "AVG km/h")
                CustomStatsMiddleText(text = "${topSpeed.value.format(1)}")
                CustomInfoText(text = "TOP km/h")
                CustomStatsTopBottomText(text = "${deltaElevDown.value.format(1)} m")
            }
        }
        TimeText()
    }

    @OptIn(ExperimentalWearMaterialApi::class)
    @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
    @Composable
    fun TransitionAllLapsStats() {
        MontblancSkiTrackingTheme {

            CustomColumn {
                CustomInfoText(text = "Statistics over all")
                Row{
                    Icon(Icons.Rounded.Search, contentDescription = "Localized description")
                    CustomLapsText(text = "${nRuns.value} laps")
                }
            }
        }
        TimeText()
    }



    @OptIn(ExperimentalWearMaterialApi::class)
    @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
    @Composable
    fun AllLapsStats() {
        MontblancSkiTrackingTheme {
            CustomColumn {
                val hours = activeTime.value.toInt() / 3600
                val minutes = (activeTime.value.toInt() % 3600) / 60
                val seconds = (activeTime.value.toInt()) % 60

                CustomStatsText(text = String.format("%02dº:%02d'':%02d'", hours, minutes, seconds))

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Icon(Icons.Rounded.Search, contentDescription = "Localized description")
                    CustomStatsTopBottomText(text = "${distTraveled.value.format(1)} m")
                }

                Row(horizontalArrangement = Arrangement.SpaceEvenly)  {
                    CustomStatsMiddleText(text = "${topSpeed.value.format(1)}")
                    CustomStatsMiddleText(text = "${avgSkiingSpeed.value.format(1)}")
                }
                Row(horizontalArrangement = Arrangement.SpaceEvenly)  {
                    CustomMiddleStatsText(text = "TOP km/h")
                    CustomMiddleStatsText(text = "AVG km/h")
                }

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Icon(Icons.Rounded.Search, contentDescription = "Localized description")
                    CustomStatsTopBottomText(text = "${deltaElevDown.value.format(1)} m")
                }
            }
        }
        val hours = activeTime.value.toInt() / 3600
        val minutes = (activeTime.value.toInt() % 3600) / 60
        val seconds = (activeTime.value.toInt()) % 60



        val leadingTextStyle = TimeTextDefaults.timeTextStyle(color = MaterialTheme.colors.primary)

        TimeText()
    }


    @OptIn(ExperimentalWearMaterialApi::class)
    @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
    @Composable
    fun FinishRun() {
        MontblancSkiTrackingTheme {
            Image(
                painter = painterResource(id = R.drawable.mountain_round),
                contentDescription = stringResource(id = R.string.dog_content_description)
            )
            CustomColumnLite {
                val startStopText = if (!isSkiing.value) "Resume" else "Pause"
                CustomCompactChipLite(text = "$startStopText skiing") {
                    toggleSkiing()
                }
                CustomCompactChip("Stop skiing") {
                    toggleSkiing()
                }
            }
        }
        TimeText()
    }*/

// BACKUP FROM THE ABOVE FUNCTION
//    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
//    @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//    @Composable
//    fun StatsApp() {
//        MontblancSkiTrackingTheme {
//            CustomColumn {
//                val hours = activeTime.value.toInt() / 3600
//                val minutes = (activeTime.value.toInt() % 3600) / 60
//                val seconds = (activeTime.value.toInt()) % 60
//
//                CustomStatsText(text = "Active time: ${String.format("%02d:%02d:%02d", hours, minutes, seconds)}")
//                CustomStatsText(text = "Nº runs: ${nRuns.value}")
//                CustomStatsText(text = "Distance: ${distTraveled.value.format(1)} m")
//                CustomStatsText(text = "Elevation: ${deltaElevDown.value.format(1)} m")
//                CustomStatsText(text = "Average speed: ${avgSkiingSpeed.value.format(1)} km/h")
//                CustomStatsText(text = "Top speed: ${topSpeed.value.format(1)} km/h")
//
//                val startStopText = if (!isSkiing.value) "Start" else "Stop"
//                CustomCompactChip(text = "$startStopText skiing") {
//                    toggleSkiing()
//                }
//            }
//        }
//    }

    //----------------------------------------------------------------------------------------
    // GPS/Location Functions

    /**
     * Simple check if GoogleAPI is available. Logs and returns a boolean (just in case).
     */
    private fun isGooglePlayServicesAvailable(): Boolean{
        val availabilityCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        val isAvailable = (availabilityCode == 0)
        Log.d(LOG_TAG, "Google Play Services Available: $isAvailable")
        return isAvailable
    }

    /**
     * Checks location permissions.
     * Returns true if fine location access if permitted.
     *         false otherwise.
     */
    private fun isLocationPermitted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if Location Permissions are enabled.
     * If not prompt user to activate permissions.
     * Otherwise do nothing.
     */
    private fun enableLocation() {

        val isLocationPermitted = isLocationPermitted()
        Log.d(LOG_TAG,"Location permitted: $isLocationPermitted")

        if (!isLocationPermitted) {
            Log.d(LOG_TAG, "Requesting Location permissions")

            val locationPermissionRequest = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        Log.d(LOG_TAG, "OK - Precise location access granted.")
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        Log.d(LOG_TAG, "BAD - Only approximate location access granted.")
                    }
                    else -> {
                        Log.d(LOG_TAG, "BAD - No location access granted.")
                    }
                }
            }

            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))

        }
    }

    private fun getCurrentLocation(locationHandler: (Location) -> Unit) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(LOG_TAG, "Missing location permissions!")
            return
        }

        // locationClient.lastLocation
        locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnCompleteListener {
                Log.d(LOG_TAG, "GetLastLocation isSuccessful: ${it.isSuccessful}")

                if (it.isSuccessful) {
                    val location = it.result
                    if (location == null) {
                        Log.d(LOG_TAG, "BAD - Got null location!")
                    }

                    location?.let {
                        Log.d(LOG_TAG, "Location: $it")
                        locationHandler(location)
                    }
                } else {
                    Log.d(LOG_TAG, "BAD - Getting location failed: ${it.result}")
                }
            }
    }

    private fun startLocationUpdates(intervalMillis: Long, locationListener: LocationListener) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(LOG_TAG, "Missing location permissions!")
            return
        }
        locationClient.requestLocationUpdates(
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis).build(),
            locationListener,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        locationClient.removeLocationUpdates(locationListener)
    }

    //----------------------------------------------------------------------------------------

}

//@Composable
//fun StatsApp() {
//    MontblancSkiTrackingTheme {
//        CustomColumn {
//            CustomText(text = "StatsActivity")
//
//            CustomStatsText(text = "Active time: 00:00:00")
//            CustomStatsText(text = "Nº runs: ${0}")
//            CustomStatsText(text = "Elevation change: +0 m   -0 m")
//            CustomStatsText(text = "Distance traveled: 0 m")
//            CustomStatsText(text = "Current speed: 0 km/h")
//            CustomStatsText(text = "Average speed: 0 km/h")
//            CustomStatsText(text = "Top speed: 0 km/h")
//
//            CustomCompactChip(text = "Button") {}
//        }
//    }
//}
//
//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultStatsPreview() {
//    StatsApp()
//}