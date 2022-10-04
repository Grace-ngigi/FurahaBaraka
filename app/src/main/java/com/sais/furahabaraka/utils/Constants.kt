package com.sais.furahabaraka.utils

import android.app.Activity
import android.net.Uri
import android.webkit.MimeTypeMap

object Constants {
	const val CAMERA = 1
	const val REQUEST_LOCATION_CODE = 2
	const val REQUEST_CHECK_SETTINGS = 3
	const val REQUEST_FOR_VISIT_UPDATE = 4
	const val CREATE_FIELD_REQUEST_CODE = 5
	const val CREATE_VISIT_REQUEST_CODE = 6
	const val CREATE_TREE_REQUEST_CODE = 7
	var ADD_VISIT_ACTIVITY_REQUEST = 8
	var ADD_TREE_ACTIVITY_REQUEST = 9
	var ADD_FIELD_ACTIVITY_REQUEST = 10
	const val IMAGE_DIRECTORY = "TreeImages"
	const val EXTRA_FIELD_DETAILS = "extra_field_details"
	const val EXTRA_PROJECT_DETAILS = "extra_project_details"
	const val EXTRA_TREE_DETAILS = "extra_tree_details"
	const val EXTRA_VISIT_DETAILS = "extra_visit_details"
	const val EXTRA_USER_DETAILS= "user"

	const val USERS = "Users"
	const val FIELDS = "Fields"
	const val TREES = "Trees"
	const val VISITS = "Visits"
	const val FIELD_IMAGE = "Field_Image"
	const val VISIT_IMAGE = "Visit_Image"
	const val FARM_ID = "farmId"
	const val FIELD_VISITS = "fieldVisits"
	const val NAME= "name"
	const val PHONE = "phone"

	val fruits = arrayOf("Mango" , "Lemon", "Tangerine","Lime" , "Oranges" , "Yellow Passion", "Purple Passion" ,
			"Pawpaw" , "Guava" , "Avocado" , "Jamun/Black Plum/Zambarau")
	val exotic = arrayOf("Cyprus" , "Gravillea" , "Bishop" , "Casuarina" , "Pine" , "Blue gum" , "Arborea")
	val indigenous = arrayOf("Prunus Africana" , "Olea Africana" , "White bottlebrush " , "Red Bottlebrush" , "Podo" ,
		"Meru Oak" , "Ekebergia Capensis" , "Dombea" , "Syzygium" , "Elgon teak" , "Nandi flame" , "African Greenheart" ,
		"Croton Megalocarpus" , "Markhamia Lutea" , "Siala" , "Neem/Mkilifi/Mwarubaini" , "Matumaywa" , "Mraba")
	val status = arrayOf("Not Planted" , "Planted" , "In Progress", "Damaged" , "Dead" , "Completed")

	fun getFileExtension(activity: Activity, uri: Uri?): String? {
		return MimeTypeMap.getSingleton()
			.getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
	}
}

