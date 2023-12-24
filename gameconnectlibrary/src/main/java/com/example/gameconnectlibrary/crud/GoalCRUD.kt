package com.example.gameconnectlibrary.crud

import com.example.gameconnectlibrary.healthGoal.Goal

interface GoalCRUD {
    suspend fun setGoal(goal: Goal)
    suspend fun getGoal(goalName: String): Goal
}