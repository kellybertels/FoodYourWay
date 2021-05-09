package ie.wit_student.fragments

import org.jetbrains.anko.toast

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import ie.wit_student.R
import ie.wit_student.adapters.FAdapter
import ie.wit_student.main.FYWApp
import ie.wit_student.models.FoodModel
import ie.wit_student.utils.*
import kotlinx.android.synthetic.main.fragment_basket.view.*
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_list.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.HashMap


class FoodFragment : Fragment(), AnkoLogger {

    lateinit var app: FYWApp
    var totalDonated = 0
    lateinit var loader : AlertDialog
    lateinit var eventListener : ValueEventListener
    lateinit var root: View
    var foodname: String = ""
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
        val spinner: Spinner = root.findViewById(R.id.food_spinner)
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            root.context,
            R.array.food_array,//will be changed on future to array created from json file( create method to load from json array
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                foodname= parent.getItemAtPosition(position) as String

                //tests if the array selected works showing id and position in the array food_array
              //  showLoader(loader, "onItemSelected position = $position id = $id")
               // info("onItemSelected position = $position id = $id")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }



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

                writeNewOrder(
                        FoodModel(
                                paymenttype = alergens,
                                amount = amount,
                                profilepic = app.userImage.toString(),
                                email = app.auth.currentUser?.email,
                                foodname = foodname


                        ),
                                  //dairy = dairy,
                                 // gluten= gluten,
                                //I think how I set up the food model here is wrong, it should be outside the foodmodel. in products.

                       )
        }
    }




    //function I am making to filter the items ( items from the database table items000, item1 milk, item2 bread,
    //once checkbok dairy is selected it show products with dairy, once checkbox with gluten selected show products with gluten. (or oposite gluten free, products(I didnt decide yet))
//that outputs on listview the products that fits the filter selected.

    fun setFilterTheItems(layout: View){
        layout.filterButton.setOnClickListener {

        }

    }


// kellyItem is a local variable that represents the array located in foodmodel, items000 is the collection name in the db,
    fun filterAvailableItems() {
        loader = createLoader(activity!!)
    //when function is loaded and slow
        showLoader(loader, "Downloading exiting items from the database")

        val kellyItem = ArrayList<FoodModel>()

    //go to the database
        app.database.child("items000")
                //executes the listener
                .addValueEventListener(object : ValueEventListener {
                    //if the function is cancelled
                    override fun onCancelled(error: DatabaseError) {


                        info("Firebase Order error : ${error.message}")
                    }
                    override fun onDataChange(snapshot: DataSnapshot) {
                        hideLoader(loader)
                        val children = snapshot.children
                        children.forEach {
                            val product = it.
                            getValue<FoodModel>(FoodModel::class.java)

                            kellyItem.add(product!!)
                          //  root.recyclerView.adapter =
                                   // FAdapter(kellyItem, this@OrdersListener,true) //he isnt locating the value items00 inside the food model? food adapter?
                          //  root.recyclerView.adapter?.notifyDataSetChanged()


                          //  app.database.child("products").removeEventListener(this)
                        }
                    }
                })
    }






// this code im working on.
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
//childrenkellylocalvariable
                //snapchot is what gets the item from the children in database table
val childrenKelly =snapshot.children
                //what to do with the item (gets value from ) - reuse this function to populate with recipes
                childrenKelly.forEach {
                    val kellyitem =it.getValue<FoodModel>(FoodModel::class.java)
                }
//after get the data outside the brackets output the data
             //  listItemsId




                //Todo: Replace here this code for a code that will populate my listview
                //this code is geting totaldonated that is zero and geting snapshot from children, for each children value of item, it.getValue food model
                //output in totaldonated adding up with the amount I had
             //   totalDonated = 0
            //    val children = snapshot.children
            //    children.forEach {
            //        val product = it.getValue<FoodModel>(FoodModel::class.java)
                    //calculation to add up total donated with amount
            //        totalDonated += product!!.amount
           //     }
                //totalSoFar is an id
           //     totalSoFar.text = format("$ $totalDonated")
                //

            }
        }

        app.database.child("user-products").child(userId!!)
            .addValueEventListener(eventListener)
    }
}




