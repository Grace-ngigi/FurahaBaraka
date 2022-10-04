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

	override fun writeToParcel(parcel: Parcel?, p1: Int) {
		parcel?.writeString(status)
		parcel?.writeString(date)
		parcel?.writeString(description)
		parcel?.writeString(image)
		parcel?.writeString(treeId)
		parcel?.writeTypedList(age)
		parcel?.writeString(visitId)

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
