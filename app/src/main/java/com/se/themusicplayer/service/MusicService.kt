package com.se.themusicplayer.service

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Message
import com.se.themusicplayer.App
import com.se.themusicplayer.ui.player.PlayerActivity
import kotlin.concurrent.thread

/**
 * 提供播放音乐服务
 * 提供监控播放进度服务
 */
class MusicService : Service() {
    //是否同步进度
    private var isUpdateProcess = false

    //音乐控制器
    private val mBinder by lazy { MusicPlayBinder() }
    inner class MusicPlayBinder:Binder(){
        /** 播放 */
        fun play(handler:Handler){
            if (!mediaPlayer.isPlaying) mediaPlayer.start()
            isUpdateProcess = true
            updateProgress(handler)
        }
        /** 暂停 */
        fun pause(){
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
            isUpdateProcess = false
        }
        /** 更新时长 */
        fun updateDuration(handler:Handler){
            App.currentDuration = mediaPlayer.duration
            handler.sendEmptyMessage(PlayerActivity.UPDATE_DURATION)
        }
        /** 更新进度 */
        private fun updateProgress(handler:Handler){
            thread {
                while (isUpdateProcess){
                    println("同步中")
                    // TODO: 优化:暂停后不再同步
                    App.currentMs = mediaPlayer.currentPosition
                    Message().let{
                        it.what = PlayerActivity.UPDATE_MUSIC_PROGRESS
                        handler.sendMessage(it)
                    }
                    Thread.sleep(100)
                }
            }
        }
    }

    //音乐播放器
    private lateinit var mediaPlayer :MediaPlayer

    override fun onCreate() {
        mediaPlayer = MediaPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: 更改歌曲载入方式
        if (!App.musicList.isEmpty()) {
            //状态音乐
            assets.openFd(App.currentSongData!!.src).let {
                mediaPlayer.setDataSource(it.fileDescriptor, it.startOffset, it.length)
                mediaPlayer.prepare()
                App.currentDuration = mediaPlayer.duration
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent) = mBinder

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        println("服务对象被销毁")
    }
}