package com.example.b1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

object Util {
    val pictureDirectory = File(Environment.getExternalStorageDirectory().absolutePath+"/"+Environment.DIRECTORY_PICTURES + "/MyPic")
    fun isFileExisted(fileName : String): Boolean{

        val path = "${pictureDirectory}/${fileName}"
        Log.d("existed", File(path).exists().toString())
        return File(path).exists()
    }
    init {
        if(!pictureDirectory.exists())
        {
            pictureDirectory.mkdirs()
        }
    }


//    fun songArt(path: String, context: Context): Bitmap {
//        val retriever = MediaMetadataRetriever()
//        val inputStream: InputStream
//        retriever.setDataSource(path)
//        if (retriever.embeddedPicture != null) {
//            inputStream = ByteArrayInputStream(retriever.embeddedPicture)
//            val bitmap = BitmapFactory.decodeStream(inputStream)
//            retriever.release()
//            return bitmap
//        } else {
//            return getLargeIcon(context)
//        }
//    }

}