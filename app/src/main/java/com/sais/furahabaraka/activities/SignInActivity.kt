package com.sais.furahabaraka.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.sais.furahabaraka.R
import com.sais.furahabaraka.databinding.ActivitySignInBinding
import com.sais.furahabaraka.firebase.FireStore
import com.sais.furahabaraka.firebase.User
import com.sais.furahabaraka.utils.Constants


class SignInActivity : BaseActivity() {

	private  var binding: ActivitySignInBinding? = null

	private lateinit var auth: FirebaseAuth
	private var user: User? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivitySignInBinding.inflate(layoutInflater)
		setContentView(binding?.root)

		if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
			user = intent.getParcelableExtra<User>(Constants.EXTRA_USER_DETAILS)!! as User
		}
		auth = FirebaseAuth.getInstance()
		window.setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN
		)
		setUpActionBar()

		binding?.btnSignIn?.setOnClickListener {
				signInUser()

			}
		binding?.tvForgotPassword?.setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }
	}

	private fun setUpActionBar(){
		setSupportActionBar(binding?.toolbarSignInActivity)
		val actionBar = supportActionBar
		if (actionBar != null){
			actionBar.setDisplayHomeAsUpEnabled(true)
			actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
			actionBar.title = resources.getString(R.string.sign_in)
		}
		binding?.toolbarSignInActivity?.setNavigationOnClickListener { onBackPressed() }
	}

fun emailVerifiedSuccess(){
	hideProgressDialog()
	startActivity(Intent(this, HomePageActivity::class.java))
	finish()
	Toast.makeText(this,"Signed In Successfully", Toast.LENGTH_SHORT).show()
}
	fun emailVerifiedFailure(){
		FirebaseAuth.getInstance().signOut();
		startActivity(Intent(this, SignInActivity::class.java))
		Toast.makeText(this,"Email not Verified. Please Verify from the Email Sent.", Toast.LENGTH_LONG).show()
	}

	private fun signInUser(){
		val email: String = binding?.etEmail?.text.toString().trim{it <= ' '}
		val password: String = binding?.etPassword?.text.toString()

		if (validateForm(email,password)){
			showProgressDialog(resources.getString(R.string.please_wait))
			auth.signInWithEmailAndPassword(email,password)
				.addOnCompleteListener { task ->
					hideProgressDialog()
					if (task.isSuccessful) {
						FireStore().checkEmailVerified(this)
					} else {
						showErrorSnackBar(task.exception.toString())
					}
				}
				.addOnFailureListener {
					hideProgressDialog()
					showErrorSnackBar(it.message.toString())
				}
		}

	}

	private fun validateForm(email:String,password: String): Boolean{
		return when {
			TextUtils.isEmpty(email) ->{
				showErrorSnackBar("Please Enter your Email Address")
				false
			}
			TextUtils.isEmpty(password) ->{
				showErrorSnackBar("Please Enter your Password")
				false
			}
			else -> { true }
		}
	}

}