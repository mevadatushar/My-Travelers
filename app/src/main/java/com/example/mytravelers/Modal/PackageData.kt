package com.example.mytravelers.Modal

data class PackageData(
    val packageId: String = "",
    val images: List<String> = emptyList(),
   // val imageUrl : ArrayList<String> = ArrayList(),
    val place: String = "",
    val price: Int = 0,
    val days: Int = 0,
    val phone : String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)