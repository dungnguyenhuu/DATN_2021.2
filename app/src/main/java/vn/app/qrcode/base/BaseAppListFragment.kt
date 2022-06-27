package vn.app.qrcode.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.base.common.base.adapter.BaseAdapter

abstract class BaseAppListFragment<E, T : ViewDataBinding, V : BaseListViewModel<E>, MODEL> :
    BaseAppFragment<E, T, V>() {

    abstract val adapter: BaseAdapter<MODEL>
    abstract val recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
    }

    open fun initList() {
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        adapter.setUpRecycleViewLoadMore(recyclerView)
        recyclerView.adapter = adapter
        adapter.loadPublisher.observe(viewLifecycleOwner, Observer {
            viewModel.loadMore()
        })
        adapter.setOnClickListener(object : BaseAdapter.OnClickItemListener<MODEL> {
            override fun onClickItem(item: MODEL, position: Int) {
                onClickRecycleViewItem(item)
            }
        })
    }

    open fun handlerLoadData(data: List<MODEL>?) {
        try {
            when (viewModel.stateLoading) {
                LoadingState.INIT, LoadingState.REFRESH -> {
                    adapter.setDataList(data as ArrayList<MODEL>)
                    recyclerView.scrollToPosition(0)
                }
                LoadingState.LOADMORE -> {
                    adapter.addLoadMoreData(data as ArrayList<MODEL>)
                }
            }
        } catch (e: NullPointerException) {
            if (viewModel.stateLoading == LoadingState.LOADMORE) {
                adapter.removeLoadMoreView()
            }
        }
    }

    open fun onClickRecycleViewItem(item: MODEL) {}

}