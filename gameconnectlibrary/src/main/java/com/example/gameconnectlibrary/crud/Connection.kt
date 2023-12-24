package com.example.gameconnectlibrary.crud

import org.json.JSONObject

interface Connection {
    fun updateData(key: String, value: Int)
    fun updateData(value: HashMap<String, String>)
    suspend fun readData(key: String) : Int
    suspend fun readJsonData(key: String) : JSONObject
    fun deleteData(key: String)
}