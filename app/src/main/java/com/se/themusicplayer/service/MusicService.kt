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
import androidx.lifecycle.Observer
import com.se.themusicplayer.App
import com.se.themusicplayer.ui.player.PlayerActivity
import kotlin.concurrent.thread

/**
 * 提供播放音乐服务
 * 提供监控播放进度服务
 */
class MusicService: Service() {
    /** 触发装载 */
    private val loadTrigger = Observer<Int?> {
        loadMusic()
    }
    /** 是否装载了资源 */
    private var isPrepared = false

    /** 音乐控制器 */
    private val mBinder by lazy { MusicPlayBinder() }
    inner class MusicPlayBinder:Binder(){
        /** 是否装载了资源 */
        fun isPrepared() = isPrepared
        /** 是否在播放 */
        fun isPlaying() = mediaPlayer.isPlaying

        /** 获取歌曲进度 */   // TODO: 不清楚装载好音乐但从未播放算不算isPlaying
        fun getProgress():Int? = if (isPrepared) mediaPlayer.currentPosition else null
        /** 获取歌曲时长 */   // TODO: 不清楚装载好音乐但从未播放算不算isPlaying
        fun getDuration():Int? = if (isPrepared) mediaPlayer.duration else null


        /** 播放 */
        fun start(){
            if (isPrepared && !mediaPlayer.isPlaying) mediaPlayer.start()
        }
        /** 暂停 */
        fun pause(){
            if (isPrepared && mediaPlayer.isPlaying) mediaPlayer.pause()
        }
        /** 跳转 */
        fun seekTo(ms:Int){
            if (isPrepared) mediaPlayer.seekTo(ms)
        }
        /** 上一首 */
        fun previous(){
            val state = mediaPlayer.isPlaying
            mediaPlayer.reset()
            App.musicList.previous()
            if (state) mediaPlayer.start()
        }
        /** 下一首 */
        fun next(){
            val state = mediaPlayer.isPlaying
            mediaPlayer.reset()
            App.musicList.next()
            if (state) mediaPlayer.start()
        }
    }
    /** 音乐播放器 */
    private lateinit var mediaPlayer :MediaPlayer


    /**
     * 装载音乐
     * @since 每次MusicList.currentSongIndex改变时就会重新装载
     */
    private fun loadMusic(){
        if (!App.musicList.isEmpty()) {
            // TODO: 更改歌曲载入方式
            assets.openFd(App.musicList.currentSongData!!.src).let {
                mediaPlayer.setDataSource(it.fileDescriptor, it.startOffset, it.length)
                mediaPlayer.prepare()
            }
            //由于mediaPlay没有判断是否载入了资源的方法所以设置isPrepared属性
            isPrepared = true
        }
    }

    override fun onCreate() {
        //获得播放器
        mediaPlayer = MediaPlayer()

        //观察当前音乐的index
        App.musicList.currentSongIndex.observeForever(loadTrigger)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onBind(intent: Intent) = mBinder
    override fun onDestroy() {
        //清理播放器
        mediaPlayer.stop()
        mediaPlayer.release()

        //取消监听音乐的index
        App.musicList.currentSongIndex.removeObserver(loadTrigger)
    }
}