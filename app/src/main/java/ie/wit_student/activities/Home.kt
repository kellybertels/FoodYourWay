package ie.wit_student.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.databinding.adapters.SearchViewBindingAdapter.setOnQueryTextListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import ie.wit_student.R
import ie.wit_student.fragments.*
//import ie.wit_student.fragments.RecipesFragment.RecipesFragment.Companion.newInstance
//import.ie.wit_student.fragments.RecipesFragment.companion.newInstance
import ie.wit_student.main.FYWApp
import ie.wit_student.utils.*
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.home.*
import kotlinx.android.synthetic.main.nav_header_home.*
import kotlinx.android.synthetic.main.nav_header_home.view.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import androidx.databinding.adapters.SearchViewBindingAdapter.setOnQueryTextListener



class Home : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    lateinit var ft: FragmentTransaction
    lateinit var app: FYWApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
        setSupportActionBar(toolbar)
        app = application as FYWApp
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action",
                Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }

        navView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.getHeaderView(0).nav_header_email.text = app.auth.currentUser?.email

        //Checking if Google User, upload google profile pic
        checkExistingPhoto(app,this)

        navView.getHeaderView(0).imageView
            .setOnClickListener { showImagePicker(this,1) }

        ft = supportFragmentManager.beginTransaction()

        val fragment = FoodFragment.newInstance()
        ft.replace(R.id.homeFrame, fragment)
        ft.commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_menu ->
                navigateTo(FoodFragment.newInstance())
            R.id.nav_basket ->
                navigateTo(BasketFragment.newInstance())
            R.id.nav_orders_all ->
                navigateTo(AllOrdersFragment.newInstance())
            R.id.nav_favourites->
                navigateTo(RecipesFragment.newInstance())
            R.id.nav_aboutus ->
                navigateTo(AboutUsFragment.newInstance())
            R.id.nav_sign_out -> signOut()

            else -> toast("You Selected Something Else")
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_donate -> toast("You Selected Settings")
            R.id.action_report -> toast("You Selected Contact")
          //  R.id.app_bar_search -> kellySearch()




        }
        return super.onOptionsItemSelected(item)
    }


   /* override fun kellySearch(){

        val manager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem (R.id.app_bar_search)
        val searchView = searchItem?.actionView as SearchView


        searchView.setSearchableInfo(manager.getSearchableInfor(componentName))

        SearchView.setOnQueryTextListener (object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                searchView.setQuery(query"",false)
                searchItem.collapseActionView()
                Toast.makeText(this@MainActivity, text "Looking for $query", Toast.LENGTH_LONG).show()
                return true
            }
            override fun onQueryTextChange(newText:String?):Boolean{
                return false
            }
        })
    }

*/


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
         else
            super.onBackPressed()
    }

    private fun navigateTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.homeFrame, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun signOut() {
        app.googleSignInClient.signOut().addOnCompleteListener(this) {
            app.auth.signOut()
            startActivity<Login>()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> {
                if (data != null) {
                    writeImageRef(app,readImageUri(resultCode, data).toString())
                    Picasso.get().load(readImageUri(resultCode, data).toString())
                        .resize(180, 180)
                        .transform(CropCircleTransformation())
                        .into(navView.getHeaderView(0).imageView, object : Callback {
                            override fun onSuccess() {
                                // Drawable is ready
                                uploadImageView(app,navView.getHeaderView(0).imageView)
                            }
                            override fun onError(e: Exception) {}
                        })
                }
            }
        }
    }
}
