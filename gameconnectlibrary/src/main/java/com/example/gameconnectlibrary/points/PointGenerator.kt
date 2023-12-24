package com.example.gameconnectlibrary.points

import android.util.Log
import com.example.gameconnectlibrary.crud.Connection
import com.example.healthgamifylib.Observer
import com.example.healthgamifylib.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * The PointGenerator queries the database to see how many coins or lives it is going to generate
 * when its update function is called
 */
abstract class PointGenerator(val goalName: String, val connection: Connection): Observer, CoroutineScope,
    Subject() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    abstract suspend fun getConfig() : Any?  // a hook, get the number of points to generate
    override fun update(value: Any?) {
        Log.i("point generator", "Update Called!!!!")
        Log.i("point generator", "Update Called!!!!")
        Log.i("point generator", "Update Called!!!!")
        Log.i("point generator", "Update Called!!!!")
        notifyObservers()
    }

    override fun notifyObservers() {
        scope.launch {
            val pointToGenerate = getConfig()
            Log.i("point generator", "$goalName, pointToGenerate: $pointToGenerate")
            for (observer in observers) {
                observer(pointToGenerate)
            }
        }
    }
}

class CoinGenerator(goalName: String, connection: Connection,
                    override val coroutineContext: CoroutineContext
) : PointGenerator(goalName, connection) {
    // the observers of a CoinGenerator are Coins

    override suspend fun getConfig(): Any {
        Log.i("Coin Generator", "get config called!!!!")
        // calls the database by doing something like connection.readJsonData
        val res = connection.readJsonData(key = goalName)
        res["coin"]::class.simpleName?.let { Log.i("Coin Generator got readJsonData", it) }
        return (res["coin"].toString().toInt())
    }
}


class LifeGenerator(goalName: String, connection: Connection,
                    override val coroutineContext: CoroutineContext
) : PointGenerator(goalName, connection) {
    // the observers of a CoinGenerator are Coins

    override suspend fun getConfig(): Any {
        Log.i("Life Generator", "get config called!!!!")
        // calls the database by doing something like connection.readJsonData
        val res = connection.readJsonData(key = goalName)
        res["life"]::class.simpleName?.let { Log.i("Life Generator got readJsonData", it) }
        return (res["life"].toString().toInt())
    }
}
