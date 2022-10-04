package com.sais.furahabaraka.firebase

import android.os.Parcel
import android.os.Parcelable

data class Fields(
	val branchName: String = "",
	val farmId: String = "",
	val fieldName: String ="",
	val size: Int = 0,
	val exotic: Int = 0,
	val indigenous: Int = 0,
	val fruits: Int = 0,
	val description: String = "",
	val county: String = "",
	val subCounty: String = "",
	val image : String = "",
	val latitude: Double = 0.0,
	val longitude : Double = 0.0,
	val owner: String = "",
	val date : String = "",
	var fieldId: String = ""


): Parcelable {
	constructor(parcel: Parcel) : this(
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readInt(),
		parcel.readInt(),
		parcel.readInt(),
		parcel.readInt(),
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readDouble(),
		parcel.readDouble(),
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
	) {
	}

	override fun describeContents(): Int {
		TODO("Not yet implemented")
	}

	override fun writeToParcel(parcel: Parcel?, p1: Int) {
		parcel?.writeString(branchName)
		parcel?.writeString(farmId)
		parcel?.writeString(fieldName)
		parcel?.writeInt(size)
		parcel?.writeInt(fruits)
		parcel?.writeInt(exotic)
		parcel?.writeInt(indigenous)
		parcel?.writeString(description)
		parcel?.writeString(county)
		parcel?.writeString(subCounty)
		parcel?.writeString(image)
		parcel?.writeDouble(latitude)
		parcel?.writeDouble(longitude)
		parcel?.writeString(owner)
		parcel?.writeString(date)
		parcel?.writeString(fieldId)
	}

	companion object CREATOR : Parcelable.Creator<Fields> {
		override fun createFromParcel(parcel: Parcel): Fields {
			return Fields(parcel)
		}

		override fun newArray(size: Int): Array<Fields?> {
			return arrayOfNulls(size)
		}
	}
}
