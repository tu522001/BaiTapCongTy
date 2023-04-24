package com.example.b1

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.b1.databinding.ActivityMainBinding
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.ColorFilterTransformation

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class MainActivity : AppCompatActivity(), ImageAdapter.OnDownloadClickListener {

    private lateinit var imageAdapter: ImageAdapter
    private lateinit var textAdapter: TextAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var layoutManager: LinearLayoutManager
    private var photoFramesList = mutableListOf<PhotoFramesX>()
    private var images = mutableListOf<Image>()
    private var defineXList = mutableListOf<DefineX>()
    private var downloadID: Long = 0L
    private var imageItem : Image? = null

    companion object {
        const val BASE_URL = "https://mystoragetm.s3.ap-southeast-1.amazonaws.com/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        // Sử dụng biến binding để truy cập các thành phần trong layout
        setContentView(binding.root)

        imageAdapter = ImageAdapter(this, images)
        binding.recyclerView.adapter = imageAdapter

        textAdapter = TextAdapter(photoFramesList)
        binding.recyclerViewText.adapter = textAdapter

        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollToRelativeCategory()
            }
        })

        imageAdapter.setOnItemClick(object : ImageAdapter.OnItemListener {
            override fun onClick(position: Int, url: String) {
                Log.d("YYY", "position MainActivity: " + position + ", URL : " + url)
//                Glide.with(this@MainActivity).load(images[position].url).into(binding.imgAvatar)
                imageItem = images[position]
                if ( imageItem != null && Util.isFileExisted(imageItem!!.fileName) ) {
                    displayImage()
                }

//
//                imageItem =
            }
        })

        imageAdapter.setDownloadClick(object : ImageAdapter.OnDownloadClickListener {
            override fun onDownloadClick(downloadId: Long) {
                Log.d("FGH", "downloadId MainActivity: $downloadId")
                // Xử lý dữ liệu downloadId ở đây
                downloadID = downloadId
            }

        })



        binding.button.setOnClickListener {
            binding.imgAvatar.setImageResource(R.drawable.img)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiInterface::class.java)
        apiService.getData().enqueue(object : Callback<ApiResponse> {

            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val photoFramesDTOList = response.body()?.listPhotoFrames ?: emptyList()

                    for (photoFrames in photoFramesDTOList) {
                        photoFramesList.add(
                            PhotoFramesX(
                                photoFrames.cover,
                                photoFrames.defines,
                                photoFrames.folder,
                                photoFrames.icon,
                                photoFrames.lock,
                                photoFrames.name,
                                photoFrames.name_vi,
                                photoFrames.openPackageName,
                                photoFrames.totalImage
                            )
                        )

                        val defineXDTOList = photoFrames.defines ?: emptyList()

                        for (defineXDTO in defineXDTOList) {
                            defineXList.add(
                                DefineX(
                                    defineXDTO.end,
                                    defineXDTO.indexDefineCollage,
                                    defineXDTO.start,
                                    defineXDTO.totalCollageItemContainer
                                )
                            )
                            Log.d("AAA", "end :" + defineXDTO.end + " start : " + defineXDTO.start)
                        }
                    }
                    photoFramesList.forEach { photoFrame ->
                        images.addAll(photoFrame.toImage())
                    }
                    images.forEach {
                        Log.d("checkImage", "onResponse: $it")
                    }
                    Log.d("fffff", "end image : " + photoFramesList.toString())

                    imageAdapter.notifyDataSetChanged()
                    textAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.d("AAA", "exception : ${t.message}")
            }
        })
    }

    fun scrollToRelativeCategory() {
        val lastVisibleFrame = layoutManager.findLastVisibleItemPosition()
        if (lastVisibleFrame > -1) {
            val image: Image = images[lastVisibleFrame]
            photoFramesList.forEach {
                if (it.folder.equals(image.folder)) {
                    binding.recyclerViewText.smoothScrollToPosition(
                        photoFramesList.indexOf(it)
                    )
                    textAdapter.selectedPosition = (photoFramesList.indexOf(it))
                }
            }
        }
    }

    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                // Lấy ID của download
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                Log.d("YUY","downloadID"+downloadID)
                Log.d("YUY","downloadId"+downloadId)
                if (downloadID==downloadId) {

                    displayImage()
                    imageAdapter.notifyDataSetChanged()
                    // Xử lý khi download hoàn thành
                    // Ví dụ: hiển thị thông báo hoặc mở file đã tải về
                }
            }
        }
    }

    private fun displayImage() {
        imageItem?.let {
            Glide.with(this).load(File(Util.pictureDirectory,it.fileName)).into(binding.imgAvatar)
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadCompleteReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(downloadCompleteReceiver)
    }

    override fun onDownloadClick(downloadId: Long) {

    }
}