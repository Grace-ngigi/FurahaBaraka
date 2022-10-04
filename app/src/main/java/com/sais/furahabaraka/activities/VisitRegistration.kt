package com.sais.furahabaraka.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.sais.furahabaraka.R
import com.sais.furahabaraka.databinding.ActivityVisitRegistrationBinding
import com.sais.furahabaraka.firebase.Age
import com.sais.furahabaraka.firebase.FireStore
import com.sais.furahabaraka.firebase.Trees
import com.sais.furahabaraka.firebase.Visit
import com.sais.furahabaraka.utils.Constants
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class VisitRegistration : BaseActivity() {
	lateinit var binding : ActivityVisitRegistrationBinding
	private var currentDate : LocalDate? = null
	private var cal = Calendar.getInstance()
	private var date :String? = null
	private var mVisitImageUrl: String = ""
	private var mSelectedImageUrl : Uri? = null
	private var trees : Trees? = null
	private var visit : Visit? = null
	private  var status: String? = null
	private var age: ArrayList<Age> = ArrayList()

	private lateinit var statusSpinner: Spinner
	private val mFireStore = FirebaseFirestore.getInstance()

	override fun onCreate(savedInstanceState: Bundle?) {
		binding = ActivityVisitRegistrationBinding.inflate(layoutInflater)
		super.onCreate(savedInstanceState)
		setContentView(binding.root)
		statusSpinner = binding.spTreeStatus

		setSupportActionBar(binding.tbAddVisits)
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		supportActionBar?.title = "Visit Registration"
		binding.tbAddVisits.setNavigationOnClickListener {
			onBackPressed()
		}

			currentDate = LocalDate.now()
			binding.etDate.setText(currentDate.toString())


		if (intent.hasExtra(Constants.EXTRA_TREE_DETAILS)) {
			trees = intent.getParcelableExtra(Constants.EXTRA_TREE_DETAILS)!!
		}

		if (intent.hasExtra(Constants.EXTRA_VISIT_DETAILS)) {
			visit = intent.getParcelableExtra(Constants.EXTRA_VISIT_DETAILS)!!
		}

		if (visit != null) {
			status = visit!!.status
			Log.i("status", status.toString())
			loadStatusData()
			Glide
				.with(this)
				.load(visit!!.image)
				.centerCrop()
				.placeholder(R.drawable.ic_person_add_24)
				.into(binding.ivShowVisitImage)
			mVisitImageUrl = visit!!.image
			mSelectedImageUrl = Uri.parse(visit!!.image)
			binding.etTreeDescription.setText(visit!!.description)
			binding.btVisitSubmit.visibility = View.GONE
			binding.btUpdate.visibility = View.VISIBLE
			for (things in visit!!.age) {
				binding.tvAgeYears.text = things.years.toString()
				binding.tvAgeMonths.text = things.months.toString()
				binding.tvAgeDays.text = things.days.toString()
			}
		}else{
			statusText()
			getAgeInDays()
		}

		binding.btVisitSubmit.setOnClickListener {
			if (checkInternetConnectivity()) {
				saveVisit()
			}
		}
		binding.btUpdate.setOnClickListener {
			if (checkInternetConnectivity()) {
				updateVisitHashMap()
			}
		}
		binding.ibTakeVisitPhoto.setOnClickListener { takePhotoFromGallery() }
	}

	private fun loadStatusData() {
		val currentStatus = visit!!.status
		val treeStatus = Constants.status
		val statusAdapter = ArrayAdapter(
				this,
				android.R.layout.simple_spinner_dropdown_item,
				treeStatus
			)
			statusSpinner.adapter = statusAdapter
			statusSpinner.setSelection(statusAdapter.getPosition(currentStatus))
		statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(
				parent: AdapterView<*>?,
				view: View?,
				position: Int,
				id: Long
			) {
				status = treeStatus[position]
			}

			override fun onNothingSelected(p0: AdapterView<*>?) {
				TODO("Not yet implemented")
			}
		}
	}

	private fun statusText(){
		val treeStatus = Constants.status
		val statusAdapter = ArrayAdapter(this,
			android.R.layout.simple_spinner_dropdown_item,
			treeStatus)
		statusSpinner.adapter = statusAdapter
			statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
				override fun onItemSelected(
					parent: AdapterView<*>?,
					view: View?,
					position: Int,
					id: Long
				) {
					status = treeStatus[position]
				}

				override fun onNothingSelected(p0: AdapterView<*>?) {
					TODO("Not yet implemented")
				}
		}
	}

	private fun saveVisit() {
		val id  = mFireStore.collection(Constants.VISITS).document().id
		val description = binding.etTreeDescription.text.toString()
		when {
			status!!.isEmpty()->{
				showErrorSnackBar("Please Select Status")
			}
			description.isEmpty() -> {
				showErrorSnackBar("Please Enter Description")
			}
			mSelectedImageUrl == null -> {
				showErrorSnackBar("Please Take a Photo")
			}
			else -> {
				showProgressDialog(resources.getString(R.string.please_wait))
				val visit = Visit(
					status!!, currentDate.toString(),
					description, mVisitImageUrl,
					trees!!.treeId, age,
					id
				)
				FireStore().saveVisitToFirebase(this@VisitRegistration, visit)
			}
		}
	}

	private fun getAgeInDays() : ArrayList<Age> {
		val birthDate = LocalDate.parse(trees!!.timeStamp, DateTimeFormatter.ISO_DATE)
		val period = Period.between(birthDate, currentDate)
		 val calAge =  Age(period.years, period.months,
			period.days)
		age.add(0, calAge)

		binding.tvAgeYears.text =  period.years.toString()
		binding.tvAgeMonths.text =  period.months.toString()
		binding.tvAgeDays.text =  period.days.toString()

		return age
	}

	fun treeCreatedSuccess(){
		setResult(Activity.RESULT_OK)
		hideProgressDialog()
		finish()
	}

	private fun updateVisitHashMap(){
		val visitHashMap = HashMap<String, Any>()
		var anyChangesMade = false
		if (mVisitImageUrl != visit!!.image){
			visitHashMap["image"] = mVisitImageUrl
			anyChangesMade = true
		}
		if (status != visit!!.status){
			visitHashMap["status"] = status!!
			anyChangesMade = true
		}
		if (binding.etTreeDescription.text.toString() != visit!!.description){
			visitHashMap["description"] = binding.etTreeDescription.text.toString()
			anyChangesMade = true
		}
		if (anyChangesMade) {
			showProgressDialog(resources.getString(R.string.please_wait))
			FireStore().updateVisit(this, visitHashMap, visit!!)
		} else {
			Toast.makeText(this, "No changes Made", Toast.LENGTH_SHORT).show()
			finish()
		}
	}

	fun updateVisitSuccess(){
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
					binding.ivShowVisitImage.setImageBitmap(thumbNail)
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
				.reference.child("Visit_Image" + System.currentTimeMillis())
			ref.putFile(mSelectedImageUrl!!).addOnSuccessListener {
					snapshot ->
				Log.i("Firebase", snapshot.metadata!!.reference!!.downloadUrl.toString())
				snapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
						uri ->
					Log.i("Downloadable Image", uri.toString())
					mVisitImageUrl = uri.toString()
					hideProgressDialog()
//                    sendToFirebase()
				}
			}.addOnFailureListener{
					e-> e.message?.let { showErrorSnackBar(it) }

				hideProgressDialog()
			}
		}
	}
}