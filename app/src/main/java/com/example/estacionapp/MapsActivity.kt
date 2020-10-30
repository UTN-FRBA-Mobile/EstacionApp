package com.example.estacionapp

import android.content.Intent
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.estacionapp.auth.AuthActivity
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.layout_bottom_sheet.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

fun Marker.areEqualTo(otherMarker: Marker?) = otherMarker != null && this.position == otherMarker.position
fun Marker.areEqualTo(latLng: LatLng?) = latLng != null && this.position == latLng
fun Marker.areEqualTo(positionObject: JSONObject?): Boolean = positionObject != null && this.position.latitude == positionObject.getDouble("latitude") && this.position.longitude == positionObject.getDouble("longitude")

fun JSONArray.toArrayListOfStrings(): ArrayList<String> {
    val photos: ArrayList<String> = arrayListOf<String>()
    for (j in 0 until this.length()) photos.add(this[j] as String)
    return photos
}

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private lateinit var geocoder: Geocoder
    private lateinit var socket: Socket
    private var positions: ArrayList<Pair<Marker, ArrayList<String>>> = ArrayList()
    private var reservedPosition: Marker? = null
    private lateinit var dialog: BottomSheetDialog
    private lateinit var preferences: SharedPreferences

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        this.myLocationButton.setOnClickListener {
            val currentLatLng: LatLng
            if (!LocationManagerCompat.isLocationEnabled(getSystemService(Context.LOCATION_SERVICE) as LocationManager)) {
                val dialog = AlertDialog.Builder(this)
                    .setTitle("Ubicación desactivada")
                    .setMessage("Debes activar la ubicación para poder ver los lugares libres cercanos")
                    .setPositiveButton("OK", null)
                    .create()
                dialog.show()
            } else if (lastLocation !== null) {
                currentLatLng = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }

        this.reportButton.setOnClickListener{
            startActivity(Intent(this, AuthActivity::class.java))
        }

        val opts = IO.Options()
        opts.transports = arrayOf(WebSocket.NAME)
        opts.query = "room=parkings"

        try {
            socket = IO.socket(getString(R.string.server_url), opts)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        socket.on(Socket.EVENT_CONNECT) { println("Socket Connect...") }
        socket.on(Socket.EVENT_DISCONNECT) { println("Socket Disconnect...") }
        socket.on(Socket.EVENT_CONNECT_ERROR) { println("Socket Error...") }
        socket.on(Socket.EVENT_CONNECT_TIMEOUT) { println("Socket Timeout...") }

        socket.on("initial_locations", onInitialParkings)
        socket.on("new_locations", onNewParking)
        socket.on("deleted_locations", onDeleteParking)

        socket.connect()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        geocoder = Geocoder(this, Locale.getDefault())
    }

    private var onInitialParkings = Emitter.Listener {
        val data = JSONArray(it[0].toString())

        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            val photos = item.getJSONArray("photos").toArrayListOfStrings()
            placeMarkerOnMap(LatLng(item.getDouble("latitude"), item.getDouble("longitude")), photos)
        }
    }

    private var onNewParking = Emitter.Listener {
        val data = JSONObject(it[0].toString())
        val photos = data.getJSONArray("photos").toArrayListOfStrings()
        placeMarkerOnMap(LatLng(data.getDouble("latitude"), data.getDouble("longitude")), photos)
    }

    private var onDeleteParking = Emitter.Listener { deletedBody ->
        val deletedPosition = JSONObject(deletedBody[0].toString())

        runOnUiThread {
            val deletedMarker = positions.find { it.first.areEqualTo(deletedPosition) && !it.first.areEqualTo(reservedPosition) }
            deletedMarker?.first?.remove()
            positions.remove(deletedMarker)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMarkerClickListener(this)
        setUpMap()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet, null)

        val photos = positions.find { it.first.areEqualTo(marker) }

        view.close.setOnClickListener {
            dialog.dismiss()
        }

        photos?.second?.take(3)?.forEachIndexed { index, photo ->
            Picasso.get()
                .load(photo)
                .placeholder(R.drawable.parking_placeholder)
                .resize(130, 130)
                .into(view.findViewById(resources.getIdentifier("photo${index}", "id", packageName)) as ImageView)
        }

        view.reservarAhoraButton.tag = marker
        val isReserved = marker.areEqualTo(reservedPosition)
        if (isReserved) {
            view.title.text = getString(R.string.lugar_reservado)
            view.reservarAhoraButton.text = getString(R.string.cancelar_reserva)
            view.reservarAhoraButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
            view.reservarAhoraButton.strokeColor = ContextCompat.getColorStateList(
                this,
                R.color.primary
            )
            view.reservarAhoraButton.strokeWidth = 1
            view.reservarAhoraButton.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.primary
                )
            )
        }

        view.reservarAhoraButton.setOnClickListener {
            if (isReserved) cancelParking(view.reservarAhoraButton) else reserveParking(view.reservarAhoraButton)
        }

        view.address.text = getAddress(marker.position)
        dialog.setContentView(view)
        dialog.show()

        return false
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                preferences
                    .edit()
                    .putFloat("lastLatitude", location.latitude.toFloat())
                    .putFloat("lastLongitude", location.longitude.toFloat())
                    .apply()
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 16f))
            } else {
                val locationPreferences = LatLng(
                    preferences.getFloat("lastLatitude", 0F).toDouble(),
                    preferences.getFloat("lastLongitude", 0F).toDouble()
                )
                if (locationPreferences.latitude != .0 && locationPreferences.longitude != .0) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(locationPreferences, 16f))
                }
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng, photos: ArrayList<String>, isEmpty: Boolean = true) {
        val markerOptions = MarkerOptions().position(location)

        markerOptions.icon(
            BitmapDescriptorFactory.fromResource(
                if (isEmpty) R.drawable.empty_place else R.drawable.reserved_place
            )
        )

        runOnUiThread {
            val prevMarker = positions.find { it.first.areEqualTo(location) }
            prevMarker?.first?.remove()
            positions.remove(prevMarker)
            val marker = map.addMarker(markerOptions)
            positions.add(Pair(marker, photos))
        }
    }

    private fun getAddress(latLng: LatLng): String {
        val addresses: List<Address>? = geocoder.getFromLocation(
            latLng.latitude,
            latLng.longitude,
            1
        )
        return if (addresses != null) addresses[0].getAddressLine(0) else ""
    }

    private fun reserveParking(view: View) {
        val marker = view.tag as Marker
        val body = JSONObject()
        val prevReservedBody = JSONObject()
        val photos = positions.find { it.first.areEqualTo(marker) }?.second

        body.put("latitude", marker.position.latitude)
        body.put("longitude", marker.position.longitude)

        socket.emit("reserve_location", body)

        if (reservedPosition !== null) {
            prevReservedBody.put("latitude", reservedPosition!!.position.latitude)
            prevReservedBody.put("longitude", reservedPosition!!.position.longitude)
            socket.emit("cancel_reserve_location", prevReservedBody)
        }

        reservedPosition = marker
        dialog.dismiss()

        Toast.makeText(this, R.string.confirmacion_reserva, Toast.LENGTH_SHORT).show()
        placeMarkerOnMap(LatLng(marker.position.latitude, marker.position.longitude), photos!!,
            false)
    }

    private fun cancelParking(view: View) {
        val body = JSONObject()

        if (reservedPosition !== null) {
            body.put("latitude", reservedPosition!!.position.latitude)
            body.put("longitude", reservedPosition!!.position.longitude)
            socket.emit("cancel_reserve_location", body)
        }

        reservedPosition = null
        dialog.dismiss()

        Toast.makeText(this, R.string.confirmacion_reserva_cancelada, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
        socket.off("locations", onNewParking)
    }
}