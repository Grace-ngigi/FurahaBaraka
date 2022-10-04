package com.sais.furahabaraka.utils

import android.app.Activity
import android.app.Dialog
import android.content.IntentSender
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

class Convertion() {
	private  lateinit var mProgressDialog: Dialog

	// request device
	fun requestDeviceLocationSettings(activity: Activity) {
		val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
			interval = 10000
			fastestInterval = 5000
			priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
		}

		val builder = LocationSettingsRequest.Builder()
				builder.setAlwaysShow(false)
			.addLocationRequest(locationRequest)


		val client: SettingsClient = LocationServices.getSettingsClient(activity)
		val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

		task.addOnSuccessListener { locationSettingsResponse ->
			val state = locationSettingsResponse.locationSettingsStates
		}

		task.addOnFailureListener { exception ->
			if (exception is ResolvableApiException) {
//				showToast("TURN ON LOCATION!!", activity)
				try {
					// Show the dialog by calling startResolutionForResult(),
					// and check the result in onActivityResult().
					exception.startResolutionForResult(
						activity,
						100
					)
				} catch (sendEx: IntentSender.SendIntentException) {
					// Ignore the error.
//					textView.text = sendEx.message.toString()
				}
			}
		}

	}

	fun showToast(msg: String, activity: Activity) {
		Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
	}
}