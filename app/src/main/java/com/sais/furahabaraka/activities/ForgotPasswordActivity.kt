package com.sais.furahabaraka.activities

import android.content.Intent
import android.os.Bundle
import com.sais.furahabaraka.R
import com.sais.furahabaraka.databinding.ActivityForgotPasswordBinding
import com.sais.furahabaraka.firebase.FireStore
import com.sais.furahabaraka.firebase.User

class ForgotPasswordActivity : BaseActivity() {
	lateinit var binding: ActivityForgotPasswordBinding
	lateinit var user: User
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
		setContentView(binding.root)
		setUpActionBar()
		binding.btnSendEmail.setOnClickListener { sendEmail() }
	}

	private fun setUpActionBar(){
		setSupportActionBar(binding.tbForgotPassword)
		val actionBar = supportActionBar
		if (actionBar != null){
			actionBar.setDisplayHomeAsUpEnabled(true)
			actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
			actionBar.title = resources.getString(R.string.forgot_password)
		}
		binding.tbForgotPassword.setNavigationOnClickListener { onBackPressed() }
	}

	private fun sendEmail(){
		val email = binding.etEmail.text.toString()
		if (email.isNotEmpty()){
			showProgressDialog(resources.getString(R.string.please_wait))
			FireStore().sendEmail(this, email)
		} else{
			showErrorSnackBar("Please enter email")

		}
	}

	 fun sendEmailSuccess() {
		 hideProgressDialog()
		val intent = Intent(this@ForgotPasswordActivity, SignInActivity::class.java)
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
		startActivity(intent)

	}
}


