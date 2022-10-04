package com.sais.furahabaraka.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sais.furahabaraka.activities.TreeRegistration
import com.sais.furahabaraka.databinding.RecyclerviewTreeItemBinding
import com.sais.furahabaraka.firebase.Trees
import com.sais.furahabaraka.utils.Constants

class TreesAdapter(private  val context: Context, private val trees: ArrayList<Trees>,

                   ):RecyclerView.Adapter<TreesAdapter.ViewHolder>() {

	class  ViewHolder(binding: RecyclerviewTreeItemBinding) : RecyclerView.ViewHolder(binding.root){
		val treeName = binding.tvVisitTree
		val category = binding.tvTreeCategory

	}
	private  var onClickListener: OnClickListener? = null
	interface OnClickListener {
		fun onClick(position: Int, item: Trees)
	}

	fun setOnClickListener(onClickListener: OnClickListener){
		this.onClickListener = onClickListener
	}


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
return  ViewHolder(RecyclerviewTreeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val item = trees[position]
		holder.treeName.text = item.treeName
		holder.category.text = item.treeType

holder.itemView.setOnClickListener {
	if (onClickListener != null){
		onClickListener!!.onClick(position, item)
	}
}

	}

	override fun getItemCount(): Int {
return trees.size
	}
	fun notifyEditItem(activity: Activity, position: Int, requestCode: Int){
		val intent = Intent(context, TreeRegistration::class.java)
		intent.putExtra(Constants.EXTRA_TREE_DETAILS, trees[position])
		activity.startActivityForResult(intent, requestCode)
		notifyItemChanged(position)
	}
}