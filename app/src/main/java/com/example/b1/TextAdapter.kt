package com.example.b1

import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.b1.databinding.ItemIconBinding
import com.example.b1.databinding.ItemTextBinding

class TextAdapter(var listText: MutableList<Contents>) :
    RecyclerView.Adapter<TextAdapter.textViewHolder>() {

    private val boldTexts = mutableSetOf<String>()

    fun updateBoldTexts(boldTexts: Set<String>) {
        this.boldTexts.clear()
        this.boldTexts.addAll(boldTexts)
        notifyDataSetChanged()
    }

    inner class textViewHolder(val textViewBinding: ItemTextBinding) :
        RecyclerView.ViewHolder(textViewBinding.root) {


        var textView: TextView

        init {
            textView = itemView.findViewById(R.id.textContent)
        }

        fun bind(contents: Contents) {
            textView.text = contents.theme
            textView.setTypeface(null, if (boldTexts.contains(contents.theme)) Typeface.BOLD else Typeface.NORMAL)
        }

    }

    override fun getItemCount(): Int {
        return listText.size
        Log.d("GGG","listText.size : "+listText.size)
    }

    override fun onBindViewHolder(holder: textViewHolder, position: Int) {
        holder.bind(listText[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): textViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        var itemTextBinding = ItemTextBinding.inflate(layoutInflater, parent, false);
        return textViewHolder(itemTextBinding)
    }


}
