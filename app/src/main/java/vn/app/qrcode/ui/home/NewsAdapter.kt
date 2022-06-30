package vn.app.qrcode.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.uamp.media.model.ItemNews
import vn.app.qrcode.R
import vn.app.qrcode.databinding.ItemLargeNewsBinding

class NewsAdapter(
    private val itemClickedListener: (ItemNews) -> Unit
): ListAdapter<ItemNews, NewsAdapter.ItemNewsViewHolder>(DiffCallback) {

    class ItemNewsViewHolder(
        private val binding: ItemLargeNewsBinding,
        private val itemClickedListener: (ItemNews) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ItemNews) {
            binding.tvTitleNews.text = item.title
            Glide.with(binding.ivNewsThumbnail)
                .load(item.thumbnail)
                .placeholder(R.drawable.ic_logo)
                .into(binding.ivNewsThumbnail)
            binding.root.setOnClickListener {
                itemClickedListener(item)
            }
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<ItemNews>() {
        override fun areItemsTheSame(oldItem: ItemNews, newItem: ItemNews): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ItemNews, newItem: ItemNews): Boolean {
            return oldItem.link == newItem.link
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemNewsViewHolder {
        return ItemNewsViewHolder(ItemLargeNewsBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false), itemClickedListener)
    }

    override fun onBindViewHolder(holder: ItemNewsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}
