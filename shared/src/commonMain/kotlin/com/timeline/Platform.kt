package com.timeline

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform