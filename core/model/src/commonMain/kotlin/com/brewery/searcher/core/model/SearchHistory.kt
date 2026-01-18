package com.brewery.searcher.core.model

data class SearchHistory(
    val id: Long,
    val query: String,
    val searchType: SearchType,
    val timestamp: Long,
)
