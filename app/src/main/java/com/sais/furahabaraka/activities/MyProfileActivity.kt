package com.sais.furahabaraka.activities

import android.app.Activity
import android.os.Bundle
import com.sais.furahabaraka.R
import com.sais.furahabaraka.databinding.ActivityMyProfileBinding
import com.sais.furahabaraka.firebase.FireStore
import com.sais.furahabaraka.firebase.User
import com.sais.furahabaraka.utils.Constants
import java.util.HashMap

class MyProfileActivity : BaseActivity() {
	lateinit var binding: ActivityMyProfileBinding
	private  lateinit var mUserDetails: User

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMyProfileBinding.inflate(layoutInflater)
		setContentView(binding.root)
		FireStore().loadUserData(this)
		setUpActionBar()

		binding.btnUpdate.setOnClickListener { updateUserProfile() }
	}

	private fun setUpActionBar(){
		setSupportActionBar(binding.tbMyProfile)
		val actionBar = supportActionBar
		if (actionBar != null){
			actionBar.setDisplayHomeAsUpEnabled(true)
			actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
			actionBar.title = resources.getString(R.string.my_profile_title)
		}
		binding.tbMyProfile.setNavigationOnClickListener { onBackPressed() }
	}

	private fun updateUserProfile(){
		val userHashMap = HashMap<String, Any>()
		var anyChangesMade = false
		if (binding.etName.text.toString() != mUserDetails.name){
			userHashMap[Constants.NAME] = binding.etName.text.toString()
			anyChangesMade = true
		}
		if (binding.etMobile.text.toString() != mUserDetails.phone.toString()){
			userHashMap[Constants.PHONE] = binding.etMobile.text.toString().toLong()
			anyChangesMade = true
		}
		if (anyChangesMade) {
			showProgressDialog(resources.getString(R.string.please_wait))
			FireStore().updateUserProfile(this, userHashMap)
		}
	}

	fun setUserDataInUi(user:User){
		mUserDetails = user
		binding.etName.setText( user.name)
		binding.etEmail.setText( user.email)
		if (user.phone != 0L ){
			binding.etMobile.setText(user.phone.toString())
		}
	}
	fun profileUpdateSuccess(){
		hideProgressDialog()
		setResult(Activity.RESULT_OK)
		finish()
	}
}