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

        //开屏动画
        binding.startAppAnimation.apply{
            repeatCount = 0
            playAnimation()
            speed = 1.5f
            addAnimatorListener(object :Animator.AnimatorListener{
                //动画结束后切换页面
                override fun onAnimationEnd(animation: Animator?) {
                    startActivity(Intent(this@MainActivity,PlayerActivity::class.java))
//                    overridePendingTransition(R.anim.alpha_in,R.anim.alpha_out)
                }
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
            })
        }


    }

}