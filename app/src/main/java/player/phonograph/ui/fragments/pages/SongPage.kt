/*
 * Copyright (c) 2022 chr_56 & Abou Zeid (kabouzeid) (original author)
 */

package player.phonograph.ui.fragments.pages

import android.annotation.SuppressLint
import android.util.Log
import android.view.Menu.NONE
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import com.github.chr56.android.menu_dsl.attach
import com.github.chr56.android.menu_extension.add
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import mt.util.color.primaryTextColor
import player.phonograph.App
import player.phonograph.BuildConfig
import player.phonograph.R
import player.phonograph.adapter.display.DisplayAdapter
import player.phonograph.adapter.display.SongDisplayAdapter
import player.phonograph.mediastore.MediaStoreUtil
import player.phonograph.mediastore.SongLoader
import player.phonograph.model.Song
import player.phonograph.model.sort.SortMode
import player.phonograph.model.sort.SortRef
import player.phonograph.service.MusicPlayerRemote
import player.phonograph.service.queue.ShuffleMode
import player.phonograph.settings.Setting
import player.phonograph.ui.fragments.pages.util.DisplayUtil
import player.phonograph.ui.fragments.pages.util.ListOptionsPopup
import player.phonograph.util.ImageUtil.getTintedDrawable
import player.phonograph.util.PhonographColorUtil.nightMode

class SongPage : AbsDisplayPage<Song, DisplayAdapter<Song>, GridLayoutManager>() {

    override fun initLayoutManager(): GridLayoutManager {
        return GridLayoutManager(hostFragment.requireContext(), 1)
            .also { it.spanCount = DisplayUtil(this).gridSize }
    }

    override fun initAdapter(): DisplayAdapter<Song> {
        val displayUtil = DisplayUtil(this)

        val layoutRes =
            if (displayUtil.gridSize > displayUtil.maxGridSizeForList) R.layout.item_grid
            else R.layout.item_list
        Log.d(
            TAG, "layoutRes: ${if (layoutRes == R.layout.item_grid) "GRID" else if (layoutRes == R.layout.item_list) "LIST" else "UNKNOWN"}"
        )

        return SongDisplayAdapter(
            hostFragment.mainActivity,
            hostFragment.cabController,
            ArrayList(), // empty until songs loaded
            layoutRes
        ) {
            usePalette = displayUtil.colorFooter
        }
    }

    override fun loadDataSet() {
        loaderCoroutineScope.launch {
            val temp = MediaStoreUtil.getAllSongs(App.instance)
            while (!isRecyclerViewPrepared) yield() // wait until ready

            withContext(Dispatchers.Main) {
                if (isRecyclerViewPrepared) adapter.dataset = temp
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun refreshDataSet() {
        adapter.notifyDataSetChanged()
    }

    override fun getDataSet(): List<Song> {
        return if (isRecyclerViewPrepared) adapter.dataset else emptyList()
    }

    override fun setupSortOrderImpl(
        displayUtil: DisplayUtil,
        popup: ListOptionsPopup,
    ) {

        val currentSortMode = displayUtil.sortMode
        if (BuildConfig.DEBUG) Log.d(GenrePage.TAG, "Read cfg: sortMode $currentSortMode")

        popup.allowRevert = true
        popup.revert = currentSortMode.revert

        popup.sortRef = currentSortMode.sortRef
        popup.sortRefAvailable =
            arrayOf(
                SortRef.SONG_NAME,
                SortRef.ALBUM_NAME,
                SortRef.ARTIST_NAME, SortRef.YEAR, SortRef.ADDED_DATE,
                SortRef.MODIFIED_DATE,
                SortRef.DURATION,
            )
    }

    override fun saveSortOrderImpl(
        displayUtil: DisplayUtil,
        popup: ListOptionsPopup,
    ) {
        val selected = SortMode(popup.sortRef, popup.revert)
        if (displayUtil.sortMode != selected) {
            displayUtil.sortMode = selected
            loadDataSet()
            Log.d(AlbumPage.TAG, "Write cfg: sortMode $selected")
        }
    }

    override fun getHeaderText(): CharSequence {
        val n = getDataSet().size
        return hostFragment.mainActivity.resources.getQuantityString(R.plurals.x_songs, n, n)
    }

    override fun configAppBar(panelToolbar: Toolbar) {
        val context = hostFragment.mainActivity

        val allSongs = SongLoader.getAllSongs(context)

        attach(context, panelToolbar.menu) {
            rootMenu.add(this, NONE, NONE, 1, getString(R.string.action_play)) {
                icon = context
                    .getTintedDrawable(R.drawable.ic_play_arrow_white_24dp,
                        context.primaryTextColor(context.resources.nightMode))
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_ALWAYS
                onClick {
                    if (Setting.instance.rememberShuffle) {
                        MaterialAlertDialogBuilder(context)
                            .setMessage(R.string.pref_title_remember_shuffle)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                MusicPlayerRemote
                                    .playQueue(allSongs, 0, true, null)
                            }
                            .setNegativeButton(android.R.string.cancel) { _, _ ->
                                MusicPlayerRemote
                                    .playQueue(allSongs, 0, true, ShuffleMode.NONE)
                            }.create().show()
                    } else {
                        MusicPlayerRemote
                            .playQueue(allSongs, 0, true, ShuffleMode.NONE)
                    }
                    true
                }
            }
            rootMenu.add(this, NONE, NONE, 2, getString(R.string.action_shuffle_all)) {
                icon = context
                    .getTintedDrawable(R.drawable.ic_shuffle_white_24dp,
                        context.primaryTextColor(context.resources.nightMode))
                showAsActionFlag = MenuItem.SHOW_AS_ACTION_ALWAYS
                onClick {
                    MusicPlayerRemote
                        .playQueue(allSongs, 0, true, ShuffleMode.SHUFFLE)
                    true
                }
            }
        }
    }

    companion object {
        const val TAG = "SongPage"
    }
}
