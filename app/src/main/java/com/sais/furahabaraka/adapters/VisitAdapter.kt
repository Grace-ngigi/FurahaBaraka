package com.sais.furahabaraka.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sais.furahabaraka.activities.VisitRegistration
import com.sais.furahabaraka.databinding.RecyclerviewVisitBinding
import com.sais.furahabaraka.firebase.Visit
import com.sais.furahabaraka.utils.Constants

class VisitAdapter(private  val context: Context, private val visits: ArrayList<Visit>,

                   ):RecyclerView.Adapter<VisitAdapter.ViewHolder>() {

	class  ViewHolder(binding: RecyclerviewVisitBinding) : RecyclerView.ViewHolder(binding.root){
		val treeStatus = binding.tvVisitStatus
		val date = binding.tvVisitDate

	}
	private  var onClickListener: OnClickListener? = null
	interface OnClickListener {
		fun onClick(position: Int, item: Visit)
	}

	fun setOnClickListener(onClickListener: OnClickListener){
		this.onClickListener = onClickListener
	}


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
return  ViewHolder(RecyclerviewVisitBinding.inflate(LayoutInflater.from(parent.context), parent, false))	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val item = visits[position]
		holder.treeStatus.text = item.status
		holder.date.text = item.date

holder.itemView.setOnClickListener {
	if (onClickListener != null){
		onClickListener!!.onClick(position, item)
	}
}

	}

	override fun getItemCount(): Int {
return visits.size
	}
	fun notifyEditItem(activity: Activity, position: Int, requestCode: Int){
		val intent = Intent(context, VisitRegistration::class.java)
		intent.putExtra(Constants.EXTRA_VISIT_DETAILS, visits[position])
		activity.startActivityForResult(intent, requestCode)
		notifyItemChanged(position)
	}
}