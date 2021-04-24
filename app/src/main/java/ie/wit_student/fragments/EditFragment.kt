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
import ie.wit_student.utils.createLoader
import ie.wit_student.utils.hideLoader
import ie.wit_student.utils.showLoader
import kotlinx.android.synthetic.main.fragment_edit.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class EditFragment : Fragment(), AnkoLogger {

    lateinit var app: FYWApp
    lateinit var loader : AlertDialog
    lateinit var root: View
    var editOrder: FoodModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as FYWApp

        arguments?.let {
            editOrder = it.getParcelable("editproduct")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_edit, container, false)
        activity?.title = getString(R.string.action_edit)
        loader = createLoader(activity!!)

        root.editAmount.setText(editOrder!!.amount.toString())
        root.editPaymenttype.setText(editOrder!!.paymenttype)
        root.editMessage.setText(editOrder!!.message)
        root.editUpvotes.setText(editOrder!!.upvotes.toString())

        root.editUpdateButton.setOnClickListener {
            showLoader(loader, "Updating Order on Server...")
            updateOrderData()
            updateOrder(editOrder!!.uid, editOrder!!)
            updateUserOrder(app.auth.currentUser!!.uid,
                               editOrder!!.uid, editOrder!!)
        }

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(product: FoodModel) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("editproduct",product)
                }
            }
    }

    fun updateOrderData() {
        editOrder!!.amount = root.editAmount.text.toString().toInt()
        editOrder!!.message = root.editMessage.text.toString()
        editOrder!!.upvotes = root.editUpvotes.text.toString().toInt()
    }

    fun updateUserOrder(userId: String, uid: String?, product: FoodModel) {
        app.database.child("user-products").child(userId).child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(product)
                        activity!!.supportFragmentManager.beginTransaction()
                        .replace(R.id.homeFrame, BasketFragment.newInstance())
                        .addToBackStack(null)
                        .commit()
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Order error : ${error.message}")
                    }
                })
    }

    fun updateOrder(uid: String?, product: FoodModel) {
        app.database.child("products").child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(product)
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Order error : ${error.message}")
                    }
                })
    }
}
