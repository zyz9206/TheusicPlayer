package com.se.themusicplayer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
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
                    Toast.makeText(parent.context,"提示",Toast.LENGTH_SHORT).show()
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