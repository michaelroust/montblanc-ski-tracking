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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Devices
import androidx.core.app.ActivityCompat
import com.github.michaelroust.montblanc_ski_tracking.presentation.theme.MontblancSkiTrackingTheme
import com.github.michaelroust.montblanc_ski_tracking.presentation.utilities.Globals.LOG_TAG
import com.github.michaelroust.montblanc_ski_tracking.presentation.utilities.Ticker
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

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

    lateinit var activeTimeTicker: Ticker
    lateinit var locationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneLapStats()
        }

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
        isSkiing.value = true
        activeTimeTicker.start()
    }

    private fun stopSkiing() {
        isSkiing.value = false
        activeTimeTicker.stop()
        nRuns.value++
    }

    //---------------------------------------------------------------------------------------
    // Location listener code
    //      All code here is related to updating all statistics related to Location data.
    //      The locationListener is meant to be called at regular intervals with new Location data.

    var prevLocationBuffer: Location? = null

    var speedTimeSum: Double = 0.0
    var timeSum: Double = 0.0

    private val locationListener = LocationListener { location ->

        if (!isSkiing.value) {
            // Set Previous Location to null. Needed due to design of average speed computation.
            prevLocationBuffer = null
        }
        val prevLocation = prevLocationBuffer

        // Update current speed
        curSpeed.value = location.speed * 3.6

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

        Log.d(LOG_TAG, "Location Listener: $location")
    }

    //----------------------------------------------------------------------------------------
    // UI

    private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
    @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
    @Composable
    fun OneLapStats() {
        MontblancSkiTrackingTheme {
            CustomColumn {
                val hours = activeTime.value.toInt() / 3600
                val minutes = (activeTime.value.toInt() % 3600) / 60
                val seconds = (activeTime.value.toInt()) % 60
                //ADD CURRENT WATCH TIME
                CustomStatsText(text = "Distance: ${distTraveled.value.format(1)} m")
                CustomStatsText(text = "Elevation: ${deltaElevDown.value.format(1)} m")
                CustomStatsText(text = "Average speed: ${avgSkiingSpeed.value.format(1)} km/h")
                CustomStatsText(text = "Top speed: ${topSpeed.value.format(1)} km/h")

                val startStopText = if (!isSkiing.value) "Start" else "Stop"
                CustomCompactChip(text = "$startStopText skiing") {
                    toggleSkiing()
                }
            }
        }
    }

    @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
    @Composable
    fun TransitionAllLapsStats() {
        MontblancSkiTrackingTheme {
            CustomColumn {
                CustomInfoText(text = "Statistics over all")
                CustomLapsText(text = "${nRuns.value} laps")
            }
        }
    }


    @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
    @Composable
    fun AllLapsStats() {
        MontblancSkiTrackingTheme {
            CustomColumn {
                val hours = activeTime.value.toInt() / 3600
                val minutes = (activeTime.value.toInt() % 3600) / 60
                val seconds = (activeTime.value.toInt()) % 60

                CustomStatsText(text = "Distance: ${distTraveled.value.format(1)} m")
                CustomStatsText(text = "Elevation: ${deltaElevDown.value.format(1)} m")
                CustomStatsText(text = "Average speed: ${avgSkiingSpeed.value.format(1)} km/h")
                CustomStatsText(text = "Top speed: ${topSpeed.value.format(1)} km/h")
                CustomStatsText(text = "Active time: ${String.format("%02dº:%02d'':%02d'", hours, minutes, seconds)}")

            }
        }
    }

    @Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
    @Composable
    fun FinishRun() {
        MontblancSkiTrackingTheme {
            CustomColumn {
                CustomCompactChip("Stop skiing") {
                    toggleSkiing()
                }
            }
        }
    }

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