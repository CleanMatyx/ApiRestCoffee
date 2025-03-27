package edu.matiasborra.apirestcoffee.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.matiasborra.apirestcoffee.R
import edu.matiasborra.apirestcoffee.databinding.CoffeeItemBinding
import edu.matiasborra.apirestcoffee.model.coffee.CoffeeItem

class CoffeeAdapter(
    val onClickCoffeeItem: (idCoffee: Int) -> Unit
): ListAdapter<CoffeeItem, CoffeeAdapter.ViewHolder>(DiffCoffeesCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = CoffeeItemBinding.bind(view)
        fun bind(coffee: CoffeeItem) {
            binding.tvCoffeeName.text = coffee.coffeeName
            if(coffee.comments > 0) {
                binding.tvComments.text = coffee.comments.toString()
                binding.tvComments.visibility = View.VISIBLE
            } else {
                binding.tvComments.text = R.string.no_comments.toString()
                binding.tvComments.visibility = View.GONE
            }
            itemView.setOnClickListener {
                onClickCoffeeItem(coffee.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CoffeeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).root
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }
}

class DiffCoffeesCallback : DiffUtil.ItemCallback<CoffeeItem>() {
    override fun areItemsTheSame(oldItem: CoffeeItem, newItem: CoffeeItem): Boolean {
        return oldItem.id == newItem.id
                && oldItem.coffeeName == newItem.coffeeName
    }

    override fun areContentsTheSame(oldItem: CoffeeItem, newItem: CoffeeItem): Boolean {
        return oldItem == newItem
    }
}