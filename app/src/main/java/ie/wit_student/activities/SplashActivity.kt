package ie.wit_student.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.os.Handler
import ie.wit_student.R
import org.jetbrains.anko.startActivity





class SplashActivity : AppCompatActivity() {

    // This is the loading time of the splash screen
    private val splash_time_out :Long = 3000 // 1 sec
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)


        Handler().postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity

            //startActivity(Intent(this,MainActivity::class.java))
           // startActivity(Intent(this,Login))
           // startActivity(Intent(this,Login))
            startActivity<Login>()
            // close this activity
            finish()
        }, splash_time_out )
    }
}