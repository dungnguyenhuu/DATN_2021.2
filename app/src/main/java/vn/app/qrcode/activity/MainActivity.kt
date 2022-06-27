package vn.app.qrcode.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.base.common.base.fragment.BaseNavFragmentHost
import vn.app.qrcode.R
import vn.app.qrcode.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var viewBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    override fun onBackPressed() {
        val fragmentHost =
            supportFragmentManager.findFragmentById(R.id.mainContainer) as BaseNavFragmentHost<*, *, *>
        if (!fragmentHost.handleBackPress())
            super.onBackPressed()
    }
}