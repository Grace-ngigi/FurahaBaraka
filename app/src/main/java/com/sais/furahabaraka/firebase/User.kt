package com.sais.furahabaraka.firebase

import android.os.Parcel
import android.os.Parcelable

data class User(
val id:String ="",
val name: String ="",
val email: String ="",
val phone:Long = 0,
): Parcelable {
	constructor(parcel: Parcel) : this(
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readString()!!,
		parcel.readLong(),
	)

	override fun describeContents(): Int {
		TODO("Not yet implemented")
	}

	override fun writeToParcel(p0: Parcel, flags: Int) {
		p0.writeString(id)
		p0.writeString(name)
		p0.writeString(email)
		p0.writeLong(phone)
	}

	companion object CREATOR : Parcelable.Creator<User> {
		override fun createFromParcel(parcel: Parcel): User {
			return User(parcel)
		}

		override fun newArray(size: Int): Array<User?> {
			return arrayOfNulls(size)
		}
	}
}

