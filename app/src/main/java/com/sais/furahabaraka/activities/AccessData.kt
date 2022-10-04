package com.sais.furahabaraka.activities

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sais.furahabaraka.R
import com.sais.furahabaraka.adapters.FieldsAdapter
import com.sais.furahabaraka.databinding.ActivityAccessDataBinding
import com.sais.furahabaraka.firebase.Fields
import com.sais.furahabaraka.firebase.FireStore
import com.sais.furahabaraka.utils.Constants
import com.sais.furahabaraka.utils.SwipeToEdit


class AccessData : BaseActivity() {
companion object{
    const val EXTRA_BRANCH_DETAILS = 1
}
    var binding : ActivityAccessDataBinding? = null
    lateinit var fieldsAdapter: FieldsAdapter
    private var farmIdList = ArrayList<Fields>()
    private var queryString: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAccessDataBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setSupportActionBar(binding?.tbAccessData)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Registered Fields"
        binding?.tbAccessData?.setNavigationOnClickListener { onBackPressed() }

//        val adapter = MainAdapter(TaskList.taskList)
//        binding?.recyclerView?.layoutManager = LinearLayoutManager(this)
//        binding?.recyclerView?.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.fabAdd)
        fab.setOnClickListener {
            val intent = Intent(this, FieldRegistration::class.java)
            startActivityForResult(intent, Constants.CREATE_FIELD_REQUEST_CODE )
        }
            FireStore().getFieldsList(this)

    }

    override fun onResume() {
        super.onResume()
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStore().getFieldsList(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK &&
        requestCode == Constants.CREATE_FIELD_REQUEST_CODE){
            FireStore().getFieldsList(this)
        }
    }
    fun getAllFields(fields: ArrayList<Fields>) {
        if (fields.size > 0) {
            fieldsAdapter = FieldsAdapter(this, fields)
            binding?.recyclerView?.layoutManager = LinearLayoutManager(this)
            binding?.recyclerView?.setHasFixedSize(true)
            binding?.recyclerView?.adapter = fieldsAdapter
            binding?.recyclerView?.visibility = View.VISIBLE
            binding?.tvNoRecords?.visibility = View.GONE

            fieldsAdapter.setOnClickListener(object : FieldsAdapter.OnClickListener{
                override fun onClick(position: Int, item: Fields) {
                    val intent = Intent(this@AccessData, FieldDetailActivity::class.java)
                    intent.putExtra(Constants.EXTRA_FIELD_DETAILS, item)
                    startActivity(intent)
                }

            })
            val editSwipeHandler = object : SwipeToEdit(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = binding?.recyclerView?.adapter as FieldsAdapter
                    adapter.notifyEditItem(
                        this@AccessData,
                        viewHolder.adapterPosition,
                        Constants.ADD_FIELD_ACTIVITY_REQUEST
                    )
                }
            }

            val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
            editItemTouchHelper.attachToRecyclerView(binding?.recyclerView)
        } else{
            binding?.recyclerView?.visibility = View.GONE
            binding?.tvNoRecords?.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.app_bar_search)
            .actionView as SearchView
        searchView.setSearchableInfo(
        searchManager.getSearchableInfo(componentName)
        )
        searchView.maxWidth = Int.MAX_VALUE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
	override fun onQueryTextSubmit(query: String?): Boolean {
        queryString= query.toString()
        showProgressDialog(resources.getString(R.string.please_wait))
        FireStore().filterFields(this@AccessData, queryString!!)
        Log.i("query", queryString.toString())
		return false
	}

	override fun onQueryTextChange(query: String?): Boolean {

		return false
	}
        })
            return true
    }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
//            when (item.itemId) {
//                R.id.app_bar_search -> {
//                }
//            }
//            return super.onOptionsItemSelected(item)
//        }
            val id: Int = item.itemId
            return if (id == R.id.app_bar_search) {
                true
            } else super.onOptionsItemSelected(item)
        }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}