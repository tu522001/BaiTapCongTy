package com.example.b1

import android.app.DownloadManager
import android.content.Context
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
import com.example.b1.databinding.ActivityMainBinding

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var imageAdapter: ImageAdapter
    private lateinit var textAdapter: TextAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var layoutManager: LinearLayoutManager

    private var photoFramesList = mutableListOf<PhotoFramesX>()
    private var images = mutableListOf<Image>()
    private var defineXList = mutableListOf<DefineX>()
//    private lateinit var listText: MutableList<Contents>


    companion object {
        const val BASE_URL = "https://mystoragetm.s3.ap-southeast-1.amazonaws.com/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        // Sử dụng biến binding để truy cập các thành phần trong layout
        setContentView(binding.root)

        imageAdapter = ImageAdapter(images)
        binding.recyclerView.adapter = imageAdapter

//        listText = Contents.getMock().toMutableList()
        textAdapter = TextAdapter(photoFramesList)
        binding.recyclerViewText.adapter = textAdapter

        layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager



        binding.recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    val index = (layoutManager.findFirstVisibleItemPosition)
                    //use this index for any operation you want to perform on the item visible on screen. eg. log(arrayList[index])
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
//                val index = layoutManager.findFirstVisibleItemPosition()
//                Log.d("HJK", " VI TRI ANH : "+index)
                scrollToRelativeCategory()
            }
        })






//        imageAdapter.setOnItemClick(object : ImageAdapter.OnItemListener {
//            override fun onClick(position: Int, url: String) {
//                Log.d("YYY", "position : " + position + ", URL : " + url)
//                Glide.with(this@MainActivity).load(images[position].url).into(binding.imgAvatar)
//
//
////                val url = "http://example.com/file.mp3" // Đường dẫn URL tới tệp cần tải xuống
//                val fileName = "file.png" // Tên tệp sẽ được lưu trữ trên thiết bị
//
//                val request = DownloadManager.Request(Uri.parse(url))
//                    .setTitle(fileName)
//                    .setDescription("Downloading")
//                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
//                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
//
//                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//                val downloadId = downloadManager.enqueue(request)
//
//
//
//                Log.d("ASSA","downloadId : "+downloadId)
//            }
//        })

        val downloadedImages = HashMap<String, Boolean>()

        imageAdapter.setOnItemClick(object : ImageAdapter.OnItemListener {
            override fun onClick(position: Int, url: String) {
                Log.d("YYY", "position : " + position + ", URL : " + url)
                Glide.with(this@MainActivity).load(images[position].url).into(binding.imgAvatar)

                val fileName = "file.png"
//                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

                if (downloadedImages[url] == true) {
                    Toast.makeText(this@MainActivity, "Hình ảnh này đã được tải về rồi", Toast.LENGTH_SHORT).show()
                    binding
                } else {
                    val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val request = DownloadManager.Request(Uri.parse(url))
                        .setTitle(fileName)
                        .setDescription("Downloading")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    val downloadId = downloadManager.enqueue(request)

                    // Lưu trạng thái của ảnh là chưa tải về
                    downloadedImages[url] = false

                    // Kiểm tra xem tệp đã được tải về hay chưa
                    val query = DownloadManager.Query()
                        .setFilterById(downloadId)
                    var cursor = downloadManager.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (columnIndex >= 0) {
                            val status = cursor.getInt(columnIndex)
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                // Lưu trạng thái của ảnh là đã tải về
                                downloadedImages[url] = true
                                Toast.makeText(this@MainActivity, "Đã tải về hình ảnh", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    cursor?.close()
                }
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

}