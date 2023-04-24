package com.example.b1

import android.os.Environment
import android.util.Log
import java.io.File

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

}