package com.appspell.shaderview.demo.video

import android.os.Bundle
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle.*
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.appspell.shaderview.demo.BuildConfig
import com.appspell.shaderview.demo.R
import com.appspell.shaderview.demo.databinding.ActivityVideoGridBinding
import com.appspell.shaderview.ext.TextureFilter
import com.appspell.shaderview.ext.getTexture2dOESSurface
import com.appspell.shaderview.gl.params.ShaderParamsBuilder
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util

class VideoActivity : AppCompatActivity() {
    lateinit var binding: ActivityVideoGridBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoGridBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listOf(binding.shaderView1, binding.shaderView2, binding.shaderView3, binding.shaderView4)
            .forEachIndexed { index, shaderView ->
                shaderView.apply {
                    textureFilter = when (index) {
                        1 -> TextureFilter.Nearest
                        2 -> TextureFilter.LinearMipmap
                        3 -> TextureFilter.NearestMipmap
                        else -> TextureFilter.Linear
                    }
                    updateContinuously = true // update the view each frame (do not forget set it "true")
                    fragmentShaderRawResId = R.raw.video_shader // fragment shader for video frame processing
                    shaderParams = ShaderParamsBuilder()
                        .addTextureOES("uVideoTexture") // video texture input/output
                        .build()
                    onViewReadyListener = { shader ->
                        // get surface from shader params
                        val surface = shader.params.getTexture2dOESSurface("uVideoTexture")

                        // initialize video player when shader is ready
                        initVideoPlayer(surface)
                    }
                }
            }
    }

    /**
     * Initialize ExoPlayer
     *
     * example: https://exoplayer.dev/hello-world.html
     */
    private fun initVideoPlayer(surface: Surface?) {
        val uri = RawResourceDataSource.buildRawResourceUri(R.raw.video)
        val mediaItem: MediaItem = MediaItem.fromUri(uri)

        val userAgent: String = Util.getUserAgent(this, BuildConfig.APPLICATION_ID)
        val defDataSourceFactory = DefaultDataSourceFactory(this, userAgent)

        val mediaSource: MediaSource = ProgressiveMediaSource
            .Factory(defDataSourceFactory)
            .createMediaSource(mediaItem)

        val player = SimpleExoPlayer.Builder(this@VideoActivity).build()
            .apply {
                setMediaSource(mediaSource)
                prepare()
                setVideoSurface(surface)
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_ALL
            }

        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Event.ON_PAUSE)
            fun onPause() {
                player.pause()
            }

            @OnLifecycleEvent(Event.ON_RESUME)
            fun onResume() {
                player.playWhenReady = true
            }
        })
    }
}