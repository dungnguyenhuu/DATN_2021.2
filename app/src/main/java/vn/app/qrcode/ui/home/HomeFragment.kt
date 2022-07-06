package vn.app.qrcode.ui.home

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.base.viewmodel.CommonEvent
import com.example.android.uamp.common.Utils
import com.example.android.uamp.media.library.UAMP_BROWSABLE_ROOT
import com.example.android.uamp.media.library.UAMP_RECOMMENDED_ROOT
import com.example.android.uamp.media.model.PrefixRoot
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.app.qrcode.R
import vn.app.qrcode.databinding.FragmentHomeBinding

class HomeFragment : BaseMVVMFragment<CommonEvent, FragmentHomeBinding, HomeViewModel>() {

    companion object {
        const val TAG = "HomeFragment"
    }

    override val layoutId: Int
        get() = R.layout.fragment_home
    override val viewModel: HomeViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewEvent()
        setupNewsRecycleView()
    }

    private fun setupViewEvent() {
        viewModel.subscribeService(UAMP_BROWSABLE_ROOT)
        val categoryAdapter = CategoryAdapter { clickedItem ->
            findNavController().navigate(HomeFragmentDirections.actionLoginToCategoryFragment(clickedItem.mediaId))
        }
        viewDataBinding.rvListCategory.adapter = categoryAdapter

        viewModel.mediaItems.observe(viewLifecycleOwner,
            Observer { list ->
                categoryAdapter.submitList(list)
            })
    }

    private fun setupNewsRecycleView() {
        val mediaId = Utils.generalMediaItemId(PrefixRoot.API, "vne-tin-moi-nhat")
        viewModel.getNewsSubscribeService(mediaId)
        val newsAdapter = NewsAdapter { item ->
           findNavController().navigate(HomeFragmentDirections.actionHomeToDetailFragment(item))
        }
        viewDataBinding.rvNewsList.adapter = newsAdapter
        viewModel.itemNewsList.observe(viewLifecycleOwner) { items ->
            newsAdapter.submitList(items)
        }
    }

}