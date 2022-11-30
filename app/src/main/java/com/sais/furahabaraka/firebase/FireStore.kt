package com.sais.furahabaraka.firebase

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.sais.furahabaraka.activities.*
import com.sais.furahabaraka.utils.Constants


class FireStore {
	private val mFireStore = FirebaseFirestore.getInstance()
	private val mAuth = FirebaseAuth.getInstance()

	fun saveUserToFirestore(activity : SignUpActivity, userInfo:User){
		mFireStore.collection(Constants.USERS)
			.document(getCurrentUserId())
			.set(userInfo, SetOptions.merge())
			.addOnSuccessListener {
			}
			.addOnFailureListener { e->
				activity.hideProgressDialog()
				Log.e(activity.javaClass.simpleName, "Error writing document")
			}
	}

	 fun verifyEmail(activity: SignUpActivity) {
		val mUser = mAuth.currentUser
		mUser!!.sendEmailVerification()
			.addOnCompleteListener { task ->
				if (task.isSuccessful) {
					Toast.makeText(
						activity,
						"Verification email sent to " + mUser.email,
						Toast.LENGTH_LONG
					).show()
					activity.sendEmailVerifySuccess()
				} else {
					Log.e(activity.javaClass.simpleName, "sendEmailVerification", task.exception)
					activity.showErrorSnackBar("Failed to send verification email: ${task.exception}")
				}
			}
	}

	fun checkEmailVerified(activity: SignInActivity){
		val user = mAuth.currentUser
		if (user != null) {
			if (user.isEmailVerified) {
					activity.emailVerifiedSuccess()
			} else {
				// email is not verified, so just prompt the message to the user and restart this activity.
				// NOTE: don't forget to log out the user.
					activity.emailVerifiedFailure()
				}
				//restart this activity
		}
	}

	fun sendEmail(activity: ForgotPasswordActivity, email: String){
		mAuth.sendPasswordResetEmail(email)
				.addOnCompleteListener { task ->
					if (task.isSuccessful) {
						Toast.makeText(
							activity,
							"Password Reset email sent to: $email",
							Toast.LENGTH_LONG
						).show()
						activity.sendEmailSuccess()
					}
				}
				.addOnFailureListener {
					Log.d(activity.javaClass.simpleName,"failed to create User: ${it.message}")
					activity.hideProgressDialog()
					it.message?.let { it1 -> activity.showErrorSnackBar(it1) }
				}
		}

	fun getCurrentUserId(): String {
		val currentUser = FirebaseAuth.getInstance().currentUser
		var currentUserId = ""
		if (currentUser != null) {
			currentUserId = currentUser.uid
		}
		return currentUserId
	}

	fun updateUserProfile(activity: MyProfileActivity,
	                      userHashMap: HashMap<String, Any>){
		mFireStore.collection(Constants.USERS)
			.document(getCurrentUserId())
			.update(userHashMap)
			.addOnSuccessListener {
				Log.i(activity.javaClass.simpleName, "Update Success")
				Toast.makeText(activity, "Update Success", Toast.LENGTH_SHORT).show()
						activity.profileUpdateSuccess()
			}.addOnFailureListener {
					e->
						activity.hideProgressDialog()
				Log.e(activity.javaClass.simpleName, "Error Updating ")
			}
	}
	fun loadUserData(activity: MyProfileActivity){
		mFireStore.collection(Constants.USERS)
			.document(getCurrentUserId())
			.get()
			.addOnSuccessListener {document ->
				val loggedInUser = document.toObject(User::class.java)!!
//				activity.hideProgressDialog()
					activity.setUserDataInUi(loggedInUser)
			}.addOnFailureListener { e ->

					activity.hideProgressDialog()
				}
				Log.e(activity.javaClass.simpleName, "Error writing document")
			}

	fun saveFieldToFirebase(activity: FieldRegistration, field: Fields) {
		mFireStore.collection(Constants.FIELDS)
			.document(field.fieldId)
			.set(field, SetOptions.merge())
			.addOnSuccessListener {
				activity.hideProgressDialog()
				Toast.makeText(activity, "Created successfully", Toast.LENGTH_SHORT).show()
				activity.fieldCreatedSuccess()
			}.addOnFailureListener { e ->
				activity.hideProgressDialog()
				Log.e(activity.javaClass.simpleName, "Error writing document")
				Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
			}
	}

	fun getFieldsList(activity: AccessData){
		mFireStore.collection(Constants.FIELDS)
			.whereEqualTo("owner", getCurrentUserId())
			.get()
			.addOnSuccessListener {
					document->
				Log.i(activity.javaClass.simpleName, document.documents.toString())
				val fieldList: ArrayList<Fields> = ArrayList()
				for (i in document.documents){

					val field = i.toObject(Fields::class.java)
					field!!.fieldId = i.id
					fieldList.add(field)
				}
					activity.getAllFields(fieldList)
				activity.hideProgressDialog()
			}
			.addOnFailureListener {
				activity.hideProgressDialog()
				// TODO: notify failure
			}
	}

	fun updateField(activity: FieldRegistration, userHashMap: HashMap<String, Any>, field: Fields){
		mFireStore.collection(Constants.FIELDS)
			.document(field.fieldId)
			.update(userHashMap)
			.addOnSuccessListener {
				Toast.makeText(activity, "Updated successfully", Toast.LENGTH_SHORT).show()
				Log.i(activity.javaClass.simpleName, "Field Updated Successfully")
				activity.updateFieldSuccess()
			}
			.addOnFailureListener { exception ->
			activity.hideProgressDialog()
				Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
				Log.e(activity.javaClass.simpleName, "Error", exception)
			}
	}

