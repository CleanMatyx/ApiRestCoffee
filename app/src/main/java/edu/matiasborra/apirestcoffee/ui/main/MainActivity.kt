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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dialogLogin: AlertDialog

    private val vm: MainViewModel by viewModels {
        val ds = RemoteDataSource()
        MainViewModelFactory(Repository(SessionManager(dataStore), ds))
    }

    private val adapter by lazy {
        CoffeeAdapter(
            onClickCoffeeItem = { idCoffee ->
                Log.d("ApiRestCoffee", "Click en el elemento $idCoffee")
                DetailCoffeeActivity.navigate(this@MainActivity, idCoffee)
            }
        )
    }

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

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
        binding.mRecycler.setHasFixedSize(true)
        binding.mRecycler.adapter = adapter

        checkSession()
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                // Obtener el token actualizado de la sesión
                vm.getSessionFlow().collect { sessionData ->
                    sessionData.first?.let { token ->
                        fetchCoffees(token)
                        return@collect
                    } ?: run {
                        // Si no hay token válido, mostrar mensaje y diálogo de login
                        Toast.makeText(this@MainActivity, R.string.txt_no_session, Toast.LENGTH_SHORT).show()
                        binding.swipeRefresh.isRefreshing = false
                        dialogLogin.show()
                    }
                }
            }
        }

        // Resto del código para la toolbar...

        binding.mToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.otp_login -> {
                    adapter.submitList(emptyList())
                    vm.logout()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            try {
                vm.loginState.collect { state ->
                    when (state) {
                        is LoginState.Idle -> Log.i("ApiRestCoffee", "Idle")
                        is LoginState.Loading -> Log.i("ApiRestCoffee", "Cargando...")
                        is LoginState.Success -> {
                            Log.i("ApiRestCoffee", "Login exitoso: ${state.response.username}")
                            Log.i("ApiRestCoffee", "Login exitoso: ${state.response.token}")
                            dialogLogin.dismiss()
                            // Cargar cafés después del login exitoso
                            state.response.token?.let { fetchCoffees(it) }
                        }
                        is LoginState.Error -> {
                            Log.e("ApiRestCoffee", "Error de login: ${state.message}")
                            Toast.makeText(this@MainActivity, R.string.txt_error_auth, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ApiRestCoffee", "Error al cargar datos: ${e.message}")
                createBindDialog()
            }
        }
    }

    private fun checkSession() {
        lifecycleScope.launch {
            vm.getSessionFlow().collect { sessionData ->
                if (sessionData.first != null) {
                    // Si hay una sesión, usar ese token para cargar cafés
                    sessionData.first?.let { fetchCoffees(it) }
                } else {
                    // Mostrar diálogo de login si no hay sesión
                    dialogLogin.show()
                    Log.d("ApiRestCoffee", "No hay sesión activa")
                }
            }
        }
    }

    private fun fetchCoffees(token: String) {
        lifecycleScope.launch {
            try {
                vm.fetchCoffees(token).collect { coffees ->
                    adapter.submitList(coffees)
                    binding.swipeRefresh.isRefreshing = false
                }
            } catch (e: Exception) {
                binding.swipeRefresh.isRefreshing = false

                // Verificar si el error es por token expirado
                val unauthorizedMessage = getString(R.string.txt_message_unauthorized)
                if (e.message?.contains("401") == true || e.message?.contains(unauthorizedMessage) == true) {
                    Log.e("ApiRestCoffee", "Token expirado: ${e.message}")
                    Toast.makeText(this@MainActivity, R.string.txt_session_expired, Toast.LENGTH_SHORT).show()

                    // Cerrar sesión actual y mostrar diálogo de login
                    vm.logout()
                    dialogLogin.show()
                } else {
                    // Otros errores
                    Log.e("ApiRestCoffee", "Error al cargar cafés: ${e.message}")
                    checkSession()
                    Toast.makeText(this@MainActivity, R.string.txt_error_coffe, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
}