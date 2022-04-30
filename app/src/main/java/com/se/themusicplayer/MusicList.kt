package com.se.themusicplayer

import androidx.lifecycle.MutableLiveData
import com.se.themusicplayer.data.ListSongData
import com.se.themusicplayer.data.Song
import java.util.*

class MusicList :LinkedList<Song>(){
    companion object{
        const val REPEAT_PLAY = 0     //循环播放
        const val RANDOM_PLAY = 1     //随机播放
        const val SEQUENCE_PLAY = 2    //顺序播放
        const val MODE_COUNT = 3    //模式的种类数
            // TODO: 添加模式时,记得要修改MODE_COUNT
    }

    /** 当前歌曲的Index */    // TODO: 新增|移除|切换歌曲时要修改这里
    var currentSongIndex = MutableLiveData<Int?>(null)
        private set
    /** 当前歌曲的Data */
    var currentSongData : Song? = null
        get() = if(currentSongIndex.value == null) null else this[currentSongIndex.value!!]
        private set

    /** 当前的模式 */
    var currentMode : Int = REPEAT_PLAY
        private set
    /** 改变循环模式 */
    fun changeMode():Int{
        //在所有模式中循环
        currentMode = (currentMode+1)% MODE_COUNT
        return currentMode
    }

    /** 上一首 */
    fun previous(){
        //由于装载资源会判空,所以即便null更新null触发了observe,也不会有影响
        currentSongIndex.value = when(currentMode){
            REPEAT_PLAY -> {
                if(!this.isEmpty()) {
                    (currentSongIndex.value!! - 1 + this.size) % this.size
                } else null
            }
            RANDOM_PLAY -> {
                if(!this.isEmpty()){
                    var newIndex: Int
                    do {
                        newIndex = (0 until this.size).random()
                    } while (newIndex==currentSongIndex.value!!)
                    newIndex
                } else null
            }
            // TODO: 改进:使播放到最后一首时停止播放,而不是一直播放最后一首
            SEQUENCE_PLAY -> {
                if (!this.isEmpty()){
                    if (currentSongIndex.value!! != 0){
                        currentSongIndex.value!! - 1
                    } else currentSongIndex.value!!
                } else null
            }
            else -> throw Exception("循环模式类型错误")
        }
    }
    /** 下一首 */
    fun next(){
        currentSongIndex.value = when(currentMode){
            REPEAT_PLAY -> {
                if(!this.isEmpty()) {
                    (currentSongIndex.value!! + 1) % this.size
                } else null
            }
            RANDOM_PLAY -> {
                if(!this.isEmpty()){
                    var newIndex: Int
                    do {
                        newIndex = (0 until this.size).random()
                    } while (newIndex==currentSongIndex.value!!)
                    newIndex
                } else null
            }
            // TODO: 改进:使播放到最后一首时停止播放,而不是一直播放最后一首
            SEQUENCE_PLAY -> {
                if (!this.isEmpty()){
                    if (currentSongIndex.value!!!=this.size-1){
                        currentSongIndex.value!!+1
                    } else currentSongIndex.value!!
                } else null
            }
            else -> throw Exception("循环模式类型错误")
        }
    }

    override fun add(element: Song): Boolean {
        if (currentSongIndex.value == null){
            currentSongIndex.value = 0
        }
        return super.add(element)
    }
}
