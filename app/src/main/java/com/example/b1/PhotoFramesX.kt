package com.example.b1

data class PhotoFramesX(
    val cover: String,
    val defines: List<DefineX>,
    val folder: String,
    val icon: String,
    val lock: Boolean,
    val name: String,
    val name_vi: String,
    val openPackageName: String,
    val totalImage: Int
) {
    fun toImage() : List<Image> {
        val images = mutableListOf<Image>()
//        images.add(
//            Image(
//                url = "https://mystoragetm.s3.ap-southeast-1.amazonaws.com/Frames/ClassicFrames/" + folder + "/$cover",
//                isFirst = true
//            )
//        )

        defines.forEach { defineX ->
            for (i in defineX.start until defineX.end) {
                images.add(
                    Image(
                        url = "https://mystoragetm.s3.ap-southeast-1.amazonaws.com/Frames/ClassicFrames/" + folder + "/" + folder + "_frame_" + i+ ".png",
                        isEnd = i == defines.size - 1
                    )
                )
            }
        }


        return images
    }

    fun openImage(){

    }
}