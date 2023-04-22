package com.example.b1

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.example.b1.databinding.ItemIconBinding
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation
import java.io.File

class ImageAdapter(var context: Context, var listImages: MutableList<Image>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    //    val downloadedImages = HashMap<String, Boolean>()
    private var selectedPosition = -1
    private val downloadedImages = mutableListOf<DownloadedImage>()
    private var urlList = mutableListOf<String>()
    private lateinit var onItemListener: OnItemListener

    override fun getItemViewType(position: Int): Int {
        return position
    }

    init {
        // Tải hình ảnh đã tải xuống từ SharedPreferences
        val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val downloadedImagesJson = prefs.getString("downloaded_images", "")
        if (downloadedImagesJson != null && downloadedImagesJson.isNotEmpty()) {
            downloadedImages.addAll(
                Gson().fromJson(
                    downloadedImagesJson, object : TypeToken<List<DownloadedImage>>() {}.type
                )
            )
        }
    }

    inner class ImageViewHolder(val itembinding: ItemIconBinding) :
        RecyclerView.ViewHolder(itembinding.root) {
        private val shapeDrawable = MaterialShapeDrawable()

        fun bind(image: Image) {
            val blurTransformation: Transformation<Bitmap> = BlurTransformation(25, 3)

            val downloadedImage = downloadedImages.find { it.fileName == image.url }
            Glide.with(itembinding.root).load(image.url).into(itembinding.imgcCoverPhoto)
            itembinding.imageViewload.visibility = View.GONE




            Log.d("III", "itembinding.textView2.text = image.url : " + image.url)

//            // Thiết lập ShapeAppearanceModel cho MaterialShapeDrawable
            if (((position + 1) % 6 == 0)) { // Bo góc ở vị trí 6, 12, 18,...
                // kích thước bo góc 14dpToPx
                val cornerSize = 14.dpToPx(itembinding.root.context)
                val shapeAppearanceModel =
                    itembinding.imgcCoverPhoto.shapeAppearanceModel.toBuilder()
                        //bo góc trên và dưới ở bên phải
                        .setTopRightCornerSize(cornerSize.toFloat())
                        .setBottomRightCornerSize(cornerSize.toFloat()).build()
                itembinding.imgcCoverPhoto.shapeAppearanceModel = shapeAppearanceModel

            } else if (((position + 1) % 6 == 1)) {
                val cornerSize = 14.dpToPx(itembinding.root.context)
                val shapeAppearanceModel =
                    itembinding.imgcCoverPhoto.shapeAppearanceModel.toBuilder()
                        //bo góc trên và dưới ở bên trái
                        .setTopLeftCornerSize(cornerSize.toFloat())
                        .setBottomLeftCornerSize(cornerSize.toFloat()).build()
                itembinding.imgcCoverPhoto.shapeAppearanceModel = shapeAppearanceModel
            }

            // Thiết lập MaterialShapeDrawable cho ImageView (Hình dạng vật liệu có thể vẽ)
            itembinding.imgcCoverPhoto.background = shapeDrawable
            urlList.add(image.url)


            // click vào ảnh con
            itembinding.imgcCoverPhoto.setOnClickListener {

                if (downloadedImage != null && selectedPosition != position ) {
                    // Thiết lập độ mờ của ImageView
                    onItemListener.onClick(adapterPosition, listImages[adapterPosition].url)

                    Glide.with(itembinding.root).load(image.url).apply(
                        RequestOptions().transforms(
                            CenterCrop(),
                            RoundedCorners(8),
                            if (position == selectedPosition) {
                                ColorFilterTransformation(Color.argb(204, 255, 255, 255))
                                BlurTransformation(50, 3)
                            } else {
                                blurTransformation
                            }
                        )
                    ).into(itembinding.imgcCoverPhoto)


                    val previousPosition = selectedPosition
                    selectedPosition = position
                    itembinding.imageViewload.visibility = View.VISIBLE
                    if (previousPosition != -1) {
                        notifyItemChanged(previousPosition)
                    }

                }
            }

            // Kiểm tra nếu hình tải về đã được tải về
            if (downloadedImage != null) {
                // Đã tải về, đặt hình từ file
                Glide.with(context).load(File(downloadedImage.path)).into(itembinding.dowload)
                itembinding.dowload.visibility = View.GONE
            } else {
                // Chưa tải về, đặt hình từ URL và tải về khi nhấp chuột
                Glide.with(context).load(image.url).into(itembinding.imgcCoverPhoto)
                itembinding.dowload.setOnClickListener {
                    // ẩn hình download đi
                    itembinding.dowload.visibility = View.GONE
                    // Tải hình về
                    val downloadManager =
                        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val request = DownloadManager.Request(Uri.parse(image.url))
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                        .setTitle(image.url)
                        .setDescription("Đang tải xuống ${image.url}")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_PICTURES,
                            image.url
                        )

                    val downloadId = downloadManager.enqueue(request)

                    // Thêm hình đã tải vào danh sách
                    val downloadedImage = DownloadedImage(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path,
                        image.url
                    )
                    downloadedImages.add(downloadedImage)

                    // Lưu các hình đã tải về vào SharedPreferences
                    val prefsEditor =
                        context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE).edit()
                    val downloadedImagesJson = Gson().toJson(downloadedImages)
                    prefsEditor.putString("downloaded_images", downloadedImagesJson)
                    prefsEditor.apply()
                    notifyDataSetChanged()
                }
            }
        }

        fun Int.dpToPx(context: Context): Int {
            val density = context.resources.displayMetrics.density
            return (this * density).toInt()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ImageAdapter.ImageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        var itemBinding = ItemIconBinding.inflate(layoutInflater, parent, false)
        return ImageViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ImageAdapter.ImageViewHolder, position: Int) {
        holder.bind(listImages[position])
        Log.d("NNN", "listImages[position].url : " + listImages[position].url)
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
    }

    override fun getItemCount(): Int {
        return listImages.size
        Log.d("SDD", "listImages.size : " + listImages.size)
    }

    fun setOnItemClick(onItemListener: OnItemListener) {
        this.onItemListener = onItemListener
    }

    interface OnItemListener {
        fun onClick(position: Int, url: String)
    }

}