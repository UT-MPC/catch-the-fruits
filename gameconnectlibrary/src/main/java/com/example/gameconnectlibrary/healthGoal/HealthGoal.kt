package com.example.gameconnectlibrary.healthGoal

import android.util.Log
import com.example.healthgamifylib.HealthData
import com.example.healthgamifylib.WindowGoal
import kotlinx.coroutines.CoroutineScope
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

/**
 * The sensing integrator will create a HealthGoal object.
 * The HealthGoal object will get the targetValue from the DB:
 * Then when the update function is called, if checks if value meets targetValue
 * If so, it calls notifyObservers(), which will tell the observers that goalAchieved.
 * The observers in this case will then turn goals into lives and coins.
 * The timer's only job is to turn goalAchieved to False
 * Health Goal observes health data
 */
class HealthGoal(
    goal: Goal,
    observedData: HealthData,
    lock: ReentrantLock, override val coroutineContext: CoroutineContext
) : WindowGoal(goal.targetValue, observedData,
    start=Date(goal.windowStartTime),
    window=goal.windowSize.toDuration(DurationUnit.MILLISECONDS).toJavaDuration(), lock
), CoroutineScope {
    init {
        log = true
    }

    override fun finalizeGoal() {
        Log.i("Health Goal", "Finalize goal")
    }
}