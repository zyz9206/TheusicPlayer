package com.se.themusicplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.se.themusicplayer.App
import com.se.themusicplayer.R
import com.se.themusicplayer.data.ListSongData

class PlaylistDialogAdapter(private val data:List<ListSongData>):RecyclerView.Adapter<PlaylistDialogAdapter.ViewHolder>(){
    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val songName : TextView = view.findViewById(R.id.songName)
        val singer : TextView = view.findViewById(R.id.singer)
    }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        LayoutInflater.from(parent.context).inflate(R.layout.rvitem_play_music_list,parent,false).let{
            ViewHolder(it).apply {
                itemView.setOnClickListener{
                    //去除原来歌曲的标注
                    data[App.musicList.currentSongIndex.value!!].let {
                        it.name = it.name.substring(1)
                    }

                    //切歌
                    App.musicServiceBinder.value?.selectSong(layoutPosition)

                    //标注当前的歌
                    if (!App.musicList.isEmpty()) {
                        data[App.musicList.currentSongIndex.value!!].let{
                            it.name = "● ${it.name}"
                        }
                    }

                    //刷新
                    notifyDataSetChanged()
                }
            }
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        data[position].let{
            holder.apply {
                songName.text = it.name
                singer.text = it.singer
            }
        }
    }
}