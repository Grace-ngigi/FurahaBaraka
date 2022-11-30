package com.sais.furahabaraka.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
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
import com.sais.furahabaraka.databinding.ActivityTreeRegistrationBinding
import com.sais.furahabaraka.firebase.Fields
import com.sais.furahabaraka.firebase.FireStore
import com.sais.furahabaraka.firebase.Trees
import com.sais.furahabaraka.utils.Constants
import com.sais.furahabaraka.utils.Convertion
import java.io.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class TreeRegistration : BaseActivity() {
    private var binding: ActivityTreeRegistrationBinding? = null
    private var cal = Calendar.getInstance()
//    private  var saveImageToStorage: Uri? = null
    private var currentLocation: Location? = null
    private var currentDate : LocalDate? = null
    private var trees: Trees? = null
    private var mTreeImageUrl: String =""
    private var mSelectedImageUrl : Uri? = null
    private var field : Fields? = null
    private  var treeName: String? = null
    var farmId: String? = null
    private val mFireStore = FirebaseFirestore.getInstance()
    private lateinit var treeNameSpinner: Spinner
    private lateinit var statusSpinner: Spinner
    private lateinit var fusedLocation: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTreeRegistrationBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding?.root)

        treeNameSpinner = binding?.spTreeName!!

        setSupportActionBar(binding?.tbAddVisits)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Tree Registration"
        binding?.tbAddVisits?.setNavigationOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(Constants.EXTRA_FIELD_DETAILS)) {
            field = intent.getParcelableExtra<Fields>(Constants.EXTRA_FIELD_DETAILS)!!
        }

        if (intent.hasExtra(Constants.EXTRA_TREE_DETAILS)) {
            trees = intent.getParcelableExtra<Trees>(Constants.EXTRA_TREE_DETAILS)!!
        }
        if (trees != null){
            binding?.llTreeName?.visibility = View.VISIBLE
            treeName = trees!!.treeName
            loadDataIntoRadioButton()
            farmId = trees!!.farmId
            binding?.etDate?.setText(trees!!.timeStamp)
            binding?.etTreeDescription?.setText(trees!!.description)
//            binding?.ivShowVisitImage?.setImageURI(Uri.parse(trees!!.image))
            Glide
                .with(this)
                .load(trees!!.image)
                .centerCrop()
                .placeholder(R.drawable.ic_person_add_24)
                .into(binding?.ivShowVisitImage!!)
            mSelectedImageUrl = Uri.parse(trees!!.image)
            mTreeImageUrl = trees!!.image
            binding?.btVisitSubmit?.visibility = View.GONE
            binding?.btUpdate?.visibility = View.VISIBLE
        }
            currentDate = LocalDate.now()
            binding?.etDate?.setText(currentDate.toString())

        binding?.btVisitSubmit?.setOnClickListener {
            if (checkInternetConnectivity()) {
                saveTree()
            }
        }
        binding?.btUpdate?.setOnClickListener {
            if (checkInternetConnectivity()) {
                updateTreeHashMap()
            }
        }
        binding?.ivGetGps?.setOnClickListener { getLocation() }

        binding?.ibTakeVisitPhoto?.setOnClickListener { takePhotoFromGallery() }
        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        getLocation()
        showTreeName()
    }
    private fun showTreeName() {
        binding?.rgTreeType?.setOnCheckedChangeListener { _, checkedId: Int ->
            when (checkedId) {
                R.id.rdFruit -> {
                    binding?.llTreeName?.visibility = View.VISIBLE
                    val fruits = Constants.fruits
                    val fruitsAdapter = ArrayAdapter(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        fruits)
                    treeNameSpinner.adapter = fruitsAdapter

                    treeNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                treeName = fruits[position]
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                            TODO("Not yet implemented")
                        }

                    }
                }
                R.id.rdExotic -> {
                    binding?.llTreeName?.visibility = View.VISIBLE
                    val exotic = Constants.exotic
                    val exoticAdapter = ArrayAdapter(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        exotic)
                    treeNameSpinner.adapter = exoticAdapter

                    treeNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                treeName = exotic[position]
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                            TODO("Not yet implemented")
                        }

                    }
                }
                R.id.rdIndigenous -> {
                    binding?.llTreeName?.visibility = View.VISIBLE
                    val indigenous = Constants.indigenous
                    val indigenousAdapter = ArrayAdapter(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        indigenous)
                    treeNameSpinner.adapter = indigenousAdapter

                    treeNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                treeName = indigenous[position]
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                            TODO("Not yet implemented")
                        }
                    }                }
                else -> {
                    showErrorSnackBar("No Tree Type Chosen")
                }
            }
        }
    }

    private fun loadDataIntoRadioButton(){
        when (trees!!.treeType) {
            "Fruit" -> {
                binding?.rgTreeType?.check(R.id.rdFruit)
                val fruits = Constants.fruits
                val fruitsAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    fruits
                )
                treeNameSpinner.adapter = fruitsAdapter
                treeNameSpinner.setSelection(fruitsAdapter.getPosition(trees!!.treeName))
            }
            "Exotic" -> {
                binding?.rgTreeType?.check(R.id.rdExotic)
                val exotic = Constants.exotic
                val exoticAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    exotic
                )
                treeNameSpinner.adapter = exoticAdapter
                treeNameSpinner.setSelection(exoticAdapter.getPosition(trees!!.treeName))
            }
            "Indigenous" -> {
                binding?.rgTreeType?.check(R.id.rdIndigenous)
                val indigenous = Constants.indigenous
                val indigenousAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    indigenous
                )
                treeNameSpinner.adapter = indigenousAdapter
                treeNameSpinner.setSelection(indigenousAdapter.getPosition(trees!!.treeName))
            }
            else -> {
                showErrorSnackBar("No Tree Type Chosen")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isLocationGranted()){
            Convertion().requestDeviceLocationSettings(this)
        }
    }

    private fun getLocation(){
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
            currentLocation = locationResult.lastLocation
            val latitude = currentLocation!!.latitude
            binding?.tvLatitude?.text = latitude.toString()
            Log.i("Current Latitude", "$latitude")

            val longitude = currentLocation!!.longitude
            binding?.tvLongitide?.text = longitude.toString()
            Log.i("Current Longitude", "$longitude")
        }
    }

    private fun getLocationPermission(){
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
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

    private fun isLocationGranted(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE)as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
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

    private fun radioGroupText(): String{
        var treeType = ""
        val intSelectButton: Int = binding?.rgTreeType!!.checkedRadioButtonId
            val radioButton: RadioButton = findViewById(intSelectButton)
            treeType = radioButton.text.toString()

        return treeType
    }

    private fun saveTree() {
        val id  = mFireStore.collection(Constants.TREES).document().id
        val treeType = radioGroupText()
        val description = binding?.etTreeDescription?.text.toString()

        when {
                treeName!!.isEmpty() -> {
                    showErrorSnackBar("Please Select The Tree Name")
                }
                binding?.rgTreeType?.checkedRadioButtonId == null -> {
                    showErrorSnackBar("Please Select The Tree Type")
                }

                treeType.isEmpty() -> {
                    showErrorSnackBar("Please Select The Tree Type")
                }
                description.isEmpty() -> {
                    showErrorSnackBar("Please Enter Tree Description")
                }
                currentLocation == null -> {
                    showErrorSnackBar("Please wait for Gps Coordinates")
                }
                mSelectedImageUrl == null -> {
                    showErrorSnackBar("Please Take a Photo")
                }
                else -> {
                    showProgressDialog(resources.getString(R.string.please_wait))
                    val trees = Trees(
                        treeName!!, field!!.farmId, treeType,
                        currentDate.toString(),
                        description, mTreeImageUrl,
                        currentLocation!!.latitude,
                        currentLocation!!.longitude,
                        id
                    )
                    FireStore().saveTreeToFirebase(this@TreeRegistration, trees)
                }
            }
        }

    fun treeCreatedSuccess(){
        setResult(Activity.RESULT_OK)
        hideProgressDialog()
        finish()
    }

    private fun updateTreeHashMap(){
        val treeHashMap = HashMap<String, Any>()
        var anyChangesMade = false
        if (treeName != trees!!.treeName){
            treeHashMap["treeName"] = treeName!!
            anyChangesMade = true
        }
        if (radioGroupText() != trees!!.treeType){
            treeHashMap["treeType"] = radioGroupText()
            anyChangesMade = true
        }
        if (mTreeImageUrl != trees!!.image){
            treeHashMap["image"] = mTreeImageUrl
            anyChangesMade = true
        }
        if (binding?.etTreeDescription?.text.toString() != trees!!.description){
            treeHashMap["description"] = binding?.etTreeDescription?.text.toString()
            anyChangesMade = true
        }
        if (anyChangesMade) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FireStore().updateTree(this, treeHashMap, trees!!)
        } else {
            Toast.makeText(this, "No changes Made", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun updateTreeSuccess(){
        setResult(Activity.RESULT_OK)
        hideProgressDialog()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ( resultCode == Activity.RESULT_OK){
            if (requestCode == Constants.CAMERA){
                val thumbNail : Bitmap = data!!.extras!!.get("data") as Bitmap
                if (checkInternetConnectivity()) {
                    mSelectedImageUrl =  saveImageToStorage(this,thumbNail)
                    uploadImageToStorage()
                    binding?.ivShowVisitImage?.setImageBitmap(thumbNail)
                }
            }
        }
    }

   private fun takePhotoFromGallery(){
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
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

    private fun saveImageToStorage(context: Context, image: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, image, "${UUID.randomUUID()}", null)
        return Uri.parse(path)
    }

    private fun uploadImageToStorage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageUrl != null){
            val ref : StorageReference = FirebaseStorage.getInstance()
                .reference.child("Tree_Image" + System.currentTimeMillis())
            ref.putFile(mSelectedImageUrl!!).addOnSuccessListener {
                    snapshot ->
                Log.i("Firebase", snapshot.metadata!!.reference!!.downloadUrl.toString())
                snapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    Log.i("Downloadable Image", uri.toString())
                    mTreeImageUrl = uri.toString()
                    hideProgressDialog()
//                    sendToFirebase()
                }
            }.addOnFailureListener{
                    e-> e.message?.let { showErrorSnackBar(it) }
                hideProgressDialog()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}