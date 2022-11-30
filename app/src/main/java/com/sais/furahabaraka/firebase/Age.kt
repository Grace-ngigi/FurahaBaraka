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

	override fun writeToParcel(p0: Parcel, p1: Int) {
		p0.writeInt(years)
		p0.writeInt(months)
		p0.writeInt(days)
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
