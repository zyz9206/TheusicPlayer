package com.se.themusicplayer

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.se.themusicplayer.data.Song
import com.se.themusicplayer.service.MusicService

class App :Application() {
    companion object {
        // TODO: 将播放队列移到Service中 
        /** 播放队列 */
        val musicList = MusicList()

        /** 服务控制器 */
        val musicServiceBinder = MutableLiveData<MusicService.MusicPlayBinder?>(null)

        /** 连接服务的connection */
        private val musicConnection by lazy {
            object :ServiceConnection{
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    musicServiceBinder.value = service as MusicService.MusicPlayBinder
                }
                override fun onServiceDisconnected(name: ComponentName?) {
                    musicServiceBinder.value = null
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        //App一打开就创建服务连接
        Intent(this,MusicService::class.java).let {
            startService(it)
            bindService(it, musicConnection, Context.BIND_AUTO_CREATE)
        }

        //添加数据
        // TODO: 删除下面的添加临时数据
        musicList.apply {
            add(Song("31010077","unity","11","http://m7.music.126.net/20220501230646/b6ee40c9006e5e9043c35169af53fe41/ymusic/4a7a/db11/c0a6/afe8ed0ed2105c44016e344d47d4c26c.mp3",0))
            add(Song("461347998","Something just like this","11","http://m7.music.126.net/20220501230714/eb5f44213df8905a2f17c12083cd83cd/ymusic/52e2/d91a/858c/2298ab6b6397600f690924e9c7e30925.mp3",0))
            add(Song("5065541","Whistle","11","http://m7.music.126.net/20220501230745/b5160bf04f9b6fc19c400133a046ab50/ymusic/ae14/4315/5e11/6468cc1d6b78f401066280f0edc2cc7c.mp3",0))
        }
    }
}