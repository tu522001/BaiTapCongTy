package com.example.b1

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.b1.databinding.ItemIconBinding
import com.google.android.material.shape.MaterialShapeDrawable

class TextAdapter(var listPhotoFramesX: MutableList<PhotoFramesX>) :
    RecyclerView.Adapter<TextAdapter.textViewHolder>() {

    var selectedPosition = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class textViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var textView: TextView

        init {
            textView = itemView.findViewById(R.id.textContent)
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TextAdapter.textViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_text, parent, false);
        return textViewHolder(view)
    }

    override fun onBindViewHolder(holder: TextAdapter.textViewHolder, position: Int) {
//        holder.bind(listPhotoFramesX[position])

        val itemText = listPhotoFramesX[position]
        holder.textView.text = itemText.folder
        Log.d("BBBN", "position : " + itemText.folder)
//        holder.textView.text = listPhotoFramesX[position]
        Log.d("OOO", "" + listPhotoFramesX[position])
        Log.d("OOO", "position : " + position)
        var search: String =
            "https://mystoragetm.s3.ap-southeast-1.amazonaws.com/Frames/ClassicFrames/" + itemText.folder + "/" + itemText.folder + "_frame_" + ".png"
        Log.d("WER", "search : " + search)

        if (position == selectedPosition) {
            holder.textView.setTextColor(Color.RED);
        } else {
            holder.textView.setTextColor(Color.BLACK);
        }

    }


    override fun getItemCount(): Int {
        return listPhotoFramesX.size
    }


}