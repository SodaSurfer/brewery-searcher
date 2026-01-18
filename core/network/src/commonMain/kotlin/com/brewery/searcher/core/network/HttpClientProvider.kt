package com.brewery.searcher.core.network

import io.ktor.client.HttpClient

expect fun createHttpClient(): HttpClient
