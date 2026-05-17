package com.example.eventbookingplatform.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.eventbookingplatform.R
import com.example.eventbookingplatform.databinding.ItemCategoryChipBinding

data class CategoryChipItem(
    val id: String,
    val label: String,
)

class CategoryChipAdapter(
    private val onChipSelected: (CategoryChipItem) -> Unit,
) : RecyclerView.Adapter<CategoryChipAdapter.ViewHolder>() {

    private var chips: List<CategoryChipItem> = emptyList()
    private var selectedId: String = HomeFilters.CATEGORY_ALL

    fun submitChips(chips: List<CategoryChipItem>, selectedId: String) {
        this.chips = chips
        this.selectedId = selectedId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCategoryChipBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chips[position])
    }

    override fun getItemCount(): Int = chips.size

    inner class ViewHolder(
        private val binding: ItemCategoryChipBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chip: CategoryChipItem) {
            val context = binding.root.context
            val selected = chip.id == selectedId
            binding.textChip.text = chip.label
            binding.textChip.setBackgroundResource(
                if (selected) {
                    R.drawable.bg_category_chip_selected
                } else {
                    R.drawable.bg_category_chip_unselected
                },
            )
            binding.textChip.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (selected) R.color.on_primary else R.color.text_primary,
                ),
            )
            binding.root.setOnClickListener {
                if (chip.id != selectedId) {
                    onChipSelected(chip)
                }
            }
        }
    }
}
