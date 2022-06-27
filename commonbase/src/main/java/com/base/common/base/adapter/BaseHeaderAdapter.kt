package com.base.common.base.adapter

import android.view.View

abstract class BaseHeaderAdapter<T>(data: ArrayList<String>, header: View) : BaseAdapter<String>(data) {

    protected val ITEM_VIEW_TYPE_HEADER = 0
    protected val ITEM_VIEW_TYPE_ITEM = 1

    protected val mHeader: View = header

    override fun getItemViewType(position: Int): Int {
        return if (isHeader(position)) ITEM_VIEW_TYPE_HEADER else ITEM_VIEW_TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    fun isHeader(position: Int): Boolean {
        return position == 0
    }
}