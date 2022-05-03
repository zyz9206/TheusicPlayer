package com.se.themusicplayer.ui.player

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.se.themusicplayer.App
import com.se.themusicplayer.MusicList
import com.se.themusicplayer.R
import com.se.themusicplayer.databinding.ActivityPlayerBinding
import com.se.themusicplayer.formatToTime
import com.se.themusicplayer.ui.dialog.PlaylistDialog
import com.se.themusicplayer.vm.PlayerViewModel
import java.util.*

class PlayerActivity : AppCompatActivity() {
    companion object{
        /** 表示资源已装载好,可以更新界面了 */
        const val UPDATE = "ACTION_UPDATE_PLAYER_ACTIVITY"
    }

    /** 暂停否 */
    private var isPause = false

    /** 界面binding */
    private lateinit var binding:ActivityPlayerBinding
    /** 界面vm */
    private val viewModel by lazy {
        ViewModelProvider(this).get(PlayerViewModel::class.java)
    }
    /** 更新界面广播 */
    private lateinit var broadcastOfUpdateView : UpdatePlayerActivityVM
    inner class UpdatePlayerActivityVM : BroadcastReceiver(){
        //过滤器,用于过滤出自己到处理的广播
        private val filter = IntentFilter().apply{
            addAction(UPDATE)
        }
        //处理器,用于处理过滤出来的广播
        private val processor = fun(context: Context?, intent: Intent?){
            (context as PlayerActivity).viewModel.refresh()
        }

        //从活动中注册
        fun register(context: Context) = context.registerReceiver(this,filter)
        //从活动中注销
        fun unregister(context: Context) = context.unregisterReceiver(this)

        //【过滤后的广播从这进入】
        override fun onReceive(context: Context?, intent: Intent?){
            processor(context,intent)		//委托自己的处理器去处理广播
        }
    }

    /** CD旋转动画 */
    private val cdAnimator:ObjectAnimator by lazy{
        ObjectAnimator.ofFloat(binding.cdCover, "rotation", 0f, 360f).apply {
            interpolator = LinearInterpolator()
            duration = 32_000L
            repeatCount = -1
            start()
        }
    }
    /** 进度更新器 */
    private val processHandler = object :Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            binding.musicProcessBar.progress = viewModel.process.value ?: 0                      //进度条
            binding.musicCurrentTime.text = viewModel.process.value?.formatToTime() ?: "0:00"    //时间
            viewModel.updateProcess()
        }
    }

    /** 右下角歌单 */
    private val musicSheet = PlaylistDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //初始化界面绑定
        initBinding()

        //初始化广播接收
        initBroadcast()

        //初始化监听器绑定
        initListener()

        //设置自身ViewModel的观察触发
        initVmObserve()

        //设置Application的观察触发
        initAppObserve()
    }

    override fun onDestroy() {
        super.onDestroy()
        //注销广播
        desBroadcast()
    }

    private fun desBroadcast() {
        broadcastOfUpdateView.unregister(this)
    }

    private fun initBroadcast() {
        broadcastOfUpdateView = UpdatePlayerActivityVM().apply{
            register(this@PlayerActivity)
        }
    }

    private fun initBinding(){
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
    }

    private fun initListener(){
        binding.apply {
            //返回按钮
            btnBack.setOnClickListener {
                Toast.makeText(this@PlayerActivity,"返回",Toast.LENGTH_SHORT).show()
            }

            //CD
            cdCover.load(R.drawable.tmp)
            cdAnimator

            //进度条
            musicProcessBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) viewModel.seekTo(progress)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            //播放模式
            playMode.apply {
                setOnClickListener {
                    viewModel.changeMode()
                }
            }

            //上一首
            previousSong.apply {
                speed = 2f
                setOnClickListener {
                    //动画效果
                    playAnimation()

                    //上一首
                    viewModel.previous()
                }
            }

            //播放 or 暂停
            musicPlay.apply {
                //动画速度
                speed = 2f
                setOnClickListener {
                    if (isPause) {
                        //动画: || 变 |>
                        resumeAnimation()

                        //暂停音乐
                        viewModel.pause()
                    }
                    else {
                        //动画: |> 变 ||
                        playAnimation()

                        //播放音乐
                        viewModel.start()
                    }
                }
                addAnimatorUpdateListener {
                    if (!isPause && it.animatedFraction > 0.5f) {
                        isPause = !isPause
                        pauseAnimation()
                    }
                }
                addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        isPause = false
                    }
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                })
            }

            //下一首
            nextSong.apply {
                speed = 2f
                setOnClickListener {
                    //动画效果
                    playAnimation()

                    //下一首
                    viewModel.next()
                }
            }

            //播放列表
            listOfSongs.apply {
                setOnClickListener {
                    musicSheet.show(supportFragmentManager, null)
                }
            }
        }
    }

    private fun initVmObserve(){
        viewModel.apply {
            //歌曲名
            songName.observe(this@PlayerActivity){
                binding.musicName.text = it ?: "Music Name"
            }
            //歌手
            singer.observe(this@PlayerActivity){
                binding.musicSinger.text = it ?: "singer"
            }

            //进度
            process.observe(this@PlayerActivity){
                processHandler.sendEmptyMessageDelayed(0,100)
            }
            //时长
            duration.observe(this@PlayerActivity){
                binding.musicProcessBar.max = it ?: 0                       //进度条
                binding.musicTotalTime.text = it?.formatToTime() ?: "0:00"  //时间
            }

            //模式
            mode.observe(this@PlayerActivity){
                when(it){
                    MusicList.REPEAT_PLAY -> R.drawable.ic_repeat_play
                    MusicList.RANDOM_PLAY -> R.drawable.ic_random_play
                    MusicList.SEQUENCE_PLAY -> R.drawable.ic_sequence_play
                    else -> throw Exception("循环模式类型错误")
                }.let { binding.playMode.setImageResource(it) }
            }
        }
    }

    private fun initAppObserve(){
        App.apply {
            //服务控制器
            musicServiceBinder.observe(this@PlayerActivity){
            }
        }
    }


        // TODO: 添加歌曲或删除歌曲后,要更新now指针和now数据 -> 将方法封装到App中
        // TODO: 将歌曲切换.播放.暂停的动作封装
}

// TODO: [笔记]LiveDate()的数据只要是被重新赋了值,就会触发观察
// TODO: [笔记]observe的连锁反应一定要放在handle中,否则会阻塞