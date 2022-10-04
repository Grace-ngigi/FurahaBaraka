package com.sais.furahabaraka.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sais.furahabaraka.R
import com.sais.furahabaraka.databinding.ActivitySignUpBinding
import com.sais.furahabaraka.firebase.FireStore
import com.sais.furahabaraka.firebase.User
import com.sais.furahabaraka.utils.Constants

class SignUpActivity : BaseActivity() {
	private var binding: ActivitySignUpBinding? = null
	val auth = FirebaseAuth.getInstance()
	private var user: User? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivitySignUpBinding.inflate(layoutInflater)
		setContentView(binding?.root)

		window.setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN
		)
		binding?.btnSignUp?.setOnClickListener { registerUser() }

		binding?.btnSignIn?.setOnClickListener { startActivity(Intent(this, SignInActivity::class.java)) }
	}

	fun sendEmailVerifySuccess(){
		hideProgressDialog()
		auth.signOut()
		val intent = Intent(this, SignInActivity::class.java)
		intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
		startActivity(intent)
		finish()
	}

	private fun registerUser(){
		val name : String = binding?.etName?.text.toString().trim{ it <= ' '}
		val email : String = binding?.etEmail?.text.toString().trim{ it<= ' '}
		val password : String = binding?.etPassword?.text.toString()

		if (validateForm(name, email,password)){
			showProgressDialog(resources.getString(R.string.please_wait))
			auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
				if (task.isSuccessful) {
					val firebaseUser: FirebaseUser = task.result!!.user!!
					val registeredEmail = firebaseUser.email!!
					user = User(firebaseUser.uid, name, registeredEmail)
					FireStore().saveUserToFirestore(this, user!!)
					FireStore().verifyEmail(this)
				} else{
					hideProgressDialog()
					task.exception!!.message?.let { showErrorSnackBar(it) }
				}

			}
		}

	}
	private fun validateForm(name: String,email:String,password: String): Boolean{
		return when {
			TextUtils.isEmpty(name) ->{
				showErrorSnackBar("Please Enter a Name")
				false
			}
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