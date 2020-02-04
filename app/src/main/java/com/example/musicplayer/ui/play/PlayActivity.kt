package com.example.musicplayer.ui.play

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Song
import com.example.musicplayer.ext.toTrack
import com.example.musicplayer.ui.songs.SongVM
import com.example.musicplayer.utils.Constants
import com.example.player.IPlayer
import com.example.player.Player
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.android.synthetic.main.content_play.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayActivity : AppCompatActivity() {

    private var title: String = ""
    private var type: String = ""
    private var id: Long = -10L
    private var songID: Long = -10L

    private var playImmediately = false

    private val vm: SongVM by viewModel()

    private val list: ArrayList<IPlayer.Track> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        setSupportActionBar(toolbar)

        loadTypeData(intent)

        //set title
        toolbar.title = title
        //set action toolbar
        setSupportActionBar(toolbar)

        vm.getSongs(id, type).observe(this, Observer {
            preparePlayList(it)
        })

        Player.liveDataPlayNow.observe(this, Observer {
            drawUI(it)
        })

        Player.progresLiveData.observe(this, Observer {
            //todo implement
        })

        Player.liveDataPlayerState.observe(this, Observer {
            togglePlayPauseIcon(it)
        })

        bindComponents()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun togglePlayPauseIcon(it: IPlayer.State?) {
        if (it == null) return

        when (it) {
            IPlayer.State.PLAY -> {
                play_pause.setImageDrawable(getDrawable(R.drawable.uamp_ic_pause_white_48dp))
            }

            IPlayer.State.PAUSE -> {
                play_pause.setImageDrawable(getDrawable(R.drawable.uamp_ic_play_arrow_white_48dp))
            }

            IPlayer.State.ERROR -> {
                play_pause.setImageDrawable(getDrawable(R.drawable.uamp_ic_play_arrow_white_48dp))
            }

            IPlayer.State.STOP -> {
                play_pause.setImageDrawable(getDrawable(R.drawable.uamp_ic_play_arrow_white_48dp))
            }

            IPlayer.State.PREPARING -> {
                play_pause.setImageDrawable(getDrawable(R.drawable.uamp_ic_play_arrow_white_48dp))
            }
        }

    }

    private fun bindComponents() {
        play_pause.setOnClickListener {
            Player.togglePlayPause()
        }

        next.setOnClickListener {
            Player.next()
        }

        prev.setOnClickListener {
            Player.prev()
        }
    }

    private fun drawUI(it: IPlayer.Track?) {
        if (it == null) return

        Glide.with(this)
            .load(it.imageUri)
            .placeholder(R.drawable.ic_launcher)
            .error(R.drawable.ic_launcher)
            .into(play_bcg)

        play_title.text = it.title
        play_artist.text = it.artist
        play_album.text = it.album
        play_endText.text = Player.trackDuration.toString()
    }

    private fun preparePlayList(it: List<Song>?) {
        //clear all previous data
        list.clear()
        it?.forEach { list.add(it.toTrack()) }
        Player.playList = list

        if (playImmediately && list.isNotEmpty()) {
            Player.start(list[0].id)
        }

        if (songID > 0) {
            Player.start(songID.toString())
        }
    }

    private fun loadTypeData(intent: Intent) {
        type = intent.getStringExtra(Constants.Type.Type) ?: ""
        id = intent.getLongExtra(Constants.Songs.ID, 0)
        title = intent.getStringExtra(Constants.Songs.Name) ?: ""
        playImmediately = intent.getBooleanExtra("requestForPlay", false)
        songID = intent.getLongExtra(Constants.Songs.SONG_ID, -10L)
    }

}
