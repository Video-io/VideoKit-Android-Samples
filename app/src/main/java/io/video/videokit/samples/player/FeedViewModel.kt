package io.video.videokit.samples.player

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.runtime.RememberObserver
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import io.video.videokit.player.AspectMode
import io.video.videokit.player.DefaultPlayer
import io.video.videokit.player.LoopMode
import io.video.videokit.player.PlayersManager
import io.video.videokit.playlist.Playlist

class FeedViewModel(
    val manager: PlayersManager
) : RememberObserver {

    val adapter = Adapter()

    private val observer = object : Playlist.Observer {
        override fun onVideoMoved(fromIndex: Int, toIndex: Int, id: String) {
            adapter.notifyItemMoved(fromIndex, toIndex)
        }
        override fun onVideosChanged(index: Int, ids: List<String>) {
            // Pass payload to avoid change animations
            adapter.notifyItemRangeChanged(index, ids.size, ids)
        }
        override fun onVideosInserted(index: Int, ids: List<String>) {
            adapter.notifyItemRangeInserted(index, ids.size)
        }
        override fun onVideosRemoved(index: Int, ids: List<String>) {
            adapter.notifyItemRangeRemoved(index, ids.size)
        }
    }

    override fun onAbandoned() = Unit
    override fun onRemembered() { manager.playlist.addObserver(observer) }
    override fun onForgotten() { manager.playlist.removeObserver(observer) }

    fun initialize(pager: ViewPager2) {
        pager.orientation = ViewPager2.ORIENTATION_VERTICAL
        pager.adapter = adapter
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val rv = adapter.recyclerView ?: return
                val vh = rv.findViewHolderForAdapterPosition(position) ?: return
                vh as ViewHolder
                vh.player?.play()
            }
        })
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {

        var recyclerView: RecyclerView? = null

        override fun getItemCount() = manager.playlist.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = FrameLayout(parent.context)
            view.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(position)
        }

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            holder.unbind()
        }

        override fun onFailedToRecycleView(holder: ViewHolder): Boolean {
            holder.unbind()
            return false
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            this.recyclerView = recyclerView
        }
    }

    inner class ViewHolder(val view: ViewGroup) : RecyclerView.ViewHolder(view) {
        var player: DefaultPlayer? = null

        fun bind(index: Int) {
            val old = player
            val new = manager[index]
            player = new
            new.aspectMode = AspectMode.Crop
            new.loopMode = LoopMode.Repeat
            if (new !== old) {
                view.removeAllViews()
                view.addView(new.view)
                view.setOnClickListener { new.toggle() }
            }
        }

        fun unbind() {
            player?.let {
                view.removeAllViews()
                view.setOnClickListener(null)
                manager.release(it)
            }
            player = null
        }
    }
}