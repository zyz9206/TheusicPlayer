package com.se.themusicplayer.data

data class Song(
    val id:String,          //歌曲ID
    val name:String,    //歌名
    val singer:String,  //歌手
    var src:String,     //歌曲资源路径     //"Music/xxx.mp3"
    val kind:Int       //歌曲类型 0:网络song 1:本地song
)
