package com.se.themusicplayer.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.se.themusicplayer.App
import com.se.themusicplayer.adapter.PlaylistDialogAdapter
import com.se.themusicplayer.databinding.DialogPlayListBinding
import com.se.themusicplayer.data.ListSongData

class PlaylistDialog : BottomSheetDialogFragment() {
    private var _binding : DialogPlayListBinding?=null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        DialogPlayListBinding.inflate(inflater,container,false).let {
            _binding = it
            it.root
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //初始化数据
        for (i in App.musicList){
            dataSet.add(ListSongData(i.name,i.singer))
        }

        //设置歌曲数
        binding.playListTitle.let {
            it.text = it.text.toString().format(App.musicList.size)
        }

        //初始化LV绑定
        binding.playListRV.apply {
            adapter = PlaylistDialogAdapter(dataSet)
            // TODO: 滚动到当前歌曲
            layoutManager = LinearLayoutManager(context)
        }
    }

    private val dataSet = mutableListOf<ListSongData>()
}