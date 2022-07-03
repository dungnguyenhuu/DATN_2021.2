package vn.app.qrcode.ui.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import vn.app.qrcode.R
import vn.app.qrcode.data.model.MediaItemData
import vn.app.qrcode.databinding.ItemLargeNewsBinding

class News1Adapter(
    private val itemClickedListener: (MediaItemData) -> Unit
) : ListAdapter<MediaItemData, News1Adapter.ItemNewsViewHolder>(MediaItemData.diffCallback) {

    class ItemNewsViewHolder(
        private val binding: ItemLargeNewsBinding,
        private val itemClickedListener: (MediaItemData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MediaItemData) {
            binding.tvTitleNews.text = item.title
            Glide.with(binding.ivNewsThumbnail)
                .load(item.albumArtUri)
                .placeholder(R.drawable.ic_logo)
                .into(binding.ivNewsThumbnail)
            binding.root.setOnClickListener {
                itemClickedListener(item)
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemNewsViewHolder {
        return ItemNewsViewHolder(
            ItemLargeNewsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), itemClickedListener
        )
    }

    override fun onBindViewHolder(holder: ItemNewsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}
