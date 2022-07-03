package vn.app.qrcode.ui.news

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.base.viewmodel.CommonEvent
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.app.qrcode.R
import vn.app.qrcode.databinding.FragmentNewsBinding
import vn.app.qrcode.ui.category.CategoryFragmentArgs
import vn.app.qrcode.ui.home.NewsAdapter

class NewsFragment : BaseMVVMFragment<CommonEvent, FragmentNewsBinding, NewsViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_news
    override val viewModel: NewsViewModel by viewModel()

    private val args: NewsFragmentArgs by navArgs()

    companion object {
        fun newInstance() = NewsFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewEvent()
    }

    private fun setupViewEvent() {
        viewModel.subscribeService(args.mediaId)
        val newsAdapter = News1Adapter { item ->
            findNavController().navigate(NewsFragmentDirections.actionNewsFragmentToDetailFragment(item))
        }
        viewDataBinding.rvNewsList.adapter = newsAdapter
        viewModel.mediaItems.observe(viewLifecycleOwner) { items ->
            newsAdapter.submitList(items)
        }
    }

}