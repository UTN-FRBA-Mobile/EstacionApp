package com.example.estacionapp

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.layout_bottom_sheet.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var geocoder: Geocoder
    private lateinit var socket: Socket
    private var positions: ArrayList<Marker> = ArrayList()
    private var reservedPosition: Marker? = null
    private lateinit var dialog: BottomSheetDialog

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        this.myLocationButton.setOnClickListener {
            val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
        }
        this.reportButton.setOnClickListener{
            //open fragment to report
        }

        val opts = IO.Options()
        opts.transports = arrayOf(WebSocket.NAME)
        opts.query = "room=parkings"

        try {
            socket = IO.socket("http://192.168.1.41:5000", opts)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        socket.on(Socket.EVENT_CONNECT, Emitter.Listener {
            println("Connect ..............................")
        })

        socket.on(Socket.EVENT_DISCONNECT, Emitter.Listener {
            println("Disconnect ..............................")
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, Emitter.Listener {
            println("Error ..............................")
        });

        socket.on(Socket.EVENT_CONNECT_TIMEOUT, Emitter.Listener {
            println("Timeout ..............................")
        });

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
            placeMarkerOnMap(
                LatLng(
                    item.getDouble("latitude"),
                    item.getDouble("longitude")
                )
            )
        }
    }

    private var onNewParking = Emitter.Listener {
        val data = JSONObject(it[0].toString())

        runOnUiThread {
            placeMarkerOnMap(LatLng(data.getDouble("latitude"), data.getDouble("longitude")))
        }
    }

    private fun areEqual(position1: JSONObject?, position2: JSONObject?): Boolean {
        if (position1 == null || position2 == null) return false;
        return position1.get("latitude") == position2.get("latitude") && position1.get("longitude") == position2.get(
            "longitude"
        )
    }

    private fun areEqual(position1: Marker?, position2: JSONObject?): Boolean {
        if (position1 == null || position2 == null) return false;
        return position1.position.latitude == position2.get("latitude") && position1.position.longitude == position2.get(
            "longitude"
        )
    }

    private fun areEqual(position1: Marker?, position2: Marker?): Boolean {
        if (position1 == null || position2 == null) return false;
        return position1.position.latitude == position2.position.latitude && position1.position.longitude == position2.position.longitude
    }

    private var onDeleteParking = Emitter.Listener {
        val deletedPosition = JSONObject(it[0].toString())

        val deletedMarker = positions.find { marker -> areEqual(marker, deletedPosition) }
        deletedMarker?.remove()
        positions.remove(deletedMarker)
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

        view.close.setOnClickListener {
            dialog.dismiss()
        }

        view.reservarAhoraButton.tag = marker
        val isReserved = areEqual(marker, reservedPosition)
        if (isReserved) {
            view.title.text = "Lugar reservado"
            view.reservarAhoraButton.text = "Cancelar reserva"
            view.reservarAhoraButton.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
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
                val currentLatLng = LatLng(location.latitude, location.longitude)

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        }
    }

    private fun placeMarkerOnMap(location: LatLng, isEmpty: Boolean = true) {
        val markerOptions = MarkerOptions().position(location)

        markerOptions.icon(
            BitmapDescriptorFactory.fromResource(
                if (isEmpty) R.drawable.empty_place else R.drawable.reserved_place
            )
        )

        val marker = map.addMarker(markerOptions)
        positions.add(marker)
    }

    private fun getAddress(latLng: LatLng): String {
        val addresses: List<Address>? = geocoder.getFromLocation(
            latLng.latitude,
            latLng.longitude,
            1
        )
        val address: Address?
        var addressText = ""

        if (addresses != null) {
            address = addresses[0]
            addressText = address.getAddressLine(0)
        }

        return addressText
    }

    override fun onDestroy() {
        super.onDestroy()

        socket.disconnect()

        socket.off("locations", onNewParking)
    }

    private fun reserveParking(view: View) {
        val marker = view.tag as Marker
        val body = JSONObject()
        body.put("latitude", marker.position.latitude)
        body.put("longitude", marker.position.longitude)

        socket.emit("reserve_location", body)

        reservedPosition = marker
        dialog.dismiss()

        marker.remove()
        positions.remove(marker)

        runOnUiThread {
            placeMarkerOnMap(
                LatLng(
                    marker.position.latitude,
                    marker.position.longitude
                ), false
            )
        }

    }

    private fun cancelParking(view: View) {
        val positionId = view.tag as Int
        val body = JSONObject()
        body.put("id", positionId)
    }
}