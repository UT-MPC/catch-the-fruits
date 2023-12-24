package com.example.healthgamifylib

interface Observer { //subscriber
    /**
     * The Observer interface, aka the subscriber.
     * The update function is called by the Subject that this Observer is observing
     */
    fun update(value: Any?) : Unit
}

abstract class Subject {
    var observers: MutableList<(Any?) -> Unit> = mutableListOf()

    fun registerObserver(whatToCall: (Any?) -> Unit) : Unit {
        observers.add(whatToCall)
    }
    fun removeObserver(whatNotToCall: (Any?) -> Unit) : Unit{
        observers.remove(whatNotToCall)
    }
    abstract fun notifyObservers() : Unit
}