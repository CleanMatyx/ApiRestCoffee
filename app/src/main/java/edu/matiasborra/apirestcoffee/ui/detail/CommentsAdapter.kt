package edu.matiasborra.apirestcoffee.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.matiasborra.apirestcoffee.databinding.CommentItemBinding
import edu.matiasborra.apirestcoffee.model.comment.CommentItem

/**
 * Adaptador para manejar la lista de comentarios en un RecyclerView.
 * @author Matias Borra
 */
class CommentsAdapter : ListAdapter<CommentItem, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    /**
     * Crea una nueva vista para un elemento de la lista.
     * @param parent Vista padre que contiene los elementos de la lista.
     * @param viewType Tipo de vista (no utilizado en este caso).
     * @return CommentViewHolder con la nueva vista creada.
     * @author Matias Borra
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = CommentItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    /**
     * Vincula los datos del comentario a la vista del ViewHolder.
     * @param holder ViewHolder que contiene la vista.
     * @param position Posición del elemento en la lista.
     * @author Matias Borra
     */
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder que contiene la vista de cada elemento de comentario.
     * @param binding Enlace al layout del elemento de comentario.
     * @author Matias Borra
     */
    class CommentViewHolder(private val binding: CommentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        /**
         * Vincula los datos del comentario a los elementos de la vista.
         * @param comment Datos del comentario que se van a mostrar.
         * @author Matias Borra
         */
        fun bind(comment: CommentItem) {
            binding.tvCommentText.text = comment.comment
            binding.tvUserName.text = comment.user
        }
    }

    /**
     * Callback para calcular las diferencias entre dos listas de elementos de comentario.
     * @author Matias Borra
     */
    class CommentDiffCallback : DiffUtil.ItemCallback<CommentItem>() {
        /**
         * Comprueba si dos elementos de comentario son los mismos comparando sus IDs.
         * @param oldItem Elemento antiguo de la lista.
         * @param newItem Elemento nuevo de la lista.
         * @return true si los elementos son los mismos, false en caso contrario.
         * @author Matias Borra
         */
        override fun areItemsTheSame(oldItem: CommentItem, newItem: CommentItem): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Comprueba si el contenido de dos elementos de comentario es el mismo.
         * @param oldItem Elemento antiguo de la lista.
         * @param newItem Elemento nuevo de la lista.
         * @return true si el contenido es el mismo, false en caso contrario.
         * @author Matias Borra
         */
        override fun areContentsTheSame(oldItem: CommentItem, newItem: CommentItem): Boolean {
            return oldItem == newItem
        }
    }
}