package vn.app.qrcode.base

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import com.base.common.base.viewmodel.BaseViewModel

abstract class BaseAppToolbarActivity<E, T : ViewDataBinding, V : BaseViewModel<E>> :
    BaseAppActivity<E, T, V>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setup Toolbar, setup homeAsUpIndicator-icon is style
        setSupportActionBar(getToolBar())
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    abstract fun getToolBar(): Toolbar

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}