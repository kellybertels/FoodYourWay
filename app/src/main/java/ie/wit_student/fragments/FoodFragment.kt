package ie.wit_student.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import ie.wit_student.R
import ie.wit_student.main.FYWApp
import ie.wit_student.models.FoodModel
import ie.wit_student.utils.*
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_list.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.lang.String.format
import java.util.HashMap


class FoodFragment : Fragment(), AnkoLogger {

    lateinit var app: FYWApp
    var totalDonated = 0
    lateinit var loader : AlertDialog
    lateinit var eventListener : ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as FYWApp
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_list, container, false)
        loader = createLoader(activity!!)
        activity?.title = getString(R.string.action_donate)


        root.amountPicker.minValue = 1
        root.amountPicker.maxValue = 10

        root.amountPicker.setOnValueChangedListener { _, _, newVal ->
            //Display the newly selected number to paymentAmount
            root.paymentAmount.setText("$newVal")
        }
        setButtonListener(root)
        return root;
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            FoodFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    fun setButtonListener( layout: View) {
        layout.addButton.setOnClickListener {
            val amount = if (layout.paymentAmount.text.isNotEmpty())
                layout.paymentAmount.text.toString().toInt() else layout.amountPicker.value



                val alergens =
                       if(layout.alergensGroup.checkedRadioButtonId == R.id.GlutenFree2) "GlutenFree"
                else "DairyFree"

                writeNewOrder(FoodModel(paymenttype = alergens, amount = amount,
                                               profilepic = app.userImage.toString(),
                                               email = app.auth.currentUser?.email))

        }
    }

    override fun onResume() {
        super.onResume()
        getTotalDonated(app.auth.currentUser?.uid)
    }

    override fun onPause() {
        super.onPause()
        if(app.auth.uid != null)
            app.database.child("user-products")
                    .child(app.auth.currentUser!!.uid)
                    .removeEventListener(eventListener)
    }

    fun writeNewOrder(product: FoodModel) {
        // Create new product at /products & /products/$uid
        showLoader(loader, "Adding Order to Firebase")
        info("Firebase DB Reference : $app.database")
        val uid = app.auth.currentUser!!.uid
        val key = app.database.child("products").push().key
        if (key == null) {
            info("Firebase Error : Key Empty")
            return
        }
        product.uid = key
        val productValues = product.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates["/products/$key"] = productValues
        childUpdates["/user-products/$uid/$key"] = productValues

        app.database.updateChildren(childUpdates)
        hideLoader(loader)
    }

    fun getTotalDonated(userId: String?) {
        eventListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                info("Firebase Order error : ${error.message}")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                totalDonated = 0
                val children = snapshot.children
                children.forEach {
                    val product = it.getValue<FoodModel>(FoodModel::class.java)
                    totalDonated += product!!.amount
                }

                totalSoFar.text = format("$ $totalDonated")
            }
        }

        app.database.child("user-products").child(userId!!)
            .addValueEventListener(eventListener)
    }
}
