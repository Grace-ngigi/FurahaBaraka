package com.sais.furahabaraka.activities

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.sais.furahabaraka.R
import com.sais.furahabaraka.databinding.ActivityMapsBinding
import com.sais.furahabaraka.firebase.FireStore
import com.sais.furahabaraka.firebase.Trees

class MapsActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

	private lateinit var mMap: GoogleMap
	private lateinit var binding: ActivityMapsBinding
	private lateinit var fusedLocation: FusedLocationProviderClient
	lateinit var currentLocation: Location
	lateinit var visits : ArrayList<Trees>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMapsBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		val mapFragment = supportFragmentManager
			.findFragmentById(R.id.map) as SupportMapFragment
		mapFragment.getMapAsync(this)
		fusedLocation = LocationServices.getFusedLocationProviderClient(this)
//		FireStore().getVisitsList(this)
	}
	private fun getLocation() {
		if (ActivityCompat.checkSelfPermission(
				this@MapsActivity,
				Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
				this@MapsActivity,
				Manifest.permission.ACCESS_COARSE_LOCATION
			) != PackageManager.PERMISSION_GRANTED
		) {
			getLocationPermission()
			return
		}
		if (isLocationGranted()) {
			val task = fusedLocation.lastLocation
			task.addOnSuccessListener(this@MapsActivity) { location ->
				if (location != null) {
					currentLocation = location
					val loc = LatLng(currentLocation.latitude, currentLocation.longitude)
					placeMarkerOnMap(loc, "CurrentLocation")

//					mMap.addMarker(MarkerOptions().position(loc).title("Mark"))
					mMap.moveCamera(CameraUpdateFactory.newLatLng(loc))
					mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 9f))


					Log.i("location", location.latitude.toString())
				}
				Log.e("location", task.toString())
			}
		}
	}

	private fun placeMarkerOnMap(currentLocation: LatLng, userInfo: String){
		val markerOptions = MarkerOptions().position(currentLocation)
		markerOptions.title(userInfo)
		mMap.addMarker(markerOptions)
	}

	fun fetchLocationFromDatabase(visitInfo: ArrayList<Trees>){
		visits = visitInfo
		for (visit in visits) {
			val name = visit.treeName
			val type = visit.treeType
//			vInfo.add(name)
//			vInfo.add(type)
//			vInfo.add(status)

			val location = LatLng(visit.latitude, visit.longitude)
			placeMarkerOnMap(location, name)
//			mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
		}
//		showProgressDialog(resources.getString(R.string.please_wait))
	}

	override fun onMapReady(googleMap: GoogleMap) {
		mMap = googleMap
		mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
		mMap.uiSettings.isCompassEnabled = true
		mMap.setOnMarkerClickListener(this)
		getLocation()
		FireStore().getTreesList(this)

	}

	private fun getLocationPermission(){
		Dexter.withActivity(this)
			.withPermissions(
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION)
			.withListener(object : MultiplePermissionsListener {
				override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
					if (report!!.areAllPermissionsGranted()){
						//TODO: inform user permission granted
						getLocation()
					}
				}
				override fun onPermissionRationaleShouldBeShown(
					permissions: MutableList<PermissionRequest>?,
					token: PermissionToken?) {
					token?.continuePermissionRequest()
					showRationalDialogForPermissions()
				}
			}).onSameThread().check()
	}

	private fun showRationalDialogForPermissions() {
		AlertDialog.Builder(this).setMessage("" + "Furaha and Baraka collects location data to enable tracking of the trees visited even when the app is closed or not in use")
			.setPositiveButton("Go to Settings"){_,_ ->
				try {
					val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
					val uri = Uri.fromParts("package", packageName,null)
					intent.data = uri
					startActivity(intent)
				} catch (e: ActivityNotFoundException){
					e.printStackTrace()
				}
			}.setNegativeButton("Cancel") { dialog, _ ->
				dialog.dismiss()
			}.show()
	}

	private fun isLocationGranted(): Boolean{
		val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE)as LocationManager
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
			LocationManager.NETWORK_PROVIDER)
	}

	override fun onMarkerClick(p0: Marker) = false
}
