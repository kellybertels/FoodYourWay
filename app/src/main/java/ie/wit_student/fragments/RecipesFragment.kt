package ie.wit_student.fragments


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

import ie.wit_student.R
import ie.wit_student.adapters.FAdapter
import ie.wit_student.adapters.OrdersListener
import ie.wit_student.main.FYWApp
import ie.wit_student.models.FoodModel
import ie.wit_student.models.FoodRecipe
import ie.wit_student.utils.*
//import kotlinx.android.synthetic.main.fragment_basket.view.*
import kotlinx.android.synthetic.main.fragment_food_recipes.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info




    // TODO: Rename and change types of parameters
    // private var param1: String? = null
    // private var param2: String? = null


    lateinit var app: FYWApp
    lateinit var loader: AlertDialog
    lateinit var root: View


    open class RecipesFragment : Fragment(), AnkoLogger
            {

        lateinit var app: FYWApp
        lateinit var loader: AlertDialog
        lateinit var root: View

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            app = activity?.application as FYWApp
        }

                companion object {
                    @JvmStatic
                    fun newInstance() =
                            RecipesFragment().apply {
                                arguments = Bundle().apply { }
                            }
                }
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_food_recipes, container, false)
        }




    }





