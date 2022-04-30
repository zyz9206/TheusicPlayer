package com.se.themusicplayer.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.se.themusicplayer.App

class PlayerViewModel:ViewModel() {
    /** 是否装载了资源 */
    var isPrepared = App.musicServiceBinder.value?.isPrepared()
    /** 刷新所有持有的数据 */
    fun refresh(){
        songName.value = App.musicList.currentSongData?.name
        singer.value = App.musicList.currentSongData?.singer
        process.value = App.musicServiceBinder.value?.getProgress()
        duration.value = App.musicServiceBinder.value?.getDuration()
    }

    /** 当前歌曲名 */
    val songName = MutableLiveData<String?>(null).apply {
        value = App.musicList.currentSongData?.name
    }
    /** 当前歌手名 */
    val singer = MutableLiveData<String?>(null).apply {
        value = App.musicList.currentSongData?.singer
    }


    /** 当前进度 */
    val process = MutableLiveData<Int?>(null).apply {
        value = App.musicServiceBinder.value?.getProgress()
    }
    /** 手动更新进度 */
    fun updateProcess(){
        //只有在播放时才更新
        if(App.musicServiceBinder.value!=null && App.musicServiceBinder.value!!.isPlaying()) {
            process.value = App.musicServiceBinder.value?.getProgress()
        }
    }
    /** 总时长 */
    val duration = MutableLiveData<Int?>(null).apply {
        value = App.musicServiceBinder.value?.getDuration()
    }


    /** 当前模式 */
    val mode = MutableLiveData<Int?>(null).apply {
        value = App.musicList.currentMode
    }
    /** 改变模式 */
    fun changeMode(){
        mode.value = App.musicList.changeMode()
    }

    /** 播放 */
    fun start(){
        App.musicServiceBinder.value?.start()
        updateProcess() //用于启动连锁反应
    }
    /** 暂停 */
    fun pause(){
        App.musicServiceBinder.value?.pause()
    }
    /** 跳转 */
    fun seekTo(ms:Int){
        App.musicServiceBinder.value?.seekTo(ms)
        if(App.musicServiceBinder.value!=null) {
            process.value = App.musicServiceBinder.value?.getProgress()
        }        //用于触发更新,暂停时不会连锁
    }
    /** 上一首 */
    fun previous(){
        App.musicServiceBinder.value?.previous()
        refresh()   //刷新数据
    }
    /** 下一首 */
    fun next(){
        App.musicServiceBinder.value?.next()
        refresh()   //刷新数据
    }
}