package com.github.michaelroust.montblanc_ski_tracking.presentation.utilities

import android.os.Handler

/**
 * Simple class to regularly call a given function
 */
class Ticker(val onTick: () -> Unit, private val delayMillis: Long) {

    private val handler = Handler()
    private var running = false

    fun start() {
        running = true
        handler.postDelayed({ runTicker() }, delayMillis)
    }

    fun stop() {
        running = false
    }

    private fun runTicker() {
        if (running) {
            onTick()
            handler.postDelayed({ runTicker() }, delayMillis)
        }
    }
}