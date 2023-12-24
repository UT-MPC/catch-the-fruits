package com.example.gameconnectlibrary.healthGoal

import org.json.JSONObject

class Goal(
    var goalName: String = "",
    var targetValue: Int = 0,
    var windowStartTime: Long = 0,
    var windowSize: Long = 0,
    private var goalAchieved: Boolean = false
) {

    fun toHashMap(): HashMap<String, String> {
        return hashMapOf(
            Pair("goalName", goalName),
            Pair("targetValue", targetValue.toString()),
            Pair("windowStartTime", windowStartTime.toString()),
            Pair("windowSize", windowSize.toString()),
            Pair("goalAchieved", goalAchieved.toString())
        )
    }

    constructor(jsonObject: JSONObject) : this() {
        goalName = jsonObject["goalName"] as String
        targetValue = jsonObject["targetValue"].toString().toInt()
        windowStartTime = jsonObject["windowStartTime"].toString().toLong()
        windowSize = jsonObject["windowSize"].toString().toLong()
        goalAchieved = jsonObject["goalAchieved"].toString().toBoolean()
    }

    override fun toString(): String {
        return "Goal(goalName=$goalName, targetValue=$targetValue, windowStartTime=$windowStartTime," +
                " windowSize=$windowSize, goalAchieved=$goalAchieved)"
    }
}