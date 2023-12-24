package com.example.healthgamifylib

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.Duration
import java.util.*
import java.util.concurrent.locks.ReentrantLock

@RequiresApi(Build.VERSION_CODES.O)
open class WindowGoal(
    var targetValue: Int, var observedData: HealthData,
    var start: Date, var window: Duration, val lock: ReentrantLock
) : Subject(), Observer {
    private val timer = Timer()
    var goalAchieved: Boolean = false
    protected var log = false

    // satisfying the Subject contract; can be overridden for more tailored behavior
    override fun notifyObservers() {
        for (o in observers) {
            o(goalAchieved)
        }
    }

    // satisfying the Observer contract; can be overridden for more tailored behavior
    override fun update(value: Any?) {
        if (!goalAchieved && value as Int >= targetValue) {
            goalAchieved = true
            notifyObservers()
        }
    }

    // finalizeGoal can be overridden if player is not penalized for not achieving the goal
    open fun finalizeGoal() {
        if (!goalAchieved) {
            notifyObservers()
        }
    }
    val tag = "Window Goal"
    private inner class StartWindow() : TimerTask() {
        override fun run() {
            lock.lock()
            if (log) {
                Log.i(tag, "====Window Starting ${observedData.name}===")
            }
            goalAchieved = false
            observedData.registerObserver(this@WindowGoal::update)
        }
    }

    private inner class EndWindow() : TimerTask() {
        override fun run() {
            if (log) {
                Log.i(tag, "====Window Ending ${observedData.name}===")
            }
            observedData.removeObserver(this@WindowGoal::update)
            finalizeGoal()
            lock.unlock()
        }
    }

    init {
        if (log) {
            Log.i(tag, "constructor, ${observedData.name}")
        }
        timer.schedule(StartWindow(), start)
        timer.schedule(EndWindow(), window.seconds*1000)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
open class RepeatingWindowGoal(
    var repetitions: Int, var streak: Int = 0, var embeddedWindowGoal: WindowGoal
) : Subject() {
    private val timer: Timer = Timer()
    val goalArray = mutableListOf<Boolean>()
    var repetitionsCompleted = 0
    var currentStreak = 0
    var log = false

    // variables for the embedded WindowGoal
    val targetValue: Int = embeddedWindowGoal.targetValue
    val observedData: HealthData = embeddedWindowGoal.observedData
    private val start: Date = embeddedWindowGoal.start
    val window: Duration = embeddedWindowGoal.window
    val lock: ReentrantLock = embeddedWindowGoal.lock

    private inner class UpdateWindowGoal() : TimerTask() {
        override fun run() {
            goalArray.add(embeddedWindowGoal.goalAchieved)
            if (log) {
                Log.i("Repeating", "goalArray: ${goalArray.toString()}")
            }
            repetitionsCompleted++
            if (embeddedWindowGoal.goalAchieved) {
                currentStreak++
                if (currentStreak >= streak){
                    notifyObservers()
                }
            } else {
                currentStreak = 0
            }
            if (goalArray.size < repetitions) {
                // keep track of the embeddedWindowGoal's observers before the embeddedWindowGoal closes
                val embeddedWindowGoalObservers = embeddedWindowGoal.observers.toMutableList()

                // create a new embeddedWindowGoal
                embeddedWindowGoal = WindowGoal(targetValue, observedData, Date(), window, lock)
                for (o in embeddedWindowGoalObservers) {
                    embeddedWindowGoal.registerObserver(o)
                }
            }
            else {
                if (log) {
                    Log.i(
                        "RepeatingWindowGoal",
                        "repetitions completed.\n Goals: ${goalArray.toString()}"
                    )
                }
            }
        }
    }

    init {

        /**
         * `observers` should observe the RepeatingWindowGoal; it only cares about the currentStreak
         * This version assumes that the embeddedWindowGoal's observers are registered elsewhere
         * It is the user's responsibility to register the embeddedWindowGoal's observers
         * Therefore the following 3 lines of code is commented out
         for (o in observers) {
            embeddedWindowGoal.registerObserver(o)
        }
         */
        timer.schedule(UpdateWindowGoal(), Date(start.time + window.seconds * 1000), window.seconds * 1000)
    }

    override fun notifyObservers() {
        for (o in observers) {
            o(currentStreak)
        }
    }
}
