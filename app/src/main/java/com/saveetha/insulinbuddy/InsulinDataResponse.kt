package com.saveetha.insulinbuddy

data class InsulinDataResponse(
    val username: String,
    val range: String,
    val from: String,
    val to: String,
    val data: List<InsulinDataEntry>
)

data class InsulinDataEntry(
    val date: String,
    val value: Int
)