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

/**
 * Adaptador para manejar la lista de elementos de café en un RecyclerView.
 * @param onClickCoffeeItem Función que se ejecuta al hacer clic en un elemento de la lista,
 * recibiendo el ID del café.
 * @author Matias Borra
 */
class CoffeeAdapter(
    val onClickCoffeeItem: (idCoffee: Int) -> Unit
): ListAdapter<CoffeeItem, CoffeeAdapter.ViewHolder>(DiffCoffeesCallback()) {

    /**
     * ViewHolder que contiene la vista de cada elemento de café.
     * @param view Vista del elemento de la lista.
     * @author Matias Borra
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = CoffeeItemBinding.bind(view)

        /**
         * Vincula los datos del café a los elementos de la vista.
         * @param coffee Datos del café que se van a mostrar.
         * @author Matias Borra
         */
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

    /**
     * Crea una nueva vista para un elemento de la lista.
     * @param parent Vista padre que contiene los elementos de la lista.
     * @param viewType Tipo de vista (no utilizado en este caso).
     * @return ViewHolder con la nueva vista creada.
     * @author Matias Borra
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CoffeeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ).root
        )
    }

    /**
     * Vincula los datos del café a la vista del ViewHolder.
     * @param holder ViewHolder que contiene la vista.
     * @param position Posición del elemento en la lista.
     * @author Matias Borra
     */
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }
}

/**
 * Callback para calcular las diferencias entre dos listas de elementos de café.
 * @author Matias Borra
 */
class DiffCoffeesCallback : DiffUtil.ItemCallback<CoffeeItem>() {
    /**
     * Comprueba si dos elementos de café son los mismos comparando sus IDs y nombres.
     * @param oldItem Elemento antiguo de la lista.
     * @param newItem Elemento nuevo de la lista.
     * @return true si los elementos son los mismos, false en caso contrario.
     * @author Matias Borra
     */
    override fun areItemsTheSame(oldItem: CoffeeItem, newItem: CoffeeItem): Boolean {
        return oldItem.id == newItem.id
                && oldItem.coffeeName == newItem.coffeeName
    }

    /**
     * Comprueba si el contenido de dos elementos de café es el mismo.
     * @param oldItem Elemento antiguo de la lista.
     * @param newItem Elemento nuevo de la lista.
     * @return true si el contenido es el mismo, false en caso contrario.
     * @author Matias Borra
     */
    override fun areContentsTheSame(oldItem: CoffeeItem, newItem: CoffeeItem): Boolean {
        return oldItem == newItem
    }
}