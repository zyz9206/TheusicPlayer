package com.se.themusicplayer.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.se.themusicplayer.adapter.PlaylistDialogAdapter
import com.se.themusicplayer.databinding.DialogPlayListBinding
import com.se.themusicplayer.model.TestSongData

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

        binding.playListRV.apply {
            initData()
            adapter = PlaylistDialogAdapter(dataSet)
            //设置列表歌曲数
            //滚动到当前歌曲
            layoutManager = LinearLayoutManager(context)
        }
    }

    private val dataSet = mutableListOf<TestSongData>()
    private fun initData(){
        repeat(10){
            dataSet.add(TestSongData("1","2"))
        }
    }
}