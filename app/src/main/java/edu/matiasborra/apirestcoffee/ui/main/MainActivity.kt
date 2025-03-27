package edu.matiasborra.apirestcoffee.ui.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import edu.matiasborra.apirestcoffee.R
import edu.matiasborra.apirestcoffee.auth.LoginState
import edu.matiasborra.apirestcoffee.auth.SessionManager
import edu.matiasborra.apirestcoffee.auth.dataStore
import edu.matiasborra.apirestcoffee.data.RemoteDataSource
import edu.matiasborra.apirestcoffee.data.Repository
import edu.matiasborra.apirestcoffee.databinding.ActivityMainBinding
import edu.matiasborra.apirestcoffee.databinding.DialogLayoutBinding
import edu.matiasborra.apirestcoffee.ui.detail.DetailCoffeeActivity
import kotlinx.coroutines.launch

/**
 * Actividad principal de la aplicación. En esta actividad se muestra la lista de cafés.
 * Si el usuario no ha iniciado sesión, se muestra un diálogo para que lo haga.
 * Una vez iniciada la sesión, se muestra la lista de cafés.
 * @property binding Layout de la actividad.
 * @property dialogLogin Diálogo para iniciar sesión.
 * @author Matias Borra
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dialogLogin: AlertDialog

    /**
     * ViewModel de la actividad. Se encarga de manejar la lógica de la pantalla principal.
     * @author Matias Borra
     */
    private val vm: MainViewModel by viewModels {
        val ds = RemoteDataSource()
        MainViewModelFactory(Repository(SessionManager(dataStore), ds))
    }

    /**
     * Adaptador para la lista de cafés. Con esto se añade un click listener para cada café.
     * Es 'lazy' para que se inicialice solo cuando se necesite.
     * @author Matias Borra
     */
    private val adapter by lazy {
        CoffeeAdapter(
            onClickCoffeeItem = { idCoffee ->
                DetailCoffeeActivity.navigate(this@MainActivity, idCoffee)
            }
        )
    }

    /**
     * Función que se ejecuta al crear la actividad. Se inicializan los elementos de la actividad
     * y se configuran los observadores. Además, se comprueba si el usuario ha iniciado sesión.
     * @param savedInstanceState Estado de la actividad.
     * @author Matias Borra
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        createBindDialog()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /**
         * Bloquea la rotación de la pantalla para que no cambie de orientación.
         * Solo se puede ver la aplicación en modo vertical.
         */
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        binding.mRecycler.setHasFixedSize(true)
        binding.mRecycler.adapter = adapter

        checkSession()
        setupObservers()
    }

    /**
     * Función que se ejecuta al iniciar la actividad. Se configuran los listeners de la actividad.
     * Aquí se configura el listener del menú de la barra superior y el listener del swipe refresh.
     * @author Matias Borra
     */
    override fun onStart() {
        super.onStart()
        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                vm.getSessionFlow().collect { sessionData ->
                    sessionData.first?.let { token ->
                        fetchCoffees(token)
                        return@collect
                    } ?: run {
                        Toast.makeText(this@MainActivity, R.string.txt_no_session, Toast.LENGTH_SHORT).show()
                        binding.swipeRefresh.isRefreshing = false
                        dialogLogin.show()
                    }
                }
            }
        }

        binding.mToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.otp_login -> {
                    adapter.submitList(emptyList())
                    vm.logout()
                    true
                }
                R.id.opt_about_us -> {
                    showAboutDialog()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Función que se ejecuta al reanudar la actividad. Se configura el observador de la sesión.
     * Si el usuario ha iniciado sesión, se obtiene la lista de cafés.
     * Si no ha iniciado sesión, se muestra el diálogo de inicio de sesión.
     * @author Matias Borra
     */
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            vm.getSessionFlow().collect { sessionData ->
                sessionData.first?.let { token ->
                    fetchCoffees(token)
                } ?: run {
                    Toast.makeText(this@MainActivity, R.string.txt_no_session, Toast.LENGTH_SHORT).show()
                    dialogLogin.show()
                }
            }
        }
    }

    /**
     * Observadores de la actividad. Se encargan de manejar los estados de la sesión y de la lista de cafés.
     * Si la sesión está en estado de carga, se muestra un mensaje de carga. Si la sesión es correcta,
     * se obtiene la lista de cafés. Si hay un error, se muestra un mensaje de error.
     * Si la lista de cafés está en estado de carga, se muestra un mensaje de carga. Si la lista de cafés
     * es correcta, se muestra la lista de cafés. Si hay un error, se muestra un mensaje de error.
     * @author Matias Borra
     */
    private fun setupObservers() {
        lifecycleScope.launch {
            try {
                vm.loginState.collect { state ->
                    when (state) {
                        is LoginState.Idle -> Log.i("ApiRestCoffee", "Idle")
                        is LoginState.Loading -> Log.i("ApiRestCoffee", "Cargando...")
                        is LoginState.Success -> {
                            dialogLogin.dismiss()
                            state.response.token?.let { fetchCoffees(it) }
                        }
                        is LoginState.Error -> {
                            Toast.makeText(this@MainActivity, R.string.txt_error_auth, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                createBindDialog()
            }
        }
    }

    /**
     * Comprueba si hay una sesión activa. Si la hay, se obtiene la lista de cafés.
     * Si no hay sesión, se muestra el diálogo de inicio de sesión.
     * @author Matias Borra
     */
    private fun checkSession() {
        lifecycleScope.launch {
            vm.getSessionFlow().collect { sessionData ->
                if (sessionData.first != null) {
                    sessionData.first?.let { fetchCoffees(it) }
                } else {
                    dialogLogin.show()
                }
            }
        }
    }

    /**
     * Obtiene la lista de cafés. Si hay un error, se muestra un mensaje de error.
     * @param token Token de autenticación.
     * @throws Exception Si hay un error al obtener la lista de cafés.
     * @author Matias Borra
     */
    private fun fetchCoffees(token: String) {
        lifecycleScope.launch {
            try {
                vm.fetchCoffees(token).collect { coffees ->
                    adapter.submitList(coffees)
                    binding.swipeRefresh.isRefreshing = false
                }
            } catch (e: Exception) {
                binding.swipeRefresh.isRefreshing = false

                val unauthorizedMessage = getString(R.string.txt_message_unauthorized)
                if (e.message?.contains("401") == true || e.message?.contains(unauthorizedMessage) == true) {
                    Toast.makeText(this@MainActivity, R.string.txt_session_expired, Toast.LENGTH_SHORT).show()

                    vm.logout()
                    dialogLogin.show()
                } else {
                    checkSession()
                    Toast.makeText(this@MainActivity, R.string.txt_error_coffe, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Crea el diálogo para iniciar sesión. Si el usuario no introduce un nombre de usuario,
     * se muestra un mensaje de error. Si introduce un nombre de usuario, se inicia sesión.
     * Si hay un error al iniciar sesión, se muestra un mensaje de error.
     * Si el usuario cancela el inicio de sesión, se muestra un mensaje de cancelación y se
     * cierra la aplicación.
     * @throws Exception Si hay un error al iniciar sesión.
     * @author Matias Borra
     */
    private fun createBindDialog() {
        val bindingCustom = DialogLayoutBinding.inflate(layoutInflater)
        dialogLogin = AlertDialog.Builder(this).apply {
            setView(bindingCustom.root)
            setCancelable(false)
            setPositiveButton(android.R.string.ok, null)
            setNegativeButton(android.R.string.cancel) { dialog, _ ->
                Toast.makeText(context, R.string.txt_cancel_login, Toast.LENGTH_SHORT).show()
                finish()
            }
        }.create()

        dialogLogin.setOnShowListener {
            dialogLogin.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if(bindingCustom.etUsername.text.isNullOrBlank()) {
                    bindingCustom.textInputUsername.error = getString(R.string.warning_username)
                } else {
                    bindingCustom.textInputUsername.error = ""
                    val username = bindingCustom.etUsername.text.toString()
                    val password = bindingCustom.etPassword.text.toString()
                    vm.login(username, password)
                }
            }
        }
    }

    /**
     * Muestra un diálogo con información sobre la aplicación.
     * @author Matias Borra
     */
    private fun MainActivity.showAboutDialog() {
        val dialog = AlertDialog.Builder(this).apply {
            setTitle(R.string.txt_about_us)
            setMessage(R.string.txt_about_message)
            setPositiveButton(android.R.string.ok, null)
        }.create()

        dialog.show()
    }
}


