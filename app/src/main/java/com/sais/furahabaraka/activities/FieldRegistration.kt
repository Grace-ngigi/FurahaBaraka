package com.sais.furahabaraka.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.sais.furahabaraka.R
import com.sais.furahabaraka.databinding.ActivityFieldRegistrationBinding
import com.sais.furahabaraka.firebase.Fields
import com.sais.furahabaraka.firebase.FireStore
import com.sais.furahabaraka.utils.Constants
import com.sais.furahabaraka.utils.Constants.IMAGE_DIRECTORY
import com.sais.furahabaraka.utils.Convertion
import java.io.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class FieldRegistration : BaseActivity() {
    lateinit var binding: ActivityFieldRegistrationBinding
    private var mFieldImageUrl: String =""
    private var mSelectedImageUrl : Uri? = null
    private var mCurrentLocation: Location? = null
    private val mFireStore = FirebaseFirestore.getInstance()
    private var fields: Fields? = null
    private var cal = Calendar.getInstance()
    private lateinit var fusedLocation: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFieldRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(Constants.EXTRA_FIELD_DETAILS)){
            fields = intent.getParcelableExtra(Constants.EXTRA_FIELD_DETAILS)!!
        }
        if (fields != null) {
            binding.etBranchName.setText(fields!!.branchName)
            binding.etCounty.setText(fields!!.county)
            binding.etSubCounty.setText(fields!!.subCounty)
            binding.etFieldName.setText(fields!!.fieldName)
            binding.etFieldID.setText(fields!!.farmId)
            binding.etFieldSize.setText(fields!!.size.toString())
            binding.etFruitsSize.setText(fields!!.fruits.toString())
            binding.etExoticNumber.setText(fields!!.exotic.toString())
            binding.etIndigenousNumber.setText(fields!!.indigenous.toString())
//            binding.ivShowImage.setImageURI(Uri.parse(fields!!.image))
            Glide
                .with(this)
                .load(fields!!.image)
                .centerCrop()
                .placeholder(R.drawable.ic_person_add_24)
                .into(binding.ivShowImage)
            mFieldImageUrl = fields!!.image
            mSelectedImageUrl = Uri.parse(fields!!.image)
            binding.editTextTextMultiLine.setText(fields!!.description)
            binding.btsubmit.visibility = View.GONE
            binding.btUpdate.visibility = View.VISIBLE
        }
        setSupportActionBar(binding.tbFieldRegistration)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Field Registration"
        binding.tbFieldRegistration.setNavigationOnClickListener { onBackPressed() }
        binding.btsubmit.setOnClickListener {
//            checkInternetConnectivity()
        }
        binding.ibTakePhoto.setOnClickListener {
            takePhoto()
        }
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        requestLocationData()

        binding.btsubmit.setOnClickListener {
            if (checkInternetConnectivity()) {
                    addField()
                }
        }
        binding.btUpdate.setOnClickListener {
            if (checkInternetConnectivity()) {
                    updateFieldHashMap()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isLocationGranted()){
            Convertion().requestDeviceLocationSettings(this)
        }
    }

    private fun isLocationGranted(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE)as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ( resultCode == Activity.RESULT_OK){
            if (requestCode == Constants.CAMERA){
                val thumbNail : Bitmap = data!!.extras!!.get("data") as Bitmap
                if (checkInternetConnectivity()) {
                    mSelectedImageUrl =  saveImage(  this, thumbNail)
                    uploadImageToFirestore()
                    binding.ivShowImage.setImageBitmap(thumbNail)
                }
                }
            }
        }

    private fun takePhoto(){
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()){
                        //TODO: inform user permission granted
                        val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(galleryIntent, Constants.CAMERA)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?) {
                    token?.continuePermissionRequest()
                }
            }).onSameThread().check()
    }

    private fun requestLocationData() {
        val mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
                getLocationPermission()
            return
        }
        fusedLocation.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()!!
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            mCurrentLocation = locationResult.lastLocation!!
            val latitude = mCurrentLocation!!.latitude
            binding.tvLatitude.text = latitude.toString()
            Log.i("Current Latitude", "$latitude")

            val longitude = mCurrentLocation!!.longitude
            binding.tvLongitide.text = longitude.toString()
            Log.i("Current Longitude", "$longitude")
        }
    }

    private fun getLocationPermission() {
        if (!isLocationGranted()) {
showErrorSnackBar( "Your location provider is turned off. Please turn it on.")
            // This will redirect you to settings from where you need to turn on the location provider.
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName,null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException){
                e.printStackTrace()
            }
        } else {
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            requestLocationData()
                        }

                        if (report.isAnyPermissionPermanentlyDenied) {
                            showErrorSnackBar("You have denied location permission. Please allow it is mandatory.")
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()
        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage("" + "Furaha and Baraka collects location data to enable trees location tracking even when the app is closed or not in use")
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

//    private fun getCurrentDate() : String {
//        var currentDate = ""
//        val myFormat = "dd.MM.yyyy"
//        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
//        currentDate= sdf.format(cal.time).toString()
//        return currentDate
//    }

    private fun addField() {
        val id = mFireStore.collection(Constants.FIELDS).document().id
        val county = binding.etCounty.text.toString()
        val subCounty = binding.etSubCounty.text.toString()
        val branch = binding.etBranchName.text.toString()
        val currentUser = FireStore().getCurrentUserId()
        val date = LocalDate.now().toString()
        val fieldName = binding.etFieldName.text.toString()
        val farmId = binding.etFieldID.text.toString()
        val fieldSize = binding.etFieldSize.text.toString()
        val fruits = binding.etFruitsSize.text.toString()
        val exotic = binding.etExoticNumber.text.toString()
        val indigenous = binding.etIndigenousNumber.text.toString()
        val description = binding.editTextTextMultiLine.text.toString()

        when {
            fieldName.isEmpty() -> {
                showErrorSnackBar("Please enter the Name of the Field Owner")
            }
            county.isEmpty() -> {
                showErrorSnackBar("Please enter the County Name")
            }
            subCounty.isEmpty() -> {
                showErrorSnackBar("Please enter the Sub County Name")
            }
            branch.isEmpty() -> {
                showErrorSnackBar("Please enter the Branch Name")
            }
            farmId.isEmpty() -> {
                showErrorSnackBar("Please enter the Field's Id")
            }
            fieldSize.isEmpty() -> {
                showErrorSnackBar("Please enter Field Size")
            }
            fruits.isEmpty() -> {
                showErrorSnackBar("Please enter number of Fruit Trees")
            }
            exotic.isEmpty() -> {
                showErrorSnackBar("Please enter number of Exotic Trees")
            }
            indigenous.isEmpty() -> {
                showErrorSnackBar("Please enter number of indigenous Trees")
            }
            description.isEmpty() -> {
                showErrorSnackBar("Please enter a Description")
            }
            mSelectedImageUrl == null -> {
                showErrorSnackBar("Please Take a  Photo")
            }
            mFieldImageUrl  == null ->{
                showErrorSnackBar("Please Take another Photo")
            }
            mCurrentLocation == null -> {
                showErrorSnackBar("Please wait for gps coordinates")
            }

            else -> {
                showProgressDialog(resources.getString(R.string.please_wait))
                val fields = Fields(branch,
                    farmId, fieldName, fieldSize.toInt(),
                    exotic.toInt(), indigenous.toInt(), fruits.toInt(),
                    description,county,subCounty, mFieldImageUrl,
                    mCurrentLocation!!.latitude,
                    mCurrentLocation!!.longitude,
                    currentUser, date, id
                )
                FireStore().saveFieldToFirebase(this@FieldRegistration, fields)
            }
        }
    }

    fun fieldCreatedSuccess(){
        setResult(Activity.RESULT_OK)
        hideProgressDialog()
        finish()
    }

    private fun saveImage(context: Context, image: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, image, "${UUID.randomUUID()}", null)
        return Uri.parse(path)
    }

    private fun uploadImageToFirestore(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageUrl != null){
            val ref : StorageReference = FirebaseStorage.getInstance()
                .reference.child(Constants.FIELD_IMAGE + System.currentTimeMillis())
            ref.putFile(mSelectedImageUrl!!).addOnSuccessListener {
                    snapshot ->
                Log.i("Firebase", snapshot.metadata!!.reference!!.downloadUrl.toString())
                snapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    Log.i("Downloadable Image", uri.toString())
                    mFieldImageUrl = uri.toString()
                    hideProgressDialog()
//                    sendToFirebase()
                }.addOnFailureListener {
                        e-> e.message?.let { showErrorSnackBar(it) }
                }
            }.addOnFailureListener{
                e-> e.message?.let { showErrorSnackBar(it) }
                hideProgressDialog()
            }
        }
    }

    private fun updateFieldHashMap(){
        val userHashMap = HashMap<String, Any>()
        var anyChangesMade = false
        if (binding.etCounty.text.toString() != fields!!.county){
            userHashMap["county"] = binding.etCounty.text.toString()
            anyChangesMade = true
        }
        if (binding.etSubCounty.text.toString() != fields!!.subCounty){
            userHashMap["subCounty"] = binding.etSubCounty.text.toString()
            anyChangesMade = true
        }
        if (binding.etBranchName.text.toString() != fields!!.branchName){
            userHashMap["branchName"] = binding.etBranchName.text.toString()
            anyChangesMade = true
        }
        if (binding.etFieldName.text.toString() != fields!!.fieldName){
            userHashMap["fieldName"] = binding.etFieldName.text.toString()
            anyChangesMade = true
        }
        if (binding.etFieldSize.text.toString() != fields!!.size.toString()){
            userHashMap["size"] = binding.etFieldSize.text.toString().toInt()
            anyChangesMade = true
        }
        if (binding.etExoticNumber.text.toString() != fields!!.exotic.toString()){
            userHashMap["exotic"] = binding.etExoticNumber.text.toString().toInt()
            anyChangesMade = true
        }
        if (binding.etFruitsSize.text.toString() != fields!!.fruits.toString()){
            userHashMap["fruits"] = binding.etFruitsSize.text.toString().toInt()
            anyChangesMade = true
        }
        if (binding.etIndigenousNumber.text.toString() != fields!!.indigenous.toString()){
            userHashMap["indigenous"] = binding.etIndigenousNumber.text.toString().toInt()
            anyChangesMade = true
        }
        if (mFieldImageUrl != fields!!.image){
            userHashMap["image"] = mFieldImageUrl
            anyChangesMade = true
        }
        if (binding.editTextTextMultiLine.text.toString() != fields!!.description){
            userHashMap["description"] = binding.editTextTextMultiLine.text.toString()
            anyChangesMade = true
        }
        if (anyChangesMade) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStore().updateField(this, userHashMap, fields!!)
        } else{
            Toast.makeText(this, "No changes Made", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun updateFieldSuccess(){
        setResult(Activity.RESULT_OK)
        hideProgressDialog()
        finish()
    }
}