package com.example.gameconnectlibrary.crud

/**
 * Interface to access game records, e.g. coins or lives
 */
interface GameDataCRUD {

    private suspend fun incrementOrDecrement(key: String, delta: Int) {
        /**
         * change the original value to value + delta
         */
        if (delta == 0) {
            return
        }
        val oldValue = readData(key)
        val newValue = oldValue + delta

        updateData(key, newValue)
    }

    suspend fun incrementCoins(increment: Int = 1) {
       incrementOrDecrement(key = "coins", increment)
    }

    suspend fun decrementCoins(decrement: Int = 1) {
        incrementOrDecrement(key = "coins", -decrement)
    }

    suspend fun incrementLives(increment: Int = 1) {
        incrementOrDecrement(key = "lives", increment)
    }

    suspend fun decrementLives(decrement: Int = 1) {
        incrementOrDecrement(key = "lives", -decrement)
    }

    suspend fun updateData(key: String, value: Int)

    suspend fun readData(key: String): Int

    fun deleteData(key: String)
}