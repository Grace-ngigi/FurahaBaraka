package com.sais.furahabaraka.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.sais.furahabaraka.databinding.ActivitySplashScreenBinding
import com.sais.furahabaraka.firebase.FireStore


class SplashScreenActivity : AppCompatActivity() {
	lateinit var binding: ActivitySplashScreenBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySplashScreenBinding.inflate(layoutInflater)
		setContentView(binding.root)

		window.setFlags(
			WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN
		)
		Handler().postDelayed({
			val currentUserId = FireStore().getCurrentUserId()
			if (currentUserId.isNotEmpty()){
				startActivity(Intent(this, HomePageActivity::class.java))
			} else {
				startActivity(Intent(this, SignUpActivity::class.java))
			}
			finish()
		}, 2500)
	}
}