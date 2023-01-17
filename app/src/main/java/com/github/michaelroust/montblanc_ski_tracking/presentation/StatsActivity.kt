/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.github.michaelroust.montblanc_ski_tracking.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.*
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

//UN-COMMENT THE THREE LINES BELOW
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.Height

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
        // Interval in milliseconds at which GPS location is polled
        const val GPS_LOCATION_INTERVAL_MILLIS = 1000L
    }

    // Mutable State Holders.
    // Changes to these values are directly propagated to the UI.
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

    // Other object fields.
    lateinit var activeTimeTicker: Ticker
    lateinit var locationClient: FusedLocationProviderClient
    lateinit var vibrator: Vibrator

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
        // Vibration setup

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

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

    //------------------------------------------------------------------------
    // Vibrations

    private fun vibrate(double: Boolean) {
        if (!double)
            posVibation()
        else
            negVibation()
    }

    val smallVibrationTime = 100L
    val bigVibrationTime = 300L

    private fun posVibation() {
        if (!recentVibration) {
            vibrator.vibrate(smallVibrationTime)
            Handler().postDelayed({vibrator.vibrate(bigVibrationTime)}, 200)
        }

        recentVibration = true
        Handler().postDelayed({recentVibration = false}, 3000)
    }

    private fun negVibation() {

        if (!recentVibration) {
            vibrator.vibrate(smallVibrationTime)

            Handler().postDelayed({
                vibrator.vibrate(smallVibrationTime)
                Handler().postDelayed({
                    vibrator.vibrate(bigVibrationTime)
                    Handler().postDelayed({
                        vibrator.vibrate(smallVibrationTime)

                        Handler().postDelayed({
                            vibrator.vibrate(smallVibrationTime)
                            Handler().postDelayed({
                                vibrator.vibrate(smallVibrationTime)
                                Handler().postDelayed({
                                    vibrator.vibrate(bigVibrationTime)
                                    Handler().postDelayed({
                                        vibrator.vibrate(smallVibrationTime)
                                    }, 200)
                                }, 400)
                            }, 200)
                        }, 600)
                                          }, 200)
                                      }, 400)
                                  }, 200)
        }

        recentVibration = true
        Handler().postDelayed({recentVibration = false}, 3000)
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

    var recentVibration: Boolean = false

    private val locationListener = LocationListener { location ->

        Log.d(LOG_TAG, "isSkiing: $isSkiing \tLocation Listener: $location")

        // Update current speed
        curSpeed.value = location.speed * 3.6

        if (!isSkiing.value) {
            // Set Previous Location to null. Needed due to design of average speed computation.
            if (curSpeed.value >= 20) {
                startSkiing()
            }

            prevLocationBuffer = null
        } else {

            if (curSpeed.value < 10) {
                stopSkiing()
            }
        }
        val prevLocation = prevLocationBuffer

        if (curSpeed.value > 30 && curSpeed.value < 31) {
            vibrate(false)
        }

        if (curSpeed.value > 60 && curSpeed.value < 61) {
            vibrate(true)
        }

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
                distTraveled.value += prevLocation.distanceTo(location)*0.001 //DOUBLECHECK
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

    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)


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

            // Vibration debug button 1
            Button(
                modifier = Modifier
                    .alpha(sideButtonAlpha)
                    .width(this.maxWidth - sideButtonsWidth * 2)
                    .height(this.maxHeight / 2)
                    .align(Alignment.TopCenter),
                onClick = {vibrate(false)}) {}

            // Vibration debug button 2
            Button(
                modifier = Modifier
                    .alpha(sideButtonAlpha)
                    .width(this.maxWidth - sideButtonsWidth * 2)
                    .height(this.maxHeight / 2)
                    .align(Alignment.BottomCenter),
                onClick = {vibrate(true)}) {}


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
                    MainStatsComposable(false)
                else if (selectedPage == 1) {
                    LapsStatsComposable()
                } else if (selectedPage == 2) {
                    MainStatsComposable(true)
                }
            }
            HorizontalPageIndicator(
                modifier = Modifier.padding(4.dp),
                pageIndicatorState = pageIndicatorState
            )
        }
    }


    @Composable
    fun LapsStatsComposable() {
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
                    Icons.Rounded.Replay, //UN-COMMENT THIS LINE
                    //Icons.Rounded.Search, //COMMENT THIS LINE
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
    fun MainStatsComposable(showTotals: Boolean) {

        val activeTimeText = formatTime((if (!showTotals) activeTime else totalActiveTime).value.toInt())
        val distTraveledText = "${(if (!showTotals) distTraveled else totalDistTraveled).value.format(1)} km"
        val avgSpeedText = (if (!showTotals) avgSkiingSpeed else totalAvgSkiingSpeed).value.format(1)
        val topSpeedText = (if (!showTotals) topSpeed else totalTopSpeed).value.format(1)
        val elevText = "${(if (!showTotals) deltaElevDown else totalDeltaElevDown).value.format(0)} m"

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            fontSize = 14.sp,
            text = activeTimeText
        )


        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.width(28.dp))
            Icon(
                //Icons.Rounded.Search, //COMMENT THIS LINE
                Icons.Rounded.Landscape, //UN-COMMENT THIS LINE
                "Localized description",
                modifier = Modifier.align(CenterVertically)
            )
            Spacer(Modifier.width(4.dp))
            CustomStatsTopBottomText(distTraveledText)
        }

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
                        fontSize = 24.sp,
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
                        fontSize = 24.sp,
                        color = MaterialTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                        text = topSpeedText
                    )
                }

                CustomInfoText(text = "TOP km/h")
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.width(38.dp))
            Icon(
                //Icons.Rounded.Search, //COMMENT THIS LINE
                Icons.Rounded.Height, //UN-COMMENT THIS LINE
                "Localized description",
                modifier = Modifier.align(CenterVertically)
            )
            Spacer(Modifier.width(4.dp))
            CustomStatsTopBottomText(elevText)
        }
        //CustomStatsTopBottomText(elevText)
    }


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

    /**
     * Requests a new location update. Calls `locationHandler` with received Location.
     */
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

    /**
     * Initiate location updates. Calling given locationListener every time a new update is
     * received. Update is requested every `intervalMillis` milliseconds.
     */
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
