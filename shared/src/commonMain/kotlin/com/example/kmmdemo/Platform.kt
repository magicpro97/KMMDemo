package com.example.kmmdemo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform