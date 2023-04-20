package com.example.b1

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.b1.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity(), ImageAdapter.OnImageScrollListener {

    private lateinit var imageAdapter: ImageAdapter
    private lateinit var textAdapter: TextAdapter
    private lateinit var binding: ActivityMainBinding

    private var photoFramesList = mutableListOf<PhotoFramesX>()
    private var images = mutableListOf<Image>()
    private var defineXList = mutableListOf<DefineX>()
    private lateinit var listText: MutableList<Contents>


    companion object {
        const val BASE_URL = "https://mystoragetm.s3.ap-southeast-1.amazonaws.com/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        // Sử dụng biến binding để truy cập các thành phần trong layout
        setContentView(binding.root)

        imageAdapter = ImageAdapter(images, )
        binding.recyclerView.adapter = imageAdapter

        listText = Contents.getMock().toMutableList()
        textAdapter = TextAdapter(listText)
        binding.recyclerViewText.adapter = textAdapter


        imageAdapter.setOnItemClick(object : ImageAdapter.OnItemListener {
            override fun onClick(position: Int, url: String) {
                Log.d("YYY", "position : " + position + ", URL : " + url)
                Glide.with(this@MainActivity).load(images[position].url).into(binding.imgAvatar)

//                handleImageClick()
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

                    imageAdapter.notifyDataSetChanged()

                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.d("AAA", "exception : ${t.message}")
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onImageScrolled() {
        val firstVisiblePosition =
            (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        val lastVisiblePosition =
            (binding.recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

        // Count the number of images for each text
        val imageCounts = mutableMapOf<String, Int>()
        for (i in firstVisiblePosition..lastVisiblePosition) {
            val text = listText[i].theme
            imageCounts[text] = imageCounts.getOrDefault(text, 0) + 1
        }

        // Determine which texts should be bold
        val boldTexts = mutableSetOf<String>()
        for ((text, count) in imageCounts) {
            if (count >= 10) {
                boldTexts.add(text)
            }
        }

        // Update TextAdapter with bold texts
        textAdapter.updateBoldTexts(boldTexts)
    }

}