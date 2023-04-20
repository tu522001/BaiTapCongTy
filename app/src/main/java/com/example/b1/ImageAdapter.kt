package com.example.b1

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.b1.databinding.ItemIconBinding
import com.google.android.material.shape.MaterialShapeDrawable

class ImageAdapter(var listImages: MutableList<Image> ) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private lateinit var listener: OnImageScrollListener
    private lateinit var onItemListener: OnItemListener
    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ImageViewHolder(val itembinding: ItemIconBinding) :
        RecyclerView.ViewHolder(itembinding.root) {
        private val shapeDrawable = MaterialShapeDrawable()



        fun bind(image: Image) {
            Glide.with(itembinding.root)
                .load(image.url)
                .into(itembinding.imageView)

            Log.d("III","Length : "+listImages.size)
            Log.d("III","%6 : "+listImages.size%6)
            Log.d("III","%7 : "+listImages.size%7)


//            // Thiết lập ShapeAppearanceModel cho MaterialShapeDrawable
            if (((position + 1) % 6 == 0) ) { // Bo góc ở vị trí 6, 12, 18,...
                val cornerSize = 14.dpToPx(itembinding.root.context)
                val shapeAppearanceModel = itembinding.imageView.shapeAppearanceModel
                    .toBuilder()
                    .setTopRightCornerSize(cornerSize.toFloat())
                    .setBottomRightCornerSize(cornerSize.toFloat())
                    .build()
                itembinding.imageView.shapeAppearanceModel = shapeAppearanceModel


            }else if(((position + 1) % 6 == 1)){
                val cornerSize = 14.dpToPx(itembinding.root.context)
                val shapeAppearanceModel = itembinding.imageView.shapeAppearanceModel
                    .toBuilder()
                    .setTopLeftCornerSize(cornerSize.toFloat())
                    .setBottomLeftCornerSize(cornerSize.toFloat())
                    .build()
                itembinding.imageView.shapeAppearanceModel = shapeAppearanceModel
            }




            // Thiết lập MaterialShapeDrawable cho ImageView
            itembinding.imageView.background = shapeDrawable

            itembinding.imageView.setOnClickListener {
                onItemListener.onClick(adapterPosition, listImages[adapterPosition].url)
            }
        }
        fun Int.dpToPx(context: Context): Int {
            val density = context.resources.displayMetrics.density
            return (this * density).toInt()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageAdapter.ImageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        var itemBinding = ItemIconBinding.inflate(layoutInflater, parent, false)




        return ImageViewHolder(itemBinding);
    }

    override fun onBindViewHolder(holder: ImageAdapter.ImageViewHolder, position: Int) {
        holder.bind(listImages[position])

        if (position > 0 && (position + 1) % 6 == 0) {
            // Nếu là ảnh thứ 6, thêm view trống
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.setMargins(0, 0, 20, 0) // margin phải 16dp
            holder.itemView.layoutParams = layoutParams
        } else {
            // Nếu không phải ảnh thứ 6, xoá margin
            val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.setMargins(0, 0, 0, 0)
            holder.itemView.layoutParams = layoutParams
        }

        val image = listImages[position]
        holder.bind(image)


        if (position == listImages.size - 1) {
            listener.onImageScrolled()
        }
    }




    override fun getItemCount(): Int {
        return listImages.size
    }

    fun setOnItemClick(onItemListener: OnItemListener) {
        this.onItemListener = onItemListener
    }

    interface OnItemListener {
        fun onClick(position: Int, url: String)
    }

    interface OnImageScrollListener {
        fun onImageScrolled()
    }

}