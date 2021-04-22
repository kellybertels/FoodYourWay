package ie.wit.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import ie.wit.R
import ie.wit.adapters.FAdapter
import ie.wit.adapters.OrdersListener
import ie.wit.main.FYWApp
import ie.wit.models.FoodModel
import ie.wit.utils.*
import kotlinx.android.synthetic.main.fragment_basket.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class AllOrdersFragment : BasketFragment(),
    OrdersListener {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_basket, container, false)
        activity?.title = getString(R.string.menu_orders_all)

        root.recyclerView.setLayoutManager(LinearLayoutManager(activity))
        setSwipeRefresh()

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AllOrdersFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    override fun setSwipeRefresh() {
        root.swiperefresh.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                root.swiperefresh.isRefreshing = true
                getAllUsersProducts()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        getAllUsersProducts()
    }

    fun getAllUsersProducts() {
        loader = createLoader(activity!!)
        showLoader(loader, "Downloading All Users Orders from Firebase")
        val productsList = ArrayList<FoodModel>()
        app.database.child("products")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    info("Firebase Order error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    hideLoader(loader)
                    val children = snapshot.children
                    children.forEach {
                        val product = it.
                            getValue<FoodModel>(FoodModel::class.java)

                        productsList.add(product!!)
                        root.recyclerView.adapter =
                            FAdapter(productsList, this@AllOrdersFragment,true)
                        root.recyclerView.adapter?.notifyDataSetChanged()
                        checkSwipeRefresh()

                        app.database.child("products").removeEventListener(this)
                    }
                }
            })
    }
}
