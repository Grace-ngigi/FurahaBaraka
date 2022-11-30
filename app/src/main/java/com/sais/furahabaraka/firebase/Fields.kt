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

	override fun writeToParcel(p0: Parcel, p1: Int) {
		p0.writeString(branchName)
		p0.writeString(farmId)
		p0.writeString(fieldName)
		p0.writeInt(size)
		p0.writeInt(fruits)
		p0.writeInt(exotic)
		p0.writeInt(indigenous)
		p0.writeString(description)
		p0.writeString(county)
		p0.writeString(subCounty)
		p0.writeString(image)
		p0.writeDouble(latitude)
		p0.writeDouble(longitude)
		p0.writeString(owner)
		p0.writeString(date)
		p0.writeString(fieldId)
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
