package com.example.b1

import android.Manifest
import android.app.DownloadManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.b1.databinding.ActivityMainBinding
import com.example.b1.service.ActionPlaying
import com.example.b1.service.ApplicationClass.Companion.ACTION_NEXT
import com.example.b1.service.ApplicationClass.Companion.ACTION_PLAY
import com.example.b1.service.ApplicationClass.Companion.ACTION_PREV
import com.example.b1.service.ApplicationClass.Companion.CHANNEL_ID_2

import com.example.b1.service.MusicService
import com.example.b1.service.NotificationReceiver
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity(), ImageAdapter.OnDownloadClickListener, ActionPlaying,
    ServiceConnection, ItemSongEventListener {

    private lateinit var imageAdapter: ImageAdapter
    private lateinit var textAdapter: TextAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var layoutManager: LinearLayoutManager
    private var photoFramesList = mutableListOf<PhotoFramesX>()
    private var images = mutableListOf<Image>()
    private var defineXList = mutableListOf<DefineX>()
    private var downloadID: Long = 0L
    private var imageItem: Image? = null
    private val REQUEST_CODE_READ_EXTERNAL_STORAGE = 1
    private var mediaPlayer: MediaPlayer? = null
    private var listSong = mutableListOf<Song>()
    private var musicService: MusicService? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    private lateinit var songAdapter: SongAdapter
    private lateinit var title: String
    private lateinit var singerName: String
    private lateinit var uris: String
    private lateinit var mediaSession: MediaSessionCompat


    companion object {
        const val BASE_URL = "https://mystoragetm.s3.ap-southeast-1.amazonaws.com/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        mediaSession = MediaSessionCompat(this, "PlayerAudio")
        // Sử dụng biến binding để truy cập các thành phần trong layout
        setContentView(binding.root)

        imageAdapter = ImageAdapter(this, images)
        binding.recyclerView.adapter = imageAdapter

        textAdapter = TextAdapter(this, photoFramesList)
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
                imageItem = images[position]
                if (imageItem != null && Util.isFileExisted(imageItem!!.fileName)) {
                    displayImage()
                }
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


        mediaPlayer = MediaPlayer()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Nếu đã được cấp quyền truy cập bộ nhớ, lấy danh sách file nhạc
            getMusicFiles()

            bottomSheetDialog = BottomSheetDialog(this@MainActivity, R.style.BottomSheetTheme)
            val sheetview: View =
                LayoutInflater.from(applicationContext).inflate(R.layout.layout_bottom_sheet, null)
            bottomSheetDialog!!.setContentView(sheetview)
            Log.d("YYYU", "LIST SONG : " + listSong)
            // Khởi tạo SongAdapter và gán cho ListView
            songAdapter = SongAdapter(this,listSong)
            sheetview.findViewById<RecyclerView>(R.id.recyclerViewSong).adapter = songAdapter

        } else {
            // Nếu chưa được cấp quyền, yêu cầu cấp quyền truy cập bộ nhớ
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_READ_EXTERNAL_STORAGE
            )
        }

//        songAdapter.setOnItemClick(object : SongAdapter.OnItemListener {
//            override fun onClickItem(position: Int) {
//
//                Log.d("AAAD", "position : " + position)
//// Update the MediaPlayer object with the new song
//                mediaPlayer?.apply {
//                    reset() // Reset the MediaPlayer object to its uninitialized state
//                    setDataSource(listSong[position].uri)
//
//                    prepare() // Prepare the MediaPlayer object to play the new song
//                    start() // Start playing the new song
//                    binding.txtSingerName.text = listSong[position].singerName
//                    binding.txtSongName.text = listSong[position].title
//                    Log.d("AAASS","title : "+title)
//                    binding.imgbtnPlay.setImageResource(R.drawable.ic_pause)
//                }
//
//// hiện thị bài hát trong file
//                val retriever = MediaMetadataRetriever()
//                retriever.setDataSource(listSong.get(position).uri) // đường dẫn đến tệp MP3
//                val artwork = retriever.embeddedPicture
//                if (artwork != null) {
//                    val bitmap = BitmapFactory.decodeByteArray(artwork, 0, artwork.size)
//                    binding.imgSong.setImageBitmap(bitmap)
//                    Log.d("QQWQ", "binding.imgSong.setImageBitmap(bitmap) : ")
//                } else {
//                    // Nếu không có hình ảnh nào nhúng trong tệp MP3
//                    // thì bạn có thể đặt một hình ảnh mặc định cho ImageView
//                    binding.imgSong.setImageResource(R.drawable.c)
//                }
//
//
//                binding.imgbtnPlay.setOnClickListener {
//                    try {
//                        if (mediaPlayer?.isPlaying == true) {
//                            // Nếu nhạc đang phát, pause và đổi ảnh nút
//                            mediaPlayer!!.pause()
//                            binding.imgbtnPlay.setImageResource(R.drawable.ic_play)
//                        } else {
//                            // Nếu nhạc chưa được phát hoặc đã tạm dừng, tiếp tục phát hoặc bắt đầu phát lại
//                            mediaPlayer?.apply {
//                                if (currentPosition > 0) {
//                                    start()
//                                    setTimeTotal()
//                                    updateTime()
//                                } else {
//                                    setDataSource(listSong[position].uri)
//                                    prepare()
//                                    start()
//                                    setTimeTotal()
//                                    updateTime()
//
//                                }
//                            }
//                            // Set trạng thái đang phát và đổi ảnh nút
//                            binding.imgbtnPlay.setImageResource(R.drawable.ic_pause)
//                        }
//                    } catch (e: Exception) {
//                        e.message
//                    }
//
//                }
//
//                bottomSheetDialog?.dismiss()
//
//            }
//        })

