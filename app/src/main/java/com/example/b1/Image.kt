package com.example.b1

data class Image(
    var url: String = "",
    var isFirst: Boolean = false,
    var isEnd: Boolean = false,
    var isSpace: Boolean = false,
    var folder: String = "",
    var downloaded: Boolean = false,
    var fileName : String = ""
) {

}
