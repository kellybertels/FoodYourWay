package ie.wit_student.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ie.wit_student.R
import ie.wit_student.fragments.FoodFragment
import ie.wit_student.models.FoodModel
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.card_product.view.*
import kotlinx.android.synthetic.main.card_product.view.alergens
import kotlinx.android.synthetic.main.fragment_edit.view.*
import kotlinx.android.synthetic.main.fragment_list.view.*


interface OrdersListener {
    fun onOrderClick(product: FoodModel)
}

class FAdapter(var products: ArrayList<FoodModel>,
               private val listener: OrdersListener, reportall: Boolean)
    : RecyclerView.Adapter<FAdapter.MainHolder>() {

    val reportAll = reportall

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.card_product,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val product = products[holder.adapterPosition]
        holder.bind(product,listener,reportAll)
    }

    override fun getItemCount(): Int = products.size

    fun removeAt(position: Int) {
        products.removeAt(position)
        notifyItemRemoved(position)
    }

    //constructs the RecyclerView for the list of products in Basket
    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(product: FoodModel, listener: OrdersListener, reportAll: Boolean) {
            itemView.tag = product
            itemView.paymentamount.text = product.amount.toString()
            itemView.alergens.text = product.paymenttype
           //itemView.appSubtitle.text =product.message
            itemView.messageDesc.text = product.message
            itemView.textItemName.text = product.foodname
            itemView.order_name.text = product.foodname

            if(!reportAll)
                itemView.setOnClickListener { listener.onOrderClick(product) }

//if product has an image set this image else sets a "brocoli" default image.// profilepic is the image or the product.
            else

            if(!product.profilepic.isEmpty()) {
                Picasso.get().load(product.profilepic.toUri())
                    //.resize(180, 180)
                    .transform(CropCircleTransformation())
                    .into(itemView.imageIcon)
            }
            else
                itemView.imageIcon.setImageResource(R.mipmap.ic_brocoli)
        }



    }
}