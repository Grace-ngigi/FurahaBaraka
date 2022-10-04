package com.sais.furahabaraka.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.sais.furahabaraka.R
import com.sais.furahabaraka.databinding.ActivityVisitDetailBinding
import com.sais.furahabaraka.firebase.Visit
import com.sais.furahabaraka.utils.Constants

class VisitDetailActivity : AppCompatActivity() {

	lateinit var binding: ActivityVisitDetailBinding
	lateinit var visit: Visit
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityVisitDetailBinding.inflate(layoutInflater)
		setContentView(binding.root)

		if (intent.hasExtra(Constants.EXTRA_VISIT_DETAILS)) {
			visit = intent.getParcelableExtra<Visit>(Constants.EXTRA_VISIT_DETAILS)!! as Visit
		}
		Glide
			.with(this)
			.load(visit.image)
			.centerCrop()
			.placeholder(R.drawable.ic_person_add_24)
			.into(binding.ivVisitImage)
		binding.tvVisitDate.text = visit.date
		binding.tvVisitStatus.text = visit.status
		binding.tvVisitDescription.text = visit.description

	}
}