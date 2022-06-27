package vn.app.qrcode.base.adapter

import android.util.SparseArray
import com.base.common.base.adapter.BaseAdapter
import vn.app.qrcode.base.BaseItemViewModel

abstract class BaseVMAdapter<T, VM : BaseItemViewModel<*, *>>(list: ArrayList<T>) :
    BaseAdapter<T>(list) {
    val viewModelProvide = SparseArray<VM>()

}