package com.sais.furahabaraka.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sais.furahabaraka.R
import com.sais.furahabaraka.adapters.VisitAdapter
import com.sais.furahabaraka.databinding.ActivityTreeDetailBinding
import com.sais.furahabaraka.firebase.FireStore
import com.sais.furahabaraka.firebase.Trees
import com.sais.furahabaraka.firebase.Visit
import com.sais.furahabaraka.utils.Constants
import com.sais.furahabaraka.utils.SwipeToEdit

class TreeDetailActivity : BaseActivity() {
	lateinit var trees: Trees
	lateinit var binding: ActivityTreeDetailBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityTreeDetailBinding.inflate(layoutInflater)
		setContentView(binding.root)

		if (intent.hasExtra(Constants.EXTRA_TREE_DETAILS)) {
			trees = intent.getParcelableExtra<Trees>(Constants.EXTRA_TREE_DETAILS)!! as Trees
		}

		Glide
			.with(this)
			.load(trees.image)
			.centerCrop()
			.placeholder(R.drawable.add_screen_image_placeholder)
			.into(binding.ivTreeImage)

		setSupportActionBar(binding.tbTreeDetail)
		supportActionBar!!.setDisplayHomeAsUpEnabled(true)
		supportActionBar!!.title = trees.treeName
		binding.tbTreeDetail.setNavigationOnClickListener { onBackPressed() }

		binding.fabAddVisit.setOnClickListener {
			val intent = Intent(this@TreeDetailActivity, VisitRegistration::class.java)
			intent.putExtra(Constants.EXTRA_TREE_DETAILS, trees)
			startActivityForResult(intent, Constants.CREATE_VISIT_REQUEST_CODE)
		}
		FireStore().getVisitsList(this, trees)

	}

	override fun onResume() {
		super.onResume()
		showProgressDialog(resources.getString(R.string.please_wait))
		FireStore().getVisitsList(this, trees)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == Activity.RESULT_OK &&
			requestCode == Constants.CREATE_VISIT_REQUEST_CODE){
			FireStore().getVisitsList(this, trees)
		}
	}

	fun getAllVisits(visitInfo: ArrayList<Visit>) {
		if (visitInfo.isNotEmpty()) {
			binding.rvVisits.layoutManager = LinearLayoutManager(this)
			binding.rvVisits.setHasFixedSize(true)
			binding.rvVisits.visibility = View.VISIBLE
			binding.tvNoVisits.visibility = View.GONE
			val visitAdapter = VisitAdapter(this, visitInfo)
			binding.rvVisits.adapter = visitAdapter

			visitAdapter.setOnClickListener(object : VisitAdapter.OnClickListener {
				override fun onClick(povsition: Int, item: Visit) {
					val intent = Intent(this@TreeDetailActivity, VisitDetailActivity::class.java)
					intent.putExtra(Constants.EXTRA_VISIT_DETAILS, item)
					startActivity(intent)

				}
			})

			val editSwipeHandler = object : SwipeToEdit(this) {
				override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
					val adapter = binding.rvVisits.adapter as VisitAdapter
					adapter.notifyEditItem(this@TreeDetailActivity, viewHolder.adapterPosition,
						Constants.ADD_VISIT_ACTIVITY_REQUEST)

				}
			}

			val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
			editItemTouchHelper.attachToRecyclerView(binding.rvVisits)

		} else {

			binding.rvVisits.visibility = View.GONE
			binding.tvNoVisits.visibility = View.VISIBLE
		}
	}
}