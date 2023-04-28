package com.example.b1.service

interface ActionPlaying {
    fun nextClicked(position : Int)
    fun prevClicked(position : Int)
    fun playClicked(position : Int)
}