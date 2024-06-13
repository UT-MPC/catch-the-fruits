/**
 * This version of Catch the Fruits connects to Withings directly to get steps,
 * and then calculate the amount of lives from those steps,
 * saves the leftover steps (i.e. total steps % stepsPerLife) in MongoDB,
 * saves the end_time timestamp of the Withings query in MongoDB
 */
package com.halil.ozel.catchthefruits

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.halil.ozel.catchthefruits.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    protected lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private lateinit var binding: ActivityMainBinding
    var score: Int = 0
    var imageArray = ArrayList<ImageView>()
    var handler: Handler = Handler(Looper.getMainLooper())
    var runnable: Runnable = Runnable { }
    private lateinit var withings: Withings

    val stepsPerLives = 1000  // get 1 life every 1000 steps
    private var lives = 0
    private var leftoverSteps = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        job = Job()

        withings = Withings(applicationContext)

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.catchFruits = this

        launch {
            // Todo: change the get activity to only get enough for one life
            // Todo: and put the for loop here?
            val steps = withings.getActivity() + withings.getLeftoverSteps()
            Log.i("Main Activity", "steps: $steps")
            lives = steps / stepsPerLives
            leftoverSteps = steps % stepsPerLives



            binding.score = getString(R.string.score_0)

            score = 0

            imageArray = arrayListOf(
                    binding.ivApple,
                    binding.ivBanana,
                    binding.ivCherry,
                    binding.ivGrapes,
                    binding.ivKiwi,
                    binding.ivOrange,
                    binding.ivPear,
                    binding.ivStrawberry,
                    binding.ivWatermelon
            )


            hideImages()

            playAndRestart()
        }
    }

    override fun onStop() {
        /**
         * Turn leftover lives into leftover steps
         */
        super.onStop()  // IDE says onStop should call super. TODO: check why

        // leftoverSteps was steps % stepsPerLives
        leftoverSteps += lives * stepsPerLives  // add unused lives back
        Log.i("onStop", "leftover steps = $leftoverSteps")
        launch { withings.saveLeftoverSteps(steps = leftoverSteps) }
    }


    private fun hideImages() {
        runnable = Runnable {
            for (image in imageArray) {
                image.visibility = View.INVISIBLE
            }
            val random = Random()
            val index = random.nextInt(8 - 0)
            imageArray[index].visibility = View.VISIBLE
            handler.postDelayed(runnable, 500)
        }
        handler.post(runnable)
    }


    @SuppressLint("SetTextI18n")
    fun increaseScore() {
        score++
        binding.score = "Score : $score"
    }

    @SuppressLint("SetTextI18n")
    fun playAndRestart() {
        Toast.makeText(this, "$lives lives left", Toast.LENGTH_LONG).show()
        if (lives <= 0) {  // not enough lives
            Toast.makeText(this, "Not enough lives, bye", Toast.LENGTH_LONG).show()
            return
        }
        else {  // deduct one life, and start playing
            lives--
        }
        score = 0
        binding.score = "Score : $score"
        hideImages()
        binding.time = "Time : " + 10000 / 1000

        for (image in imageArray) {
            image.visibility = View.INVISIBLE
        }

        object : CountDownTimer(10000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                binding.time = "Time's up!!!"
                handler.removeCallbacks(runnable)

                val dialog = AlertDialog.Builder(this@MainActivity).apply {
                    setCancelable(false)
                    setTitle(getString(R.string.game_name))
                    setMessage("Your score : $score\nWould you like play again?")
                }
                dialog.setPositiveButton(getString(R.string.yes)) { _, _ ->
                    playAndRestart()
                }
                    .setNegativeButton(getString(R.string.no)) { _, _ ->
                        score = 0
                        ("Score : $score").apply { binding.score = this }
                        ("Time : " + "0").apply { binding.time = this }

                        for (image in imageArray) {
                            image.visibility = View.INVISIBLE
                        }
                        finish()
                    }
                 dialog.create().apply {
                    show()
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onTick(p0: Long) {
                binding.time = getString(R.string.time) + p0 / 1000
            }
        }.start()
    }


}
