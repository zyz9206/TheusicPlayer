package com.se.themusicplayer.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.se.themusicplayer.App
import com.se.themusicplayer.ui.player.PlayerActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.concurrent.thread

/**
 * 提供播放音乐服务
 * 提供监控播放进度服务
 */
class MusicService: Service() {
    companion object{
        /** 播放装载好的音乐 */
        const val PLAY_MUSIC = "ACTION_PLAY_IT_NOW"
    }

    /** 触发装载 */
    private val loadTrigger = Observer<Int?> {
        loadMusic()
    }

    /** 切歌之前的播放状态 */
    private var previousPlayingState = false
    /** 是否装载了资源 */
    private var isPrepared = false

    /** 音乐控制器 */
    private val mBinder by lazy { MusicPlayBinder() }
    inner class MusicPlayBinder:Binder(){
        /** 是否装载了资源 */
        fun isPrepared() = isPrepared
        /** 是否在播放 */
        fun isPlaying() = mediaPlayer.isPlaying

        /** 获取歌曲进度 */
        fun getProgress():Int? = if (isPrepared) mediaPlayer.currentPosition else null
        /** 获取歌曲时长 */
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
            //保存切歌之前的Playing状态
            previousPlayingState = mediaPlayer.isPlaying

            //切歌
            mediaPlayer.reset()
            App.musicList.previous()
        }
        /** 选择歌单中的某首 */
        fun selectSong(index:Int){
            //保存切歌之前的Playing状态
            previousPlayingState = mediaPlayer.isPlaying

            //切歌
            mediaPlayer.reset()
            App.musicList.selectSong(index)
        }
        /** 下一首 */
        fun next(){
            //保存切歌之前的Playing状态
            previousPlayingState = mediaPlayer.isPlaying

            //切歌
            mediaPlayer.reset()
            App.musicList.next()
        }
    }
    /** 音乐播放器 */
    private lateinit var mediaPlayer :MediaPlayer
    /** 音乐播放广播 */
    private lateinit var broadcastOfPlay:MusicPlay
    inner class MusicPlay:BroadcastReceiver(){
        //过滤器,用于过滤出自己到处理的广播
        private val filter = IntentFilter().apply{
            addAction(PLAY_MUSIC)
        }
        //处理器,用于处理过滤出来的广播
        private val processor = fun(context: Context?, intent: Intent?){
            if (previousPlayingState){
                mediaPlayer.start()
            }
        }

        //从活动中注册
        fun register(context: Context) = context.registerReceiver(this,filter)
        //从活动中注销
        fun unregister(context: Context) = context.unregisterReceiver(this)

        //【过滤后的广播从这进入】
        override fun onReceive(context: Context?, intent: Intent?){
            processor(context,intent)		//委托自己的处理器去处理广播
        }
    }

    /**
     * 装载音乐
     * @since 每次MusicList.currentSongIndex改变时就会重新装载
     */
    private fun loadMusic(){
        if (!App.musicList.isEmpty()) {
            // TODO: 更改歌曲载入方式
            when(App.musicList.currentSongData!!.kind){
                0 -> {
                    thread {
                        //URL
                        val HOST = "http://music.eleuu.com/song/url?id="
                        val request= Request.Builder().url("${HOST}${App.musicList.currentSongData!!.id}").build()

                        //通讯
                        val client= OkHttpClient()
                        val response=client.newCall(request).execute()

                        //解析
                        val url = JSONObject(response.body?.string().toString()).getJSONArray("data").getJSONObject(0).getString("url")
                        App.musicList.currentSongData!!.src = url

                        //装载
                        mediaPlayer.setDataSource(this, Uri.parse(url))
                        mediaPlayer.prepare()

                        //通知: 更新界面
                        Intent(PlayerActivity.UPDATE).let {
                            it.`package` = application.packageName
                            this@MusicService.sendBroadcast(it)
                        }

                        //通知: 播放它(看情况)
                        Intent(PLAY_MUSIC).let {
                            it.`package` = application.packageName
                            this@MusicService.sendBroadcast(it)
                        }
                    }
                }
                1 -> assets.openFd(App.musicList.currentSongData!!.src).let {
                    mediaPlayer.setDataSource(it.fileDescriptor, it.startOffset, it.length)
                    mediaPlayer.prepare()
                }
            }
            //由于mediaPlay没有判断是否载入了资源的方法所以设置isPrepared属性
            isPrepared = true
        }
    }

    override fun onCreate() {
        //获得播放器
        mediaPlayer = MediaPlayer()
        App.musicList.currentSongIndex.observeForever(loadTrigger)

        //初始化广播
        broadcastOfPlay = MusicPlay().apply {
            register(this@MusicService)
        }
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

        //卸载广播
        broadcastOfPlay.unregister(this@MusicService)
    }
}