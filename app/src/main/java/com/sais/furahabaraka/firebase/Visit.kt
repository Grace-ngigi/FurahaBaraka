package com.sais.furahabaraka.firebase

import android.os.Parcel
import android.os.Parcelable

data class Visit(
	var status: String = "",
	var date: String = "",
	var description: String ="",
	var image: String = "",
	var treeId: String = "",
	var age: ArrayList<Age> = ArrayList(),
	var visitId: String = "",
	): Parcelable {
	constructor(parcel: Parcel) : this(
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.createTypedArrayList(Age.CREATOR)!!,
		parcel.readString()!!,
	) {
	}

	override fun describeContents(): Int {
		TODO("Not yet implemented")
	}

	override fun writeToParcel(p0: Parcel, p1: Int) {
		p0.writeString(status)
		p0.writeString(date)
		p0.writeString(description)
		p0.writeString(image)
		p0.writeString(treeId)
		p0.writeTypedList(age)
		p0.writeString(visitId)

	}

	companion object CREATOR : Parcelable.Creator<Visit> {
		override fun createFromParcel(parcel: Parcel): Visit {
			return Visit(parcel)
		}

		override fun newArray(size: Int): Array<Visit?> {
			return arrayOfNulls(size)
		}
	}
}
