package com.kiliccambaz.expenseapp.ui.login

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.data.UserRole
import com.kiliccambaz.expenseapp.databinding.FragmentLoginBinding
import com.kiliccambaz.expenseapp.ui.employee.ExpenseListFragment
import com.kiliccambaz.expenseapp.ui.register.RegisterFragment
import com.kiliccambaz.expenseapp.utils.ValidationHelper

class LoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private var binding: FragmentLoginBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        binding!!.btnSignIn.setOnClickListener {
            val email = binding!!.etEmail.text.toString()
            if (email.isEmpty()) {
                binding!!.etEmailInputLayout.error = "E-posta alanı boş olamaz"
            } else if (!ValidationHelper.isEmailValid(email)) {
                binding!!.etEmailInputLayout.error = "Geçerli bir e-posta giriniz"
            } else {
                binding!!.etEmailInputLayout.error = null
                val password = binding!!.etPassword.text.toString()
                if (password.isEmpty()) {
                    binding!!.etPasswordInputLayout.error = "Parola alanı boş olamaz"
                } else {
                    binding!!.etPasswordInputLayout.error = null
                    loginViewModel.signInWithEmailAndPassword(email, password) { result ->
                        when (result) {
                            is Result.Success -> {
                                val userRole = UserRole.valueOf(result.data)
                                when (userRole) {
                                    UserRole.Employee -> {
                                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                        transaction.replace(R.id.fragment_container, ExpenseListFragment())
                                        transaction.addToBackStack(null)
                                        transaction.commit()
                                    }
                                    UserRole.Manager -> {
                                        // Yöneticinin yapabileceği işlemler
                                    }
                                    UserRole.Accountant -> {

                                    }
                                    UserRole.Admin -> {
                                        // Adminin yapabileceği işlemler
                                    }
                                }
                            }
                            is Result.Error -> {
                                val errorMessage = result.message
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }

        binding!!.tvRegister.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, RegisterFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return binding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
    }

}