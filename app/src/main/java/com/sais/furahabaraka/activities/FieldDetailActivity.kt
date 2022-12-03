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
import com.sais.furahabaraka.adapters.TreesAdapter
import com.sais.furahabaraka.databinding.ActivityFieldDetailBinding
import com.sais.furahabaraka.firebase.Age
import com.sais.furahabaraka.firebase.Fields
import com.sais.furahabaraka.firebase.FireStore
import com.sais.furahabaraka.firebase.Trees
import com.sais.furahabaraka.utils.Constants
import com.sais.furahabaraka.utils.SwipeToEdit
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class FieldDetailActivity : BaseActivity() {
	private  var binding: ActivityFieldDetailBinding? = null
	lateinit var fields: Fields

	override fun onCreate(savedInstanceState: Bundle?) {
		binding = ActivityFieldDetailBinding.inflate(layoutInflater)
		super.onCreate(savedInstanceState)
		setContentView(binding?.root)

		if (intent.hasExtra(Constants.EXTRA_FIELD_DETAILS)) {
			fields = intent.getParcelableExtra<Fields>(Constants.EXTRA_FIELD_DETAILS)!! as Fields
		}
			setSupportActionBar(binding?.tbFieldDetail)
			supportActionBar!!.setDisplayHomeAsUpEnabled(true)
			supportActionBar!!.title = "Field Details"
			binding?.tbFieldDetail?.setNavigationOnClickListener { onBackPressed() }
//			binding?.ivFieldImage?.setImageURI(Uri.parse(fields.image))
			Glide
			.with(this)
			.load(fields.image)
			.centerCrop()
			.placeholder(R.drawable.ic_person_add_24)
			.into(binding!!.ivFieldImage)
			val total = fields.fruits + fields.indigenous + fields.exotic
			binding?.tvTotal?.text = buildString { append(total.toString())
				 append(" Trees")}
			binding?.tvFarmSize?.text = buildString { append(fields.size.toString())
				 append(" Acres")}
			binding?.tvFieldName?.text = fields.fieldName
		getCarbonAbsorbed(total)


		binding?.fabAddVisit?.setOnClickListener {
			val intent = Intent(this@FieldDetailActivity, TreeRegistration::class.java)
			intent.putExtra(Constants.EXTRA_FIELD_DETAILS, fields)
			startActivityForResult(intent, Constants.CREATE_TREE_REQUEST_CODE)
		}
		FireStore().getTreesList(this, fields )
	}

	private fun getCarbonAbsorbed(total: Int) {
		val currentDate = LocalDate.now()
		val birthDate = LocalDate.parse(fields.date, DateTimeFormatter.ISO_DATE)
		val period = Period.between(birthDate, currentDate)
//		val years = if (period.years < 1) { 1 } else { period.years }
		binding?.tvCarbon?.text = buildString { append((total * 0.83 * period.months).toString())
			append(" KGs")}
	}

	override fun onResume() {
		super.onResume()
		showProgressDialog(resources.getString(R.string.please_wait))
		FireStore().getTreesList(this, fields )
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == Activity.RESULT_OK &&
			requestCode == Constants.CREATE_TREE_REQUEST_CODE){
			FireStore().getTreesList(this, fields)
		}
	}

		fun getAllTrees(treeInfo: ArrayList<Trees>) {
	if (treeInfo.size > 0) {
		binding?.rvVisits?.layoutManager = LinearLayoutManager(this)
		binding?.rvVisits?.setHasFixedSize(true)
		binding?.rvVisits?.scrollToPosition(treeInfo.size - 1)
		binding?.rvVisits?.visibility = View.VISIBLE
		binding?.tvNoVisits?.visibility = View.GONE
		val treesAdapter = TreesAdapter(this, treeInfo)
		binding?.rvVisits?.adapter = treesAdapter

		treesAdapter.setOnClickListener(object : TreesAdapter.OnClickListener {
					override fun onClick(position: Int, item: Trees) {
						val intent = Intent(this@FieldDetailActivity, TreeDetailActivity::class.java)
						intent.putExtra(Constants.EXTRA_TREE_DETAILS, item)
						startActivity(intent)
					}
				})

				val editSwipeHandler = object : SwipeToEdit(this) {
					override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
						val adapter = binding?.rvVisits?.adapter as TreesAdapter
						adapter.notifyEditItem(this@FieldDetailActivity, viewHolder.adapterPosition,
							Constants.ADD_TREE_ACTIVITY_REQUEST)

					}
				}

				val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
				editItemTouchHelper.attachToRecyclerView(binding?.rvVisits)

			} else {

				binding?.rvVisits?.visibility = View.GONE
				binding?.tvNoVisits?.visibility = View.VISIBLE
			}
		}

//	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//		super.onActivityResult(requestCode, resultCode, data)
//		if (requestCode == Constants.REQUEST_FOR_VISIT_UPDATE) {
//			if (resultCode == Activity.RESULT_OK) {
//				val visitDao = (application as TorTreeApp).db.visitInfoDao()
//				lifecycleScope.launch {
//					visitDao.getFieldsWithVisits(farmId = fieldInfo.farmId).collect {
//						val visitsList = ArrayList(it)
//						displayVisitInfoDataIntoRecycler(visitsList)
//						Log.i("visit", "$it")
//					}
//				}
//			}
//		}
//	}
	override fun onDestroy() {
		super.onDestroy()
		binding = null
	}
}