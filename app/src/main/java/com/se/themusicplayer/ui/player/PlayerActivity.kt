package com.se.themusicplayer.ui.player

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.se.themusicplayer.databinding.ActivityPlayerBinding
import com.se.themusicplayer.ui.dialog.PlaylistDialog

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPlayerBinding
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
    }

}