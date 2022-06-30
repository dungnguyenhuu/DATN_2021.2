package vn.app.qrcode.ui.home

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.base.viewmodel.CommonEvent
import com.example.android.uamp.media.library.UAMP_BROWSABLE_ROOT
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
        val categoryAdapter = CategoryAdapter { clickedItem ->
            println("AAA click item $clickedItem")
            viewModel.subscribeService(UAMP_BROWSABLE_ROOT)
        }
        viewDataBinding.rvListCategory.adapter = categoryAdapter
        val categorylist = mutableListOf(
            CategoryNews("Express", Uri.parse("android.resource://vn.app.news/drawable/ic_album")),
            CategoryNews(
                "News",
                Uri.parse("android.resource://vn.app.news/drawable/ic_recommended")
            ),
            CategoryNews(
                "News",
                Uri.parse("android.resource://vn.app.news/drawable/ic_recommended")
            ),
            CategoryNews(
                "News",
                Uri.parse("android.resource://vn.app.news/drawable/ic_recommended")
            ),
            CategoryNews(
                "News",
                Uri.parse("android.resource://vn.app.news/drawable/ic_recommended")
            ),
            CategoryNews(
                "News",
                Uri.parse("android.resource://vn.app.news/drawable/ic_recommended")
            ),
            CategoryNews(
                "News",
                Uri.parse("android.resource://vn.app.news/drawable/ic_recommended")
            ),
            CategoryNews(
                "News",
                Uri.parse("android.resource://vn.app.news/drawable/ic_recommended")
            ),
            CategoryNews(
                "News",
                Uri.parse("android.resource://vn.app.news/drawable/ic_recommended")
            ),

            )
        categoryAdapter.submitList(categorylist)

        viewModel.mediaItems.observe(viewLifecycleOwner,
            Observer { list ->
                println("AAA list ${list.size}")
            })
    }

    fun setupNewsRecycleView() {
        val newsAdapter = NewsAdapter { item ->
            println("AAA ${item.title}")
        }
        viewDataBinding.rvNewsList.adapter = newsAdapter
        viewModel.itemNewsList.observe(viewLifecycleOwner) { items ->
            newsAdapter.submitList(items)
        }
    }

}