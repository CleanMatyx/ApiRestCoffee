package edu.matiasborra.apirestcoffee.ui.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.inputmethod.InputMethodManager
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

/**
 * Actividad para mostrar los detalles de un café específico.
 * @property sessionManager Administrador de sesión para manejar la autenticación.
 * @property vm ViewModel para manejar la lógica de la pantalla de detalles del café.
 * @property binding Enlace al layout de la actividad.
 * @author Matias Borra
 */
class DetailCoffeeActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    private val vm: DetailCoffeeViewModel by lazy {
        val ds = RemoteDataSource()
        val sessionManager = SessionManager(dataStore)
        val idCoffee = intent.getIntExtra(COFFEE_ID, -1)
        viewModels<DetailCoffeeViewModel> {
            DetailCoffeeViewModelFactory(
                Repository(sessionManager, ds),
                idCoffee
            )
        }.value
    }

    private lateinit var binding: ActivityDetailCoffeeBinding

    companion object {
        const val COFFEE_ID = "COFFEE_ID"

        /**
         * Navega a la actividad de detalles del café.
         * @param activity Actividad desde la cual se navega.
         * @param idCoffee ID del café a mostrar.
         * @author Matias Borra
         */
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

    /**
     * Función que se llama cuando se crea la actividad.
     * @param savedInstanceState Estado guardado de la actividad.
     * @author Matias Borra
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailCoffeeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
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
                            commentsAdapter.submitList(comments)
                            binding.rvComments.scrollToPosition(commentsAdapter.itemCount - 1)
                        }
                    }
                }
            }
        }
    }

    /**
     * Envía una reseña del café.
     * @param review Reseña a enviar.
     * @author Matias Borra
     */
    private fun sendReview(review: String) {
        lifecycleScope.launch {
            try {
                val coffeeId = vm.coffee.value?.id ?: return@launch
                val (token, username) = sessionManager.sessionFlow.first()
                /**
                 * Si el token es nulo o vacío, muestra un mensaje de error de autenticación.
                 */
                if (token.isNullOrEmpty()) {
                    showErrorAutenticacion()
                    return@launch
                }

                /**
                 * Si el nombre de usuario no es nulo, añade el comentario.
                 */
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
                        hideKeyboard()
                        vm.refreshComments(token)
                    } catch (e: retrofit2.HttpException) {
                        if (e.code() == 401) {
                            showErrorAutenticacion()
                        } else {
                            showError(getString(R.string.txt_error_401))
                        }
                    }
                }
            } catch (e: Exception) {
                showError(getString(R.string.txt_error_send_review))
            }
        }
    }

    /**
     * Oculta el teclado. Si no hay un teclado visible, no hace nada.
     * @author Matias Borra
     */
    @SuppressLint("ServiceCast")
    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**
     * Muestra un mensaje de error de autenticación.
     * Redirige al usuario a la pantalla de inicio de sesión.
     * @author Matias Borra
     */
    private fun showErrorAutenticacion() {
        Toast.makeText(this, getString(R.string.txt_error_401), Toast.LENGTH_LONG).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Muestra un mensaje de error.
     * @param mensaje Mensaje de error a mostrar.
     * @author Matias Borra
     */
    private fun showError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}