package com.sais.furahabaraka.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sais.furahabaraka.R
import com.sais.furahabaraka.activities.FieldRegistration
import com.sais.furahabaraka.databinding.RecyclerviewFieldItemBinding
import com.sais.furahabaraka.firebase.Fields
import com.sais.furahabaraka.utils.Constants


class FieldsAdapter(private val context: Context,
                    private val fields: ArrayList<Fields>
):RecyclerView.Adapter<FieldsAdapter.ViewHolder>() {

	class  ViewHolder(binding: RecyclerviewFieldItemBinding) : RecyclerView.ViewHolder(binding.root){
		val llMain = binding.llFieldMain
		val tvFarmName = binding.tvFarmName
		val branch = binding.tvBranchName
	}

	private var fieldsList = ArrayList<Fields>()

	private  var onClickListener: OnClickListener? = null
	interface OnClickListener {
		fun onClick(position: Int, item: Fields)
	}

	fun setOnClickListener(onClickListener: OnClickListener){
		this.onClickListener = onClickListener
	}


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
return  ViewHolder(RecyclerviewFieldItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val item = fields[position]
		holder.tvFarmName.text = item.fieldName
		holder.branch.text = item.branchName

		if (position % 2 ==0){
			holder.llMain.setBackgroundColor(ContextCompat.getColor(holder.itemView.context,
				R.color.lightGrey
			))
		}else{
			holder.llMain.setBackgroundColor(ContextCompat.getColor(holder.itemView.context,
				R.color.white
			))
		}
			holder.itemView.setOnClickListener {
			if (onClickListener != null){
			onClickListener!!.onClick(position, item)
			}
		}
	}

	override fun getItemCount(): Int {
return fields.size	}

	fun notifyEditItem(activity: Activity, position: Int, requestCode: Int){
		val intent = Intent(context, FieldRegistration::class.java)
		intent.putExtra(Constants.EXTRA_FIELD_DETAILS, fields[position])
		activity.startActivityForResult(intent, requestCode)
		notifyItemChanged(position)
	}

	fun filterList(farmIdList: ArrayList<Fields>){
		this.fieldsList = farmIdList
		notifyDataSetChanged()
	}
}