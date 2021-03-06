package ie.wit_student.utils

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.tasks.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageException
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import ie.wit_student.R
import ie.wit_student.main.FYWApp
import ie.wit_student.models.FoodModel
import ie.wit_student.models.UserPhotoModel
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.home.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import org.jetbrains.anko.info
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.HashMap

fun createLoader(activity: FragmentActivity) : AlertDialog {
    val loaderBuilder = AlertDialog.Builder(activity)
        .setCancelable(true) // 'false' if you want user to wait
        .setView(R.layout.loading)
    var loader = loaderBuilder.create()
    loader.setTitle(R.string.app_name)
    loader.setIcon(R.mipmap.ic_brocoli)

    return loader
}

fun showLoader(loader: AlertDialog, message: String) {
    if (!loader.isShowing()) {
        loader.setTitle(message)
        loader.show()
    }
}

fun hideLoader(loader: AlertDialog) {
    if (loader.isShowing())
        loader.dismiss()
}

fun convertImageToBytes(imageView: ImageView) : ByteArray {
    // Get the data from an ImageView as bytes
    lateinit var bitmap: Bitmap

    if(imageView is AdaptiveIconDrawable || imageView is AppCompatImageView)
        bitmap = imageView.drawable.toBitmap()
    else
        bitmap = (imageView.drawable as BitmapDrawable).toBitmap()

    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    return baos.toByteArray()
}

fun uploadImageView(app: FYWApp, imageView: ImageView) {
    val uid = app.auth.currentUser!!.uid
    val imageRef = app.storage.child("photos").child("${uid}.jpg")
    val uploadTask = imageRef.putBytes(convertImageToBytes(imageView))

    uploadTask.addOnFailureListener { object : OnFailureListener {
        override fun onFailure(error: Exception) {
            Log.v("Order", "uploadTask.exception" + error)
        }
    }
    }.addOnSuccessListener {
        uploadTask.continueWithTask { _ ->
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                app.userImage = task.result!!.toString().toUri()
                updateAllOrders(app)
                writeImageRef(app,app.userImage.toString())
                Picasso.get().load(app.userImage)
                    .resize(180, 180)
                    .transform(CropCircleTransformation())
                    .into(imageView)
            }
        }
    }
}

fun showImagePicker(parent: Activity, id: Int) {
    val intent = Intent()
    intent.type = "image/*"
    intent.action = Intent.ACTION_OPEN_DOCUMENT
    intent.addCategory(Intent.CATEGORY_OPENABLE)
    val chooser = Intent.createChooser(intent, R.string.select_profile_image.toString())
    parent.startActivityForResult(chooser, id)
}

fun readImageUri(resultCode: Int, data: Intent?): Uri? {
    var uri: Uri? = null
    if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
        try { uri = data.data }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return uri
}

fun updateAllOrders(app: FYWApp) {
    val userId = app.auth.currentUser!!.uid
    val userEmail = app.auth.currentUser!!.email
    var productRef = app.database.ref.child("products")
                                  .orderByChild("email")
    val userproductRef = app.database.ref.child("user-products")
                                  .child(userId).orderByChild("uid")

    productRef.equalTo(userEmail).addListenerForSingleValueEvent(
        object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {}
        override fun onDataChange(snapshot: DataSnapshot) {
            snapshot.children.forEach {
                it.ref.child("profilepic")
                    .setValue(app.userImage.toString())
            }
        }
    })

    userproductRef.addListenerForSingleValueEvent(
        object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {}
        override fun onDataChange(snapshot: DataSnapshot) {
            snapshot.children.forEach {
                it.ref.child("profilepic")
                    .setValue(app.userImage.toString())
            }
        }
    })

    writeImageRef(app, app.userImage.toString())
}

fun writeImageRef(app: FYWApp, imageRef: String) {
    val userId = app.auth.currentUser!!.uid
    val values = UserPhotoModel(userId,imageRef).toMap()
    val childUpdates = HashMap<String, Any>()

    childUpdates["/user-photos/$userId"] = values
    app.database.updateChildren(childUpdates)
}

fun validatePhoto(app: FYWApp, activity: Activity) {

    var imageUri: Uri? = null
    val imageExists = app.userImage.toString().length > 0
    val googlePhotoExists = app.auth.currentUser?.photoUrl != null

    if(imageExists)
        imageUri = app.userImage
    else
        if (googlePhotoExists)
            imageUri = app.auth.currentUser?.photoUrl!!

    if (googlePhotoExists || imageExists) {
        if(!app.auth.currentUser?.displayName.isNullOrEmpty())
        activity.navView.getHeaderView(0)
            .nav_header_name.text = app.auth.currentUser?.displayName
        else
            activity.navView.getHeaderView(0)
                .nav_header_name.text = activity.getText(R.string.nav_header_title)

        Picasso.get().load(imageUri)
            .resize(180, 180)
            .transform(CropCircleTransformation())
            .into(activity.navView.getHeaderView(0).imageView, object : Callback {
                override fun onSuccess() {
                    // Drawable is ready
                    uploadImageView(app,activity.navView.getHeaderView(0).imageView)
                }
                override fun onError(e: Exception) {}
            })
    }
    else    // New Regular User, upload default pic of A Brocoli
    {
        activity.navView.getHeaderView(0).imageView.setImageResource(R.mipmap.ic_brocoli)
        uploadImageView(app, activity.navView.getHeaderView(0).imageView)
    }
}

fun checkExistingPhoto(app: FYWApp,activity: Activity) {

    app.userImage = "".toUri()
    Log.v("Order","checkExistingPhoto 1 app.userImage : ${app.userImage}")

    app.database.child("user-photos").orderByChild("uid")
        .equalTo(app.auth.currentUser!!.uid)
        .addListenerForSingleValueEvent(object : ValueEventListener {

            //updates the photo of the user in the database? user_Image ( or product?)
        override fun onDataChange(snapshot: DataSnapshot ) {
            snapshot.children.forEach {
                val usermodel = it.getValue<UserPhotoModel>(UserPhotoModel::class.java)
                app.userImage = usermodel!!.profilepic.toUri()
                Log.v("Order","checkExistingPhoto 2 app.userImage : ${app.userImage}")
            }
            Log.v("Order","validatePhoto 3 app.userImage : ${app.userImage}")
            validatePhoto(app,activity)
        }
       override fun onCancelled(databaseError: DatabaseError ) {}
    })
}