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
        //先清空原来的
        dataSet.clear()

        //装载
        for (i in App.musicList){
            dataSet.add(ListSongData(i.name,i.singer))
        }

        //标注当前的歌
        if (!App.musicList.isEmpty()) {
            dataSet[App.musicList.currentSongIndex.value!!].let{
                it.name = "● ${it.name}"
            }
        }

        //设置歌曲数
        binding.playListTitle.let {
            it.text = it.text.toString().format(App.musicList.size)
        }

        //初始化LV绑定
        binding.playListRV.apply {
            //初始化
            adapter = PlaylistDialogAdapter(dataSet)
            layoutManager = LinearLayoutManager(context)

            //滚动到当前
            if(App.musicList.currentSongIndex.value!=null){
                scrollToPosition(App.musicList.currentSongIndex.value!!)
            }
        }
    }

    private val dataSet = mutableListOf<ListSongData>()
}