package vn.app.qrcode.ui.detail

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.base.common.base.fragment.BaseMVVMFragment
import com.base.common.base.viewmodel.CommonEvent
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import vn.app.qrcode.R
import vn.app.qrcode.data.model.MediaItemData
import vn.app.qrcode.databinding.FragmentDetailBinding

class DetailFragment : BaseMVVMFragment<CommonEvent, FragmentDetailBinding, DetailViewModel>() {
    override val layoutId: Int
        get() = R.layout.fragment_detail
    override val viewModel: DetailViewModel by viewModel()
    private val args: DetailFragmentArgs by navArgs()
    private var mediaItem: MediaItemData? = null

    companion object {
        fun newInstance() = DetailFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaItem = args.mediaItem
        viewModel.subscribeService(args.mediaItem.mediaId)
       viewModel.playRecord()
        println("AAA detailFragment ${args.mediaItem.mediaUri}")
        setWebView()
        // Always true, but lets lint know that as well.
        val context = activity ?: return

        // Attach observers to the LiveData coming from this ViewModel
        viewModel.mediaMetadata.observe(viewLifecycleOwner,
            Observer { mediaItem ->
                println("AAA mediaItem in detailFragment ${mediaItem.id}")
                updateUI(view, mediaItem)
            })
        viewModel.mediaButtonRes.observe(viewLifecycleOwner,
            Observer { res ->
                viewDataBinding.mediaButton.setImageResource(res)
            })
        viewModel.mediaPosition.observe(viewLifecycleOwner,
            Observer { pos ->
                viewDataBinding.position.text = NowPlayingMetadata.timestampToMSS(context, pos)
            })

        // Setup UI handlers for buttons
        viewDataBinding.mediaButton.setOnClickListener {
            viewModel.mediaMetadata.value?.let { viewModel.playMediaId(it.id) }
        }

        // Initialize playback duration and position to zero
        viewDataBinding.duration.text = NowPlayingMetadata.timestampToMSS(context, 0L)
        viewDataBinding.position.text = NowPlayingMetadata.timestampToMSS(context, 0L)
    }

    /**
     * Internal function used to update all UI elements except for the current item playback
     */
    private fun updateUI(view: View, metadata: NowPlayingMetadata) = with(viewDataBinding) {
//        if (metadata.albumArtUri == Uri.EMPTY) {
//            albumArt.setImageResource(R.drawable.ic_album_black_24dp)
//        } else {
//            Glide.with(view)
//                .load(metadata.albumArtUri)
//                .into(albumArt)
//        }
        title.text = metadata.title
        subtitle.text = metadata.subtitle
        duration.text = metadata.duration
    }

    private fun setWebView() {
        println("AAA ${args.mediaItem}")
        viewDataBinding.webView.apply {
            webViewClient = WebViewClient()
            args.mediaItem.mediaUri?.let { loadUrl(it) }
            settings.javaScriptEnabled = true
            settings.setSupportZoom(true)
        }
    }

}