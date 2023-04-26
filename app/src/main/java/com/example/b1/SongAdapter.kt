package com.example.b1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide

class SongAdapter (var itemSongEventListener: ItemSongEventListener, var listSong : MutableList<Song>) : RecyclerView.Adapter<SongAdapter.MusicViewHolder>() {

//    private lateinit var onItemListener: OnItemListener

    inner class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var imageView: ImageView
        private var tvSong: TextView
        private var tvSinger: TextView
        init {
            imageView = itemView.findViewById(R.id.imgSongs)
            tvSong = itemView.findViewById(R.id.txtSongNames)
            tvSinger = itemView.findViewById(R.id.txtSingerNames)
        }

        fun bind(song: Song) {
            Glide.with(itemView.context).load(Util.songArt(song.uri,itemView.context)).into(imageView)
            tvSong.text = song.title
            tvSinger.text = song.singerName
            itemView.setOnClickListener{
                itemSongEventListener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_layout_song, parent, false);
        return MusicViewHolder(view);
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(listSong[position])
    }

    override fun getItemCount(): Int {
        return listSong.size
    }

//    fun setOnItemClick(onItemListener: OnItemListener) {
//        this.onItemListener = onItemListener
//    }

//    interface OnItemListener {
//        fun onClickItem(position: Int)
//    }
}