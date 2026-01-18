package com.brewery.searcher.core.network.api

class ApiException(
    val userMessage: String,
    cause: Throwable? = null,
) : Exception(userMessage, cause)
