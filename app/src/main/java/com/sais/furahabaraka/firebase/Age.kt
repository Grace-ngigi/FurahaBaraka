package com.sais.furahabaraka.firebase

import android.os.Parcel
import android.os.Parcelable

data class Age(
	val years : Int = 0,
	val months : Int = 0,
	val days : Int = 0
	) : Parcelable {
	constructor(parcel: Parcel) : this(
		parcel.readInt(),
		parcel.readInt(),
		parcel.readInt()
	) {
	}

	override fun describeContents(): Int {
		TODO("Not yet implemented")
	}

	override fun writeToParcel(parcel: Parcel?, p1: Int) {
		parcel?.writeInt(years)
		parcel?.writeInt(months)
		parcel?.writeInt(days)
	}

	companion object CREATOR : Parcelable.Creator<Age> {
		override fun createFromParcel(parcel: Parcel): Age {
			return Age(parcel)
		}

		override fun newArray(size: Int): Array<Age?> {
			return arrayOfNulls(size)
		}
	}
}
