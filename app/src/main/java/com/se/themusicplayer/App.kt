package com.se.themusicplayer

import android.app.Application
import com.se.themusicplayer.data.Song
import java.util.*

class App :Application() {
    companion object {
//        //同步音乐毫秒用的key
//        const val CURRENT_TIME = "current_ms"


        /** 播放队列 */
        val musicList: LinkedList<Song> = LinkedList()
        /** 当前歌曲指针 */
        var currentSongIndex : Int? = if(!musicList.isEmpty()) 0 else null
        /** 当前歌曲 */
        var currentSongData :Song? = if(!musicList.isEmpty()) musicList[currentSongIndex!!] else null

        /** 当前歌曲的当前毫秒 */
        var currentMs = 0
        /** 当前歌曲的总毫秒 */
        var currentDuration = 0

        /** 刷新now指针和now数据 */
        fun refreshMusicListStateData(){
            currentSongIndex = if(!musicList.isEmpty()) 0 else null
            currentSongData = if(!musicList.isEmpty()) musicList[currentSongIndex!!] else null
        }
    }
}