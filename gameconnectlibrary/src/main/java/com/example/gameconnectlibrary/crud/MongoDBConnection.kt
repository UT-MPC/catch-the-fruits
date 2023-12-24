package com.example.gameconnectlibrary.crud

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class ConnectionURL(val backendURL: String, val user_id: Int) {
    abstract fun updateURL(key: String, value: Int) : String
    abstract fun readURL(key: String) : String
}

class GameConnectionURL(backendURL: String, user_id: Int) : ConnectionURL(backendURL, user_id) {
    override fun updateURL(key: String, value: Int): String {
        return "$backendURL/insert_point?point_type=$key&amount=$value&user_id=$user_id"
    }

    override fun readURL(key: String): String {
        return "$backendURL/read_point?point_type=$key&user_id=$user_id"
    }
}

class GoalConnectionURL(backendURL: String, user_id: Int) : ConnectionURL(backendURL, user_id) {
    override fun updateURL(key: String, value: Int): String {
        return "$backendURL/insert_goal"
    }

    override fun readURL(key: String): String {
        return "$backendURL/read_goal?goal_name=$key"
    }
}


class GoalToPointsRuleURL(backendURL: String, user_id: Int) : ConnectionURL(backendURL, user_id) {
    override fun updateURL(key: String, value: Int): String {
        return "$backendURL/create_rule"
    }

    override fun readURL(key: String): String {
        return "$backendURL/read_rule?goal=$key"
    }

    fun rulesHashMap(goalName: String, coinsPerGoal: String, livesPerGoal: String) : HashMap<String, String> {
        return hashMapOf(
            Pair("goal", goalName),
            Pair("coin", coinsPerGoal),
            Pair("life", livesPerGoal)
        )
    }

}


class HealthDataConnectionURL(backendURL: String, user_id: Int) : ConnectionURL(backendURL, user_id) {
    override fun updateURL(key: String, value: Int): String {
        TODO("Not yet implemented")
    }

    override fun readURL(key: String): String {
        TODO("Not yet implemented")
    }
}


class MongoDBConnection(val context: Context, private val connectionURL: ConnectionURL): Connection{
    private val TAG = "MongoDB Connection"
    private fun makeRequest(url: String) {
        val queue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                Log.i("makeRequest", response)  // Response.Listener
            },
            { Log.i("makeRequest", "That didn't work") }  // Response.ErrorListener
            )

        queue.add(stringRequest)
    }
    private fun makePostRequest(url: String, value: HashMap<String, String>) {
        Log.i("makePostRequest", "url: $url")
        val queue = Volley.newRequestQueue(context)
        val stringRequest = object: StringRequest(
            Method.POST, url, { response ->
            Log.i("makePostRequest", response)  // Response.Listener
        },
            { Log.i("makePostRequest", "That didn't work") }) {

            override fun getParams(): MutableMap<String, String> {
                Log.i("makePostRequest", value.toString())
                return value
            }
        }
        Log.i("makePostRequest: string request", stringRequest.toString())
        queue.add(stringRequest)
    }

    override fun updateData(key: String, value: Int) {
        /**
        * This function updates the value with the key by value.
         * Calls the makeRequest, uses GET
         * For example, if the value is positive, the value in the DB will increase;
         * if the value is negative, the value in the DB will decrease
         * If key doesn't exist, a new entry will be created.
         */
        // val url = "$backendURL/insert_point?point_type=$key&amount=$value&user_id=$user_id"
        // val url = connectionURL.updateURL(key, value)
         makeRequest(url = connectionURL.updateURL(key, value))
        // makePostRequest(url = "https://gameconnect-376617.uc.r.appspot.com/insert_data", value = hashMapOf())
    }

    override fun updateData(value: HashMap<String, String>) {
        Log.i(TAG, "update Post Data")
        /**
         * This function updates the value with the key by value.
         * If the type of the value is not an integer, and is a HashMap instead, use this function
         * Calls the makePostRequest, uses POST
         */
        makePostRequest(url = connectionURL.updateURL("",0), value = value)
    }

    override suspend fun readData(key: String) = suspendCoroutine<Int> { cont ->
        /**
         * This function will return the value read from MongoDB
         * code from https://stackoverflow.com/a/53486222
         */
        Log.i(TAG, "called readData")
        // val url = "$backendURL/read_point?point_type=$key&user_id=$user_id"
        val url = connectionURL.readURL(key)
        val queue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                Log.i("$TAG, readData", "response is $response")
                cont.resume(response.toInt())
            },
            {
                Log.i("$TAG, readData", "Something went wrong in readData")
                cont.resume(-1) }
            )
        queue.add(stringRequest)
    }

    override suspend fun readJsonData(key: String) = suspendCoroutine<JSONObject> { cont ->
        Log.i(TAG, "called readJsonData")
        val url = connectionURL.readURL(key)
        val queue = Volley.newRequestQueue(context)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
            Log.i("$TAG, readJsonData", "Response: %s".format(response.toString()))
                cont.resume(response)
        },
            { error ->
                Log.i("$TAG, readJsonData", "error: $error")
            })
        queue.add(jsonObjectRequest)
    }

    override fun deleteData(key: String) {
        TODO("Not yet implemented")
    }
}