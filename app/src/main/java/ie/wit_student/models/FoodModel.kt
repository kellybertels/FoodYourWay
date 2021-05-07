package ie.wit_student.models

import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import ie.wit_student.fragments.app
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class FoodModel(
    var uid: String? = "",
    var paymenttype: String = "N/A ",
    var amount: Int = 0,
    var message: String = "a message",
    var foodname: String ="Item Name",
    var upvotes: Int = 0,
    var profilepic: String = "",
    var email: String? = "joe@bloggs.com",
    //var dairy: String = "dairy",
   // var gluten: String = "gluten"

)
    : Parcelable
{
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "alergens" to paymenttype,
            "amount" to amount,
            "message" to message,
            "foodname" to foodname,
            "upvotes" to upvotes,
            "profilepic" to profilepic,
            "email" to email
        )
    }
}



