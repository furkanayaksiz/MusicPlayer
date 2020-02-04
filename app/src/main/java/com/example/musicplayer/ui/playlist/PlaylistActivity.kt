package com.example.musicplayer.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.R
import com.example.musicplayer.data.model.Playlist
import com.example.musicplayer.data.repo.PlaylistRepository
import com.example.musicplayer.ui.main.ClickListener
import com.example.musicplayer.ui.songs.SongActivity
import com.example.musicplayer.utils.Constants
import com.iamsdt.androidextension.MyCoroutineContext
import com.iamsdt.androidextension.gone
import com.iamsdt.androidextension.nextActivity
import com.iamsdt.androidextension.show
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_playlist.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_playlist.*
import kotlinx.android.synthetic.main.playlist_dialogs.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistActivity : AppCompatActivity(), ClickListener<Playlist> {

    private val vm: PlaylistVM by viewModel()

    private val uiScope = MyCoroutineContext()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)
        setSupportActionBar(toolbar)
        //add observer to coroutines
        lifecycle.addObserver(uiScope)

        //text
        playlist_text.setOnClickListener {
            showPlaylistDialog()
        }

        //recyclerview
        val adapter = PlaylistAdapter(this, this)
        val manager = LinearLayoutManager(this)
        playlist_rcv.layoutManager = manager
        playlist_rcv.adapter = adapter

        //item decoration
        val dividerItemDecoration = DividerItemDecoration(
            main_rcv.context,
            manager.orientation
        )
        playlist_rcv.addItemDecoration(dividerItemDecoration)

        vm.getPlaylist().observe(this, Observer {
            if (it == null || it.isEmpty()) {
                emptyView()
            } else {
                regularView()
                //submit list to the adapter
                adapter.submitList(it)
            }
        })
    }

    private fun showPlaylistDialog() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(
            R.layout.playlist_dialogs, playlist_root, false
        )
        builder.setView(view)
        val dialog = builder.create()

        view.playlist_dialog_btn.setOnClickListener {
            val name = view.playlist_dialog_et.text.toString()
            if (name.isEmpty()) {
                Toasty.warning(this, "Please input valid name").show()
            } else {
                val repo = PlaylistRepository.getInstance(this)
                uiScope.launch(Dispatchers.IO) {
                    repo?.createPlaylist(name)
                    withContext(Dispatchers.Main) {
                        if (dialog.isShowing) {
                            dialog.dismiss()
                        }
                    }
                }
            }
        }

        dialog.show()
    }

    private fun regularView() {
        playlist_rcv.show()
        playlist_text.gone()
    }

    private fun emptyView() {
        playlist_rcv.gone()
        playlist_text.show()
    }

    override fun click(model: Playlist) {
        val map = mapOf(
            Pair(Constants.Type.Type, Constants.Type.TypePlaylist),
            Pair(Constants.Playlist.PlaylistID, model.id),
            Pair(Constants.Playlist.PlaylistName, model.name)
        )

        nextActivity<SongActivity>(list = map)
    }

}