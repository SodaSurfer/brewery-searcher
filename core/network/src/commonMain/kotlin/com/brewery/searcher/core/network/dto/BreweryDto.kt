package com.brewery.searcher.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BreweryDto(
    val id: String,
    val name: String,
    @SerialName("brewery_type") val breweryType: String? = null,
    @SerialName("address_1") val address1: String? = null,
    val city: String? = null,
    @SerialName("state_province") val stateProvince: String? = null,
    @SerialName("postal_code") val postalCode: String? = null,
    val country: String? = null,
    val longitude: String? = null,
    val latitude: String? = null,
    val phone: String? = null,
    @SerialName("website_url") val websiteUrl: String? = null,
)
