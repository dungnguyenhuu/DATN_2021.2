package vn.app.qrcode.ui.category

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.base.viewmodel.CommonEvent
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.app.qrcode.R
import vn.app.qrcode.databinding.FragmentCategoryBinding

class CategoryFragment : BaseMVVMFragment<CommonEvent, FragmentCategoryBinding, CategoryViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_category
    override val viewModel: CategoryViewModel by viewModel()

    private val args: CategoryFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewEvent()
    }

    private fun setupViewEvent() {
        viewModel.subscribeService(args.mediaId)
        val categoryNewAdapter = CategoryAdapter { clickedItem ->
            println("AAA ${clickedItem.mediaId}")
        }

        viewDataBinding.rvNewsCategory.adapter = categoryNewAdapter

        viewModel.mediaItems.observe(viewLifecycleOwner) {
            categoryNewAdapter.submitList(it)
        }
    }

}