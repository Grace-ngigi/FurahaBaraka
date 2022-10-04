package com.sais.furahabaraka.activities

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.sais.furahabaraka.databinding.ActivityHomePageBinding
import com.sais.furahabaraka.utils.Convertion

class HomePageActivity : BaseActivity() {
	lateinit var binding: ActivityHomePageBinding
	private lateinit var fusedLocation: FusedLocationProviderClient


	override fun onCreate(savedInstanceState: Bundle?) {
		binding = ActivityHomePageBinding.inflate(layoutInflater)
		super.onCreate(savedInstanceState)
		setContentView(binding.root)

		fusedLocation = LocationServices.getFusedLocationProviderClient(this)
		if (!isLocationGranted()) {
			Convertion().requestDeviceLocationSettings(this)
		}

		binding.flFields.setOnClickListener {
			startActivity(Intent(this, AccessData::class.java))
		}

		binding.flMaps.setOnClickListener {
			startActivity(Intent(this, MapsActivity::class.java))
		}
		binding.flProfile.setOnClickListener {
			startActivity(Intent(this, MyProfileActivity::class.java))
		}
		binding.ivSignOut.setOnClickListener {
			FirebaseAuth.getInstance().signOut()
			val intent = Intent(this, SignInActivity::class.java)
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
			startActivity(intent)
			finish()
		}
	}

	override fun onBackPressed() {
		doubleBackToExit()
	}

	override fun onResume() {
		super.onResume()
		if (!isLocationGranted()) {
			Convertion().requestDeviceLocationSettings(this)
		}
	}

	private fun isLocationGranted(): Boolean {
		val locationManager: LocationManager =
			getSystemService(Context.LOCATION_SERVICE) as LocationManager
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
				locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
	}
}