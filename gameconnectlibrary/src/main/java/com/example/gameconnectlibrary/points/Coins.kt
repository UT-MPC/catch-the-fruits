package com.example.gameconnectlibrary.points

import com.example.gameconnectlibrary.crud.GameDataCRUD
import com.example.healthgamifylib.Points
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class Coins(maxValue: Int = Int.MAX_VALUE, private val gameDataCRUD: GameDataCRUD,
            override val coroutineContext: CoroutineContext
) : Points(maxValue), CoroutineScope {
    /**
     * The Coins class derives from the abstract Points class from GamifyHealth
     * The sensor integrator is responsible for making it observe a Goal object
     */
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    override fun update(value: Any?) {
        scope.launch { gameDataCRUD.incrementCoins(value as Int) }
    }

    fun consumeCoins(value: Int) {
        scope.launch { gameDataCRUD.decrementCoins(value) }
    }

    suspend fun currentAmount() : Int {
        return gameDataCRUD.readData("coins")
    }
}