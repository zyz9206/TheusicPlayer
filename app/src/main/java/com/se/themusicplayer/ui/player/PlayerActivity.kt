package com.se.themusicplayer.ui.player

import android.animation.Animator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.se.themusicplayer.R
import com.se.themusicplayer.databinding.ActivityPlayerBinding
import com.se.themusicplayer.ui.dialog.PlaylistDialog

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPlayerBinding
    companion object{
        val REPEAT_PLAY = 0
        val RANDOM_PLAY = 1
        val SEQUENCE_PLAY =2
    }
    //暂停否
    private var isPause = false
    //循环模式
    private var repeatMode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()



        binding.listOfSongs.apply {
            setOnClickListener {
                PlaylistDialog().show(supportFragmentManager,null)
            }
        }
        binding.playMode.apply{
            setOnClickListener {
                repeatMode = (repeatMode + 1) % 3
                when (repeatMode) {
                    0 -> R.drawable.ic_repeat_play
                    1 -> R.drawable.ic_random_play
                    else -> R.drawable.ic_sequence_play
                }.let { setImageResource(it) }
            }
        }
        binding.nextSong.apply {
            speed = 2f
            setOnClickListener { playAnimation() }
        }
        binding.previousSong.apply {
            speed = 2f
            setOnClickListener { playAnimation() }
        }
        binding.musicPlay.apply{
            speed = 2f
            setOnClickListener {
                if (isPause) resumeAnimation()
                else playAnimation()
            }
            addAnimatorUpdateListener {
                println(isPause)
                if(!isPause && it.animatedFraction > 0.5f) {
                    isPause = !isPause
                    pauseAnimation()
                }
            }
            addAnimatorListener(object: Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isPause = false
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }

            })
        }
    }

}