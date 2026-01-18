package com.brewery.searcher.core.model

data class Brewery(
    val id: String,
    val name: String,
    val breweryType: BreweryType,
    val address: String?,
    val city: String?,
    val stateProvince: String?,
    val postalCode: String?,
    val country: String?,
    val longitude: Double?,
    val latitude: Double?,
    val phone: String?,
    val websiteUrl: String?,
)
