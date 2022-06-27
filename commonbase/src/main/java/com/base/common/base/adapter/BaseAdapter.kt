package com.base.common.base.adapter

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.base.common.R
import com.base.common.utils.ext.inflateExt
import org.jetbrains.anko.sdk27.coroutines.onClick
import timber.log.Timber

abstract class BaseAdapter<T>(var list: ArrayList<T>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //load more
    private val loadMoreType = 199
    var loadPublisher = MutableLiveData<Int>()
    private var pageCount: Int = 1
    var totalPage: Int = -1
    private var pagingThreshold = 5
    private var isLoading = false
    private var recyclerView: RecyclerView? = null
    //click

    fun setUpRecycleViewLoadMore(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.layoutManager?.let {
            if (it is GridLayoutManager) {
                it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (getItemViewType(position)) {
                            loadMoreType -> it.spanCount
                            else -> 1
                        }
                    }
                }
            }
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                recyclerView.layoutManager?.let {

                    val visibleItemCount = it.childCount
                    val totalItemCount = it.itemCount
                    val firstVisibleItemPosition = when (it) {
                        is LinearLayoutManager -> it.findLastVisibleItemPosition()
                        is GridLayoutManager -> it.findLastVisibleItemPosition()
                        else -> return
                    }
                    if (totalPage <= pageCount || isLoading) return
                    if ((visibleItemCount + firstVisibleItemPosition + pagingThreshold) >= totalItemCount) {
                        isLoading = true
                        pageCount++
                        addData(null as T, list.size)
                        loadPublisher.value = pageCount
                    }
                }
            }
        })
    }

    protected var onClickItemListener: OnClickItemListener<T>? = null

    fun setOnClickListener(onClickItemListener: OnClickItemListener<T>) {
        this.onClickItemListener = onClickItemListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == loadMoreType) return LoadMoreViewHolder(parent.inflateExt(R.layout.item_load_more))
        return onCreateViewHolderBase(parent, viewType)
    }

    abstract fun onCreateViewHolderBase(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LoadMoreViewHolder) return
        onBindViewHolderBase(holder, position)
        holder.itemView.onClick {
            onClickItemListener?.onClickItem(list[position],position)
        }
    }

    abstract fun onBindViewHolderBase(holder: RecyclerView.ViewHolder?, position: Int)

    override fun getItemCount(): Int {
        return if (list.isNotEmpty()) list.size else 0
    }

    fun getDataList(): List<T> {
        return list
    }

    fun setDataList(datas: ArrayList<T>) {
        resetPage()
        this.list = datas
        notifyDataSetChanged()
    }

    /**
     * add item to list
     */
    fun addData(item: T, position: Int = list.size) {
        list.add(position, item)
        if (recyclerView != null) {
            recyclerView?.post {
                notifyItemInserted(list.size - 1)
            }
        } else {
            notifyItemInserted(list.size - 1)
        }
    }

    /**
     * add more data at bottom list
     */
    fun addLoadMoreData(item: ArrayList<T>) {
        removeLoadMoreView()
        addListData(item, pageCount == 1)
    }

    fun removeLoadMoreView() {
        isLoading = false
        if (pageCount != 1) {
            val index = list.indexOf(null as T)
            if (index != -1) {
                list.remove(null as T)
                notifyItemRemoved(index)
            }
        }
    }

    /**
     * add list data with clear option
     */
    private fun addListData(listData: MutableList<T>, isDelete: Boolean) {
        if (isDelete) {
            list.clear()
        }
        val oldLastIndex = list.size - 1
        list.addAll(listData)
        if (recyclerView == null) {
            notifyDataSetChanged()
            return
        }
        recyclerView?.post {
            try {
                Timber.e("Loadmore oldLastIndex $oldLastIndex - ${listData.size} - ${list.size}")
                if (oldLastIndex >= 0) {
                    notifyItemRangeChanged(oldLastIndex, listData.size)
                } else {
                    notifyDataSetChanged()
                }
            }catch (e : Exception){
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (list[position] == null) return loadMoreType
        return super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * delete item at position
     */
    fun deletePositionData(position: Int) {
        if (position >= 0 && position < list.size) {
            list.remove(list[position])
            notifyDataSetChanged()
        } else {
            Log.d("del", "delete item failed, position error!")
        }
    }

    /**
     * delete all data
     */
    fun deleteAllData() {
        list.removeAll(list)
        notifyDataSetChanged()
    }

    /**
     * return paging observer
     */
    fun loadMore(): LiveData<Int> = this.loadPublisher


    /**
     * return page count
     */
    fun getPageCount(): Int {
        return this.pageCount
    }

    /**
     * set loading
     */
    fun setLoading(isLoading: Boolean) {
        this.isLoading = isLoading
    }

    fun resetPage() {
        pageCount = 1
    }

    fun setPagingThreshold(threshold: Int) {
        this.pagingThreshold = threshold
    }

    interface OnClickItemListener<T> {
        fun onClickItem(item: T, position: Int)
    }

    class LoadMoreViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}