package edu.matiasborra.apirestcoffee.ui.detail

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.matiasborra.apirestcoffee.auth.dataStore
import edu.matiasborra.apirestcoffee.R
import edu.matiasborra.apirestcoffee.auth.SessionManager
import edu.matiasborra.apirestcoffee.data.RemoteDataSource
import edu.matiasborra.apirestcoffee.data.Repository
import edu.matiasborra.apirestcoffee.databinding.ActivityDetailCoffeeBinding
import edu.matiasborra.apirestcoffee.model.comment.CommentItem
import edu.matiasborra.apirestcoffee.ui.main.MainActivity
import edu.matiasborra.apirestcoffee.utils.checkConnection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DetailCoffeeActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    private val vm: DetailCoffeeViewModel by viewModels {
        val ds = RemoteDataSource()
        val sessionManager = SessionManager(dataStore)
        val idCoffee = intent.getIntExtra(COFFEE_ID, -1)
        DetailCoffeeViewModelFactory(
            Repository(sessionManager, ds),
            idCoffee
        )
    }

    private lateinit var binding: ActivityDetailCoffeeBinding

    companion object {
        const val COFFEE_ID = "COFFEE_ID"
        fun navigate(activity: AppCompatActivity, idCoffee: Int = -1) {
            val intent = Intent(activity, DetailCoffeeActivity::class.java).apply {
                putExtra("COFFEE_ID", idCoffee)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(
                intent,
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailCoffeeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(dataStore)

        binding.btnSend.setOnClickListener {
            val review = binding.edtComment.text.toString()
            if (review.isNotEmpty()) {
                sendReview(review)
            } else {
                binding.edtComment.error = getString(R.string.txt_empty_field)
            }
        }

        binding.mToolbar.navigationIcon =
            AppCompatResources.getDrawable(this, R.drawable.ic_arrow_back)
        binding.mToolbar.setNavigationOnClickListener {
            finishAfterTransition()
        }

        val commentsAdapter = CommentsAdapter()
        binding.rvComments.adapter = commentsAdapter
        binding.rvComments.layoutManager = LinearLayoutManager(this)

        if (checkConnection(this)) {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        vm.coffee.collect { coffee ->
                            binding.tvCoffeeName.text = coffee?.coffeeName
                            binding.mToolbar.title = coffee?.coffeeName
                            binding.tvDescription.text = if (!coffee?.coffeeDesc.isNullOrEmpty()) {
                                Html.fromHtml(coffee.coffeeDesc, Html.FROM_HTML_MODE_LEGACY)
                            } else {
                                ""
                            }
                        }
                    }

                    launch {
                        vm.comments.collect { comments ->
                            Log.d("Comments", "Cantidad de comentarios: ${comments.size}")
                            Log.d("Comments", "Contenido: $comments")
                            commentsAdapter.submitList(comments)
                            binding.rvComments.scrollToPosition(commentsAdapter.itemCount - 1)
                        }
                    }
                }
            }
        }
    }

    private fun sendReview(review: String) {
        lifecycleScope.launch {
            try {
                val coffeeId = vm.coffee.value?.id ?: return@launch
                val (token, username) = sessionManager.sessionFlow.first()

                // Verificar que el token exista y sea válido
                if (token.isNullOrEmpty()) {
                    // Mostrar mensaje y redirigir a login
                    mostrarErrorAutenticacion()
                    return@launch
                }

                if (username != null) {
                    val comment = CommentItem(
                        comment = review,
                        id = 0,
                        idCoffee = coffeeId,
                        user = username
                    )
                    try {
                        vm.addComment(token, comment)
                        binding.edtComment.text?.clear()
                        vm.refreshComments(token)
                    } catch (e: retrofit2.HttpException) {
                        if (e.code() == 401) {
                            mostrarErrorAutenticacion()
                        } else {
                            mostrarError("Error: ${e.message()}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("DetailCoffeeActivity", "Error al enviar comentario: ${e.message}")
                mostrarError("No se pudo enviar el comentario")
            }
        }
    }

    private fun mostrarErrorAutenticacion() {
        // Mostrar mensaje de sesión expirada
        Toast.makeText(this, "Sesión expirada. Por favor inicia sesión nuevamente", Toast.LENGTH_LONG).show()

        // Redirigir a MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}