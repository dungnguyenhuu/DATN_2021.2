package vn.app.qrcode.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import vn.app.qrcode.R
import vn.app.qrcode.data.model.MediaItemData
import vn.app.qrcode.databinding.ItemCategoryNewsBinding

class CategoryAdapter(
    private val itemClickedListener: (MediaItemData) -> Unit
): ListAdapter<MediaItemData, CategoryAdapter.CategoryViewHolder>(MediaItemData.diffCallback) {

    class CategoryViewHolder(
        private val binding: ItemCategoryNewsBinding,
        private val itemClickedListener: (MediaItemData) -> Unit
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MediaItemData) {
            binding.tvCategoryName.text = item.title
            Glide.with(binding.ivCategoryLogo)
                .load(item.albumArtUri)
                .placeholder(R.drawable.ic_logo)
                .into(binding.ivCategoryLogo)
            binding.root.setOnClickListener {
                itemClickedListener(item)
            }
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(ItemCategoryNewsBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false), itemClickedListener)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}



data class CategoryNews(
    val name: String,
    val imageUri: Uri
)