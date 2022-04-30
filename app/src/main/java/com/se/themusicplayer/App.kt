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
            add(Song("孙笑川干了哪些恶事1","孙笑川","Music/孙笑川1.mp3"))
            add(Song("孙笑川干了哪些恶事2","孙笑川","Music/孙笑川1.mp3"))
            add(Song("孙笑川干了哪些恶事3","孙笑川","Music/孙笑川1.mp3"))
        }
    }
}