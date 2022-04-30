package com.se.themusicplayer.ui.player

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import coil.load
import com.se.themusicplayer.App
import com.se.themusicplayer.R
import com.se.themusicplayer.data.Song
import com.se.themusicplayer.databinding.ActivityPlayerBinding
import com.se.themusicplayer.formatToTime
import com.se.themusicplayer.service.MusicService
import com.se.themusicplayer.ui.dialog.PlaylistDialog

class PlayerActivity : AppCompatActivity() {
    companion object{
        const val REPEAT_PLAY = 0     //循环播放
        const val RANDOM_PLAY = 1     //随机播放
        const val SEQUENCE_PLAY =2    //顺序播放

        const val UPDATE_MUSIC_PROGRESS = 0   //更新进度条和当前时间
        const val UPDATE_DURATION = 1         //更新歌曲总时间
    }

    /** 暂停否 */
    private var isPause = false
    /** 循环模式 */
    private var repeatMode = REPEAT_PLAY
    /** 播放连接的状态 */
    private var isPlayConnection = false
    /** 暂停连接的状态 */
    private var isPauseConnection = false

    /** 界面binding */
    private lateinit var binding:ActivityPlayerBinding

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
            when (msg.what) {
                /** 更新歌曲时间进度 */
                UPDATE_MUSIC_PROGRESS -> {
                    binding.musicProcessBar.progress = App.currentMs
                    binding.musicCurrentTime.text = App.currentMs.formatToTime()
                }
                /** 更新歌曲时长 */
                UPDATE_DURATION -> {
                    binding.musicTotalTime.text = App.currentDuration.formatToTime()
                }

                else -> throw Exception("PlayerActivity线程更新类型选择错误")
            }
        }
    }

    /** 播放的连接 */
    private val cnctOfPlay = object :ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            (service as MusicService.MusicPlayBinder).apply {
                play(processHandler)
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }
    /** 暂停的连接 */
    private val cnctOfPause = object :ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            (service as MusicService.MusicPlayBinder).pause()
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }
    /** 更新歌曲时长的连接 */
    private val cnctOfDuration = object :ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            (service as MusicService.MusicPlayBinder).updateDuration(processHandler)
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: 删除这个临时数据准备
        initData()

        //初始化界面绑定
        initBinding()

        //初始化服务
        initService()

        //初始化监听器绑定
        initListener()

        //初始化控件状态
        initView()
    }

    private fun initView() {
        binding.apply {
            if (!App.musicList.isEmpty()) {
                //当前歌曲
                musicName.text = App.currentSongData!!.name
                musicSinger.text = App.currentSongData!!.singer
                
                //进度条最大值
                musicProcessBar.max = App.currentDuration

                //进度条当前值
                musicProcessBar.progress = App.currentMs

                //当前时间
                musicCurrentTime.text = App.currentMs.formatToTime()

                //最大时间
                bindService(Intent(this@PlayerActivity,MusicService::class.java),cnctOfDuration,Context.BIND_AUTO_CREATE)
            }
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
                    // TODO: Service控制器定义跳转到时刻的方法,然后修改下面
//                    if (fromUser) player.seekTo(progress)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            //播放模式
            playMode.apply {
                setOnClickListener {
                    repeatMode = (repeatMode + 1) % 3
                    when (repeatMode) {
                        REPEAT_PLAY -> R.drawable.ic_repeat_play
                        RANDOM_PLAY -> R.drawable.ic_random_play
                        SEQUENCE_PLAY -> R.drawable.ic_sequence_play
                        else -> {throw Exception("播放模式越界")}
                    }.let { setImageResource(it) }
                }
            }

            //上一首
            // TODO: 更新顶部歌曲信息 
            previousSong.apply {
                speed = 2f
                setOnClickListener {
                    //动画效果
                    playAnimation()

                    if(!App.musicList.isEmpty()) {
                        //确保关闭Service对象
                        if (isPauseConnection) {
                            unbindService(cnctOfPause)
                            isPauseConnection = false
                        }
                        if (isPlayConnection) unbindService(cnctOfPlay)
                        stopService(Intent(this@PlayerActivity, MusicService::class.java))

                        //切换now指针
                        App.currentSongIndex!!.let{
                            if(it>0) it.dec()
                        }

                        //重启服务以装载新的歌曲
                        startService(Intent(this@PlayerActivity, MusicService::class.java))

                        //若之前是播放状态,则继续保持
                        if (isPlayConnection) bindService(Intent(this@PlayerActivity,
                            MusicService::class.java), cnctOfPlay, Context.BIND_AUTO_CREATE)
                    }
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

                        //断开暂停的连接
                        if(isPauseConnection) {
                            unbindService(cnctOfPause)
                        }

                        //开启暂停连接
                        Intent(this@PlayerActivity,MusicService::class.java).let {
                            bindService(it,cnctOfPause,Context.BIND_AUTO_CREATE)
                        }
                        isPauseConnection = true
                    }
                    else {
                        //动画: |> 变 ||
                        playAnimation()

                        //断开播放的连接
                        if(isPlayConnection){
                            unbindService(cnctOfPlay)
                            isPlayConnection = false
                        }

                        //开启播放的连接
                        Intent(this@PlayerActivity,MusicService::class.java).let {
                            bindService(it,cnctOfPlay,Context.BIND_AUTO_CREATE)
                        }

                        isPlayConnection = true
                    }
                }
                // TODO: 将isPause的改变向外提出到if中
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

                    if(!App.musicList.isEmpty()) {
                        //确保关闭Service对象
                        if (isPauseConnection) {
                            unbindService(cnctOfPause)
                            isPauseConnection = false
                        }
                        if (isPlayConnection) unbindService(cnctOfPlay)
                        stopService(Intent(this@PlayerActivity, MusicService::class.java))

                        //切换now指针
                        App.currentSongIndex!!.let{
                            if(it<App.musicList.size-1) it.inc()
                        }

                        //重启服务以装载新的歌曲
                        startService(Intent(this@PlayerActivity, MusicService::class.java))

                        //若之前是播放状态,则继续保持
                        if (isPlayConnection) bindService(Intent(this@PlayerActivity,
                            MusicService::class.java), cnctOfPlay, Context.BIND_AUTO_CREATE)
                    }
                }
            }

            //播放列表
            listOfSongs.apply {
                setOnClickListener {
                    PlaylistDialog().show(supportFragmentManager, null)
                }
            }
        }
    }

    private fun initService(){
        if (!App.musicList.isEmpty()){
            startService(Intent(this,MusicService::class.java))
        }
    }

    // TODO: 删除它
    private fun initData(){
        App.musicList.apply {
            add(Song("孙笑川干了哪些恶事","孙笑川","Music/孙笑川.mp3"))
        }
        App.refreshMusicListStateData()
    }

    // TODO: 添加歌曲或删除歌曲后,要更新now指针和now数据 -> 将方法封装到App中
    // TODO: 将歌曲切换.播放.暂停的动作封装
}




