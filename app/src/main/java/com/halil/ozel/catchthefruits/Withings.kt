package com.halil.ozel.catchthefruits

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.json.JSONObject


class Withings(private val context: Context) {
    private val backend_url = "https://gameconnect-376617.uc.r.appspot.com"
    private val withings_url = "https://wbsapi.withings.net/v2"

    suspend fun getActivity() : Int {
        /**
         * Returns total steps between startdate and now, where
         * startdate is the timestamp of the last seen steps
         */
        val access_token = get_withings_access_token()
        val startdate="1712642400"  // todo: get this from the backend
        val enddate="1712728800"  // todo: min(86400 + startdate, now)
        val url = "$withings_url/measure?action=getintradayactivity&meastype=1&startdate=$startdate&enddate=$enddate&data_fields=steps"

        val res = getActivityFromWithings(url, access_token)  // JSONObject

        if (res.getInt("status") != 0) {  // Response status == 0 is successful
            Log.i("activity", "status != 0")
            return 0
        }

        val steps = res.getJSONObject("body").getJSONObject("series")
        var totalSteps = 0
        for (key in steps.keys()) {
           totalSteps += (steps[key] as JSONObject).getInt("steps")
        }

        // todo: replace the startdate in the DB with enddate

        return totalSteps
    }

    private suspend fun getActivityFromWithings(url: String, accessToken: String) = suspendCoroutine<JSONObject> { cont ->
        val queue = Volley.newRequestQueue(context)
        val jsonObjectRequest = object: JsonObjectRequest(
                Request.Method.POST, url, null,
                {response -> Log.i("Volley", "Response: $response")
                    cont.resume(response)
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

    private suspend fun get_withings_access_token(): String {
        // Gets access token from database
        val url = "$backend_url/android/get_withings_access_token?user_id=123456"
        return makeGetRequest(url).toString()
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

    // Todo: def getStartTimestamp

    // Todo: def updateStartTimestamp

}