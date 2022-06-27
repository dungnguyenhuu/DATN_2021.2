package vn.app.qrcode.base

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.base.common.base.adapter.BaseAdapter
import com.base.common.base.viewmodel.BaseViewModel
import kotlinx.android.synthetic.main.base_static_list_activity.*
import vn.app.qrcode.R
import vn.app.qrcode.databinding.BaseStaticListActivityBinding

abstract class BaseStaticListActivity<E, V : BaseViewModel<E>, MODEL> :
    BaseAppToolbarActivity<E, BaseStaticListActivityBinding, V>() {
    override fun getToolBar(): Toolbar = toolbar
    abstract val adapter: BaseAdapter<MODEL>
    override val layoutId: Int = R.layout.base_static_list_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initList()
    }

    open fun setTitleToolbar(title: String) {
        toolbarTitle.text = title
    }

    open fun initList() {
        rvStaticList.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter.setUpRecycleViewLoadMore(rvStaticList)
        rvStaticList.adapter = adapter
        adapter.setOnClickListener(object : BaseAdapter.OnClickItemListener<MODEL> {
            override fun onClickItem(item: MODEL, position: Int) {
                onClickRecycleViewItem(item)
            }
        })
    }


    open fun onClickRecycleViewItem(item: MODEL) {}
}