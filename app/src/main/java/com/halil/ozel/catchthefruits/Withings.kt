package com.halil.ozel.catchthefruits

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.json.JSONObject
import java.time.Instant
import kotlin.math.max
import kotlin.math.min

class Withings(private val context: Context) {
    private val backend_url = "https://gameconnect-376617.uc.r.appspot.com"
    private val withings_url = "https://wbsapi.withings.net/v2"
    private val user_id = 123456

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getActivity() : Int {
        /**
         * Returns total steps between startdate and now, where
         * startdate is the timestamp of the last seen steps, and
         * now is the current unix timestamp
         */
        var totalSteps = 0
        var newestTimestamp = 0  // timestamp for the most recent response from Withings
        fun calculateSteps(withingsJSONObject: JSONObject) {  // parse steps from JSONObject
            val stepsJSONObject = withingsJSONObject.getJSONObject("body").getJSONObject("series")
            for (key in stepsJSONObject.keys()) {
                totalSteps += (stepsJSONObject[key] as JSONObject).getInt("steps")
                newestTimestamp = max(newestTimestamp, key.toInt())
            }
        }

        val accessToken = getWithingsAccessToken()  // make a GET request to the backend
        val now = Instant.now()  // current unix timestamp
        var startInt = getActivityStartTimestamp()
        var endInt = min(now.epochSecond, Instant.ofEpochSecond(startInt.toLong()).plus(java.time.Duration.ofDays(1)).epochSecond).toInt()  // min(86400 + startdate, now)
        do {
            /**
             * Make requests to Withings with time ranges of at most 24 hours
             * because the Withings API will only return the first 24 hours
             * if startdate and enddate are more than 24 hours apart
             *
             * This do while terminates if
             *  - it has gone all the way up to the current timestamp
             *  - received an error from Withings
             */
            val url = "$withings_url/measure?action=getintradayactivity&meastype=1&startdate=$startInt&enddate=$endInt&data_fields=steps"
            val res = getActivityFromWithings(url, accessToken)  // JSONObject

            if (res.getInt("status") != 0) {
                /**
                 * Response status == 0 is successful, 601 is too many requests
                 * Save the most recent successful timestamp to the database, and
                 * return all the steps
                 */
                updateActivityStartTimestamp(newestTimestamp)  // makes GET request to backend to save timestamp
                return totalSteps
            }

            calculateSteps(res)

            startInt = endInt
            endInt = Instant.ofEpochSecond(endInt.toLong()).plus(java.time.Duration.ofDays(1)).epochSecond.toInt()

        } while (endInt < now.epochSecond.toInt())

        updateActivityStartTimestamp(now.epochSecond.toInt())

        return totalSteps
    }

    suspend fun getLeftoverSteps(): Int {
        val url = "$backend_url/android/get_leftover_steps?user_id=$user_id"
        return makeGetRequest(url).toString().toInt()
    }

    suspend fun saveLeftoverSteps(steps: Int) {
        val url = "$backend_url/android/save_leftover_steps?user_id=$user_id&steps=$steps"
        makeGetRequest(url)
    }

    private suspend fun getActivityFromWithings(url: String, accessToken: String) = suspendCoroutine<JSONObject> { cont ->
        /**
         * This function makes a POST request.
         * The cURL equivalent is:
         * curl --location --request POST $url \
         * --header 'Authorization: Bearer $accessToken'
         * In this programming task, it is used to get intra day activity from Withings
         */
        val queue = Volley.newRequestQueue(context)
        val jsonObjectRequest = object: JsonObjectRequest(
                Request.Method.POST, url, null,
                {response -> // Listener
                    cont.resume(response)  // returns the response. In this case, its type is a JSONObject
                },
                { error -> Log.i("Volley", "Error: $error")}
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $accessToken"
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }

    private suspend fun getWithingsAccessToken(): String {
        // Gets access token from database
        val url = "$backend_url/android/get_withings_access_token?user_id=$user_id"
        return makeGetRequest(url).toString()
    }


    private suspend fun getActivityStartTimestamp(): Int {
        val url = "$backend_url/android/get_timestamp?user_id=$user_id"
        return makeGetRequest(url).toString().toInt()
    }


    private suspend fun updateActivityStartTimestamp(timestamp: Int) {
        val url = "$backend_url/android/save_timestamp?user_id=$user_id&timestamp=$timestamp"
        makeGetRequest(url)
    }


    private suspend fun makeGetRequest(url: String) = suspendCoroutine<Any> { cont ->
        Log.i("makeRequest", "start___\n url: $url")
        val queue = Volley.newRequestQueue(context)
        val stringRequest = StringRequest(Request.Method.GET, url,
                { response ->
                    Log.i("makeRequest", response)  // Response.Listener
                    cont.resume(response)
                },
                { Log.i("makeRequest", "That didn't work") }  // Response.ErrorListener
        )
        queue.add(stringRequest)
    }

}