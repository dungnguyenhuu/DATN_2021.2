package vn.app.qrcode.ui.home

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import vn.app.qrcode.R
import vn.app.qrcode.databinding.ItemCategoryNewsBinding

class CategoryAdapter: ListAdapter<CategoryNews, CategoryAdapter.CategoryViewHolder>(DiffCallback) {

    class CategoryViewHolder( private val binding: ItemCategoryNewsBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryNews) {
            binding.tvCategoryName.text = item.name
            Glide.with(binding.ivCategoryLogo)
                .load(item.imageUri)
                .placeholder(R.drawable.ic_logo)
                .into(binding.ivCategoryLogo)
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<CategoryNews>() {
        override fun areItemsTheSame(oldItem: CategoryNews, newItem: CategoryNews): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: CategoryNews, newItem: CategoryNews): Boolean {
            return oldItem.name == newItem.name
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(ItemCategoryNewsBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))
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