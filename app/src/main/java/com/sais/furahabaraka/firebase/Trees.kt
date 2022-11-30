package com.sais.furahabaraka.firebase

import android.os.Parcel
import android.os.Parcelable

data class Trees(
	var treeName: String ="",
	var farmId: String = "",
	var treeType: String ="",
	var timeStamp: String? = "",
	var description: String ="",
	var image: String = "",
	var latitude: Double = 0.0,
	var longitude: Double= 0.0,
	var treeId: String = "",
	): Parcelable {
	constructor(parcel: Parcel) : this(
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readDouble(),
		parcel.readDouble(),
		parcel.readString()!!,
//		parcel.createTypedArrayList(Task.CREATOR)!!
	) {
	}

	override fun describeContents(): Int {
		TODO("Not yet implemented")
	}

	override fun writeToParcel(p0: Parcel, p1: Int) {
		p0.writeString(treeName)
		p0.writeString(farmId)
		p0.writeString(treeType)
		p0.writeString(timeStamp)
		p0.writeString(description)
		p0.writeString(image)
		p0.writeDouble(latitude)
		p0.writeDouble(longitude)
		p0.writeString(treeId)
	}

	companion object CREATOR : Parcelable.Creator<Trees> {
		override fun createFromParcel(parcel: Parcel): Trees {
			return Trees(parcel)
		}

		override fun newArray(size: Int): Array<Trees?> {
			return arrayOfNulls(size)
		}
	}
}
