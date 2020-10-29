package com.example.estacionapp.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import org.json.JSONArray
import org.json.JSONObject

fun Marker.areEqualTo(otherMarker: Marker?) = otherMarker != null && this.position == otherMarker.position
fun Marker.areEqualTo(latLng: LatLng?) = latLng != null && this.position == latLng
fun Marker.areEqualTo(positionObject: JSONObject?): Boolean = positionObject != null && this.position.latitude == positionObject.getDouble("latitude") && this.position.longitude == positionObject.getDouble("longitude")

fun JSONArray.toArrayListOfStrings(): ArrayList<String> {
    val array: ArrayList<String> = arrayListOf()
    for (j in 0 until this.length()) array.add(this[j] as String)
    return array
}