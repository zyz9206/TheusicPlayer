package com.se.themusicplayer

import android.animation.Animator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.se.themusicplayer.databinding.ActivityMainBinding

import com.se.themusicplayer.ui.player.PlayerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.startAppAnimation.apply{
            repeatCount = 0
            playAnimation()
            speed = 1f
            addAnimatorListener(object :Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    Intent(this@MainActivity,PlayerActivity::class.java).let{
                        startActivity(it)
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {
                }
                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
        }


    }

}