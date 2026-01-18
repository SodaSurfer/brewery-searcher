package com.brewery.searcher.core.data.mapper

import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.model.BreweryType
import com.brewery.searcher.core.network.dto.BreweryDto

fun BreweryDto.toDomain(): Brewery = Brewery(
    id = id,
    name = name,
    breweryType = breweryType?.toBreweryType() ?: BreweryType.UNKNOWN,
    address = address1,
    city = city,
    stateProvince = stateProvince,
    postalCode = postalCode,
    country = country,
    longitude = longitude?.toDoubleOrNull(),
    latitude = latitude?.toDoubleOrNull(),
    phone = phone,
    websiteUrl = websiteUrl,
)

fun String.toBreweryType(): BreweryType = try {
    BreweryType.valueOf(uppercase())
} catch (e: IllegalArgumentException) {
    BreweryType.UNKNOWN
}