// Khai báo biến để xác định trạng thái của MediaPlayer

        binding.imgbtnPlay.setOnClickListener {
            try {
                if (mediaPlayer?.isPlaying == true) {
                    // Nếu nhạc đang phát, pause và đổi ảnh nút
                    mediaPlayer!!.pause()
                    binding.imgbtnPlay.setImageResource(R.drawable.ic_play)
                } else {
                    // Nếu nhạc chưa được phát hoặc đã tạm dừng, tiếp tục phát hoặc bắt đầu phát lại
                    mediaPlayer?.apply {
                        if (currentPosition > 0) {
                            start()
                            setTimeTotal()
                            updateTime()
                        } else {
                            setDataSource(listSong[1].uri)
                            binding.txtSingerName.text = singerName
                            binding.txtSongName.text = title
                            prepare()
                            start()
                            setTimeTotal()
                            updateTime()
                        }
                    }
                    // Set trạng thái đang phát và đổi ảnh nút
                    binding.imgbtnPlay.setImageResource(R.drawable.ic_pause)
                }
            } catch (e: Exception) {
                e.message
            }
        }
        Log.d("QQWWEE", "listSong : " + listSong.size)
        binding.constraintlayout.setOnClickListener {
            bottomSheetDialog?.show()
        }
    }

    private fun getMusicFiles() {
        val musicFiles = mutableListOf<String>()
        Log.d("AAA", "musicFiles : " + musicFiles)
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Images.Media.DATA
        )

        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val cursor = applicationContext.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.let {
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val singerNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val dataUri = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
//            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 1)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                title = it.getString(nameIndex)
                singerName = it.getString(singerNameIndex)
                uris = it.getString(dataUri)

                listSong.add(Song(title, singerName, uris))
                Log.d("YYY", "listSong : " + title)
            }
            it.close()
        }

        // In danh sách tên file nhạc
        musicFiles.forEach {
            Log.d("Music", it)

            bottomSheetDialog
        }

        // bắt sự kiện trên SeekBak
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            // chạm vào kéo seekBar xong buông ra thì nó sẽ lấy giá trị seekBar cuối cùng khi buông ra
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mediaPlayer!!.seekTo(seekBar.progress)
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
                Log.d("YUY", "downloadID" + downloadID)
                Log.d("YUY", "downloadId" + downloadId)
                if (downloadID == downloadId) {

                    displayImage()
                    imageAdapter.notifyDataSetChanged()
                    // Xử lý khi download hoàn thành
                    // Ví dụ: hiển thị thông báo hoặc mở file đã tải về
                }
            }
        }
    }


    private fun updateTime() {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                val dinhDangGio = SimpleDateFormat("mm:ss")
                binding.txtStart.text = dinhDangGio.format(mediaPlayer!!.currentPosition)

                //update progress skSong
                binding.seekBar.progress = mediaPlayer!!.currentPosition

                handler.postDelayed(this, 500)
            }
        }, 100)
    }

    private fun setTimeTotal() {
        // hàm xử lý sự kiện phút và giây
        val dinhDangGio = SimpleDateFormat("mm:ss")
        binding.txtEnd.setText(dinhDangGio.format(mediaPlayer!!.duration))
        // gán SeeBak = tổng thời gian bài hát
        // tức là gán max của skSong = mediaPlayer.getDuration()
        binding.seekBar.max = mediaPlayer!!.duration
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            // Nếu đã được cấp quyền truy cập bộ nhớ, lấy danh sách file nhạc
            getMusicFiles()
        }
    }

    private fun displayImage() {
        imageItem?.let {
            Glide.with(this).load(File(Util.pictureDirectory, it.fileName)).into(binding.imgAvatar)
            binding.txtName.text = it.fileName
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadCompleteReceiver, intentFilter)

    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(downloadCompleteReceiver)
        unbindService(this)
    }

    override fun onDownloadClick(downloadId: Long) {

    }

    override fun nextClicked() {

    }

    override fun prevClicked() {

    }

    override fun playClicked() {

    }

    override fun onServiceConnected(name: ComponentName?, iBinder: IBinder) {
        val binder: MusicService.MyBinder = iBinder as MusicService.MyBinder
        musicService = binder.getService()
        musicService?.setCallBack(this@MainActivity)
        Log.e("Connected", musicService.toString() + "")
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
        Log.e("Disconnected", musicService.toString() + "")
    }

    fun showNotification(playPauseBtn: Int, position : Int) {
        val intent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val prevIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_PREV)
        val prevpendingIntent =
            PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val playIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_PLAY)
        val playpendingIntent =
            PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val nextIntent = Intent(this, NotificationReceiver::class.java).setAction(ACTION_NEXT)
        val nextpendingIntent =
            PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val picture = Util.songArt(listSong.get(position).uri,this)
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID_2)
            .setSmallIcon(R.drawable.c)
            .setLargeIcon(picture)
            .setContentTitle(listSong.get(position).title)
            .setContentText(listSong.get(position).singerName)
            .addAction(R.drawable.ic_previous, "Previous", prevpendingIntent)
            .addAction(playPauseBtn, "Play", playpendingIntent)
            .addAction(R.drawable.ic_next, "Next", nextpendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notification)
    }

    override fun onItemClick(position: Int) {
        Log.d("AAAD", "position : " + position)
// Update the MediaPlayer object with the new song
        mediaPlayer?.apply {
            reset() // Reset the MediaPlayer object to its uninitialized state
            setDataSource(listSong[position].uri)

            prepare() // Prepare the MediaPlayer object to play the new song
            start() // Start playing the new song
            binding.txtSingerName.text = listSong[position].singerName
            binding.txtSongName.text = listSong[position].title
            Log.d("AAASS", "title : " + title)
            binding.imgbtnPlay.setImageResource(R.drawable.ic_pause)
        }

// hiện thị bài hát trong file
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(listSong.get(position).uri) // đường dẫn đến tệp MP3
        val artwork = retriever.embeddedPicture
        if (artwork != null) {
            val bitmap = BitmapFactory.decodeByteArray(artwork, 0, artwork.size)
            binding.imgSong.setImageBitmap(bitmap)
            Log.d("QQWQ", "binding.imgSong.setImageBitmap(bitmap) : ")
        } else {
            // Nếu không có hình ảnh nào nhúng trong tệp MP3
            // thì bạn có thể đặt một hình ảnh mặc định cho ImageView
            binding.imgSong.setImageResource(R.drawable.c)
        }

        showNotification(R.drawable.ic_play,position)
        binding.imgbtnPlay.setOnClickListener {
            try {
                if (mediaPlayer?.isPlaying == true) {
                    // Nếu nhạc đang phát, pause và đổi ảnh nút
                    mediaPlayer!!.pause()
                    binding.imgbtnPlay.setImageResource(R.drawable.ic_play)

                } else {
                    // Nếu nhạc chưa được phát hoặc đã tạm dừng, tiếp tục phát hoặc bắt đầu phát lại
                    mediaPlayer?.apply {
                        if (currentPosition > 0) {
                            start()
                            setTimeTotal()
                            updateTime()
                        } else {
                            setDataSource(listSong[position].uri)
                            prepare()
                            start()
                            setTimeTotal()
                            updateTime()

                        }
                    }
                    // Set trạng thái đang phát và đổi ảnh nút
                    binding.imgbtnPlay.setImageResource(R.drawable.ic_pause)
                }
            } catch (e: Exception) {
                e.message
            }

        }
        bottomSheetDialog?.dismiss()

    }

}