	fun filterFields(activity: AccessData,text: String){
		mFireStore.collection(Constants.FIELDS)
			.whereEqualTo("branchName",text)
			.get()
			.addOnSuccessListener {
					document->
				Log.i(activity.javaClass.simpleName, document.documents.toString())
				val fieldList: ArrayList<Fields> = ArrayList()
				for (i in document.documents){

					val field = i.toObject(Fields::class.java)
					field!!.fieldId = i.id
					fieldList.add(field)
				}
				activity.hideProgressDialog()
				activity.getAllFields(fieldList)
			}
			.addOnFailureListener {
				activity.hideProgressDialog()
				Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
			}
	}

	fun getTreesList(activity: MapsActivity,){
		mFireStore.collection(Constants.TREES)
			.get()
			.addOnSuccessListener {
					document->
				Log.i(activity.javaClass.simpleName, document.documents.toString())
				val treeList: ArrayList<Trees> = ArrayList()
				for (i in document.documents){

					val tree = i.toObject(Trees::class.java)
					tree!!.treeId = i.id
					treeList.add(tree)
				}
				activity.fetchLocationFromDatabase(treeList)
//				activity.hideProgressDialog()

			}
			.addOnFailureListener {
				activity.hideProgressDialog()
				Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
			}
	}

	fun getTreesList(activity: FieldDetailActivity,field: Fields){
		mFireStore.collection(Constants.TREES)
			.whereEqualTo(Constants.FARM_ID,field.farmId )
			.get()
			.addOnSuccessListener {
					document->
				Log.i(activity.javaClass.simpleName, document.documents.toString())
				val treeList: ArrayList<Trees> = ArrayList()
				for (i in document.documents){

					val tree = i.toObject(Trees::class.java)
					tree!!.treeId = i.id
					treeList.add(tree)
				}
					activity.getAllTrees(treeList)
				activity.hideProgressDialog()
			}
			.addOnFailureListener {
					activity.hideProgressDialog()
				Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
			}
	}

	fun saveTreeToFirebase(activity: TreeRegistration, trees: Trees) {
		mFireStore.collection(Constants.TREES)
			.document(trees.treeId)
			.set(trees, SetOptions.merge())
			.addOnSuccessListener {
				activity.hideProgressDialog()
				Toast.makeText(activity, "Tree Registered successfully", Toast.LENGTH_SHORT).show()
				activity.treeCreatedSuccess()
			}.addOnFailureListener { e ->
				activity.hideProgressDialog()
				Log.e(activity.javaClass.simpleName, "Error writing document")
				Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
			}
	}

	fun updateTree(activity: TreeRegistration, treeHashMap: HashMap<String, Any>, trees: Trees){
		mFireStore.collection(Constants.TREES)
			.document(trees.treeId)
			.update(treeHashMap)
			.addOnSuccessListener {
				Toast.makeText(activity, "Updated successfully", Toast.LENGTH_SHORT).show()
				Log.i(activity.javaClass.simpleName, "Tree Details Updated Successfully")
				activity.updateTreeSuccess()
			}
			.addOnFailureListener { exception ->
				activity.hideProgressDialog()
				Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
				Log.e(activity.javaClass.simpleName, "Error", exception)
			}
	}

	fun getVisitsList(activity: TreeDetailActivity, trees: Trees){
		mFireStore.collection(Constants.VISITS)
			.whereEqualTo("treeId",trees.treeId )
			.get()
			.addOnSuccessListener {
					document->
				Log.i(activity.javaClass.simpleName, document.documents.toString())
				val visitList: ArrayList<Visit> = ArrayList()
				for (i in document.documents){

					val visit = i.toObject(Visit::class.java)
					visit!!.visitId = i.id
					visitList.add(visit)
				}
				activity.getAllVisits(visitList)
				activity.hideProgressDialog()
			}
			.addOnFailureListener {
				activity.hideProgressDialog()
				Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
			}
	}

	fun saveVisitToFirebase(activity: VisitRegistration, visit: Visit) {
		mFireStore.collection(Constants.VISITS)
			.document(visit.visitId)
			.set(visit, SetOptions.merge())
			.addOnSuccessListener {
				activity.hideProgressDialog()
				Toast.makeText(activity, "Created successfully", Toast.LENGTH_SHORT).show()
				activity.treeCreatedSuccess()
			}.addOnFailureListener { e ->
				activity.hideProgressDialog()
				Log.e(activity.javaClass.simpleName, "Error writing document")
				Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
			}
	}

	fun updateVisit(activity: VisitRegistration, visitHashMap: HashMap<String, Any>, visit: Visit){
		mFireStore.collection(Constants.VISITS)
			.document(visit.visitId)
			.update(visitHashMap)
			.addOnSuccessListener {
				Toast.makeText(activity, "Updated successfully", Toast.LENGTH_SHORT).show()
				Log.i(activity.javaClass.simpleName, "Tree Details Updated Successfully")
				activity.updateVisitSuccess()
			}
			.addOnFailureListener { exception ->
				activity.hideProgressDialog()
				Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
				Log.e(activity.javaClass.simpleName, "Error", exception)
			}
	}
}


