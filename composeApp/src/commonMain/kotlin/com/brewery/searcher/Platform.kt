package com.brewery.searcher

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform