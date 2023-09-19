package com.kiliccambaz.expenseapp.ui.login

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.auth.User
import com.kiliccambaz.expenseapp.R
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.data.UserRole
import com.kiliccambaz.expenseapp.databinding.FragmentLoginBinding
import com.kiliccambaz.expenseapp.ui.admin.AdminActivity
import com.kiliccambaz.expenseapp.utils.ValidationUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var loginViewModel: LoginViewModel
    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (email.isEmpty()) {
                binding.etEmailInputLayout.error = getString(R.string.email_or_username_validation)
            } else if (email.contains("@") && !ValidationUtils.isEmailValid(email)) {
                binding.etEmailInputLayout.error = getString(R.string.email_validation_error)
            } else {
                binding.etEmailInputLayout.error = null
                val password = binding.etPassword.text.toString()
                if (password.isEmpty()) {
                    binding.etPasswordInputLayout.error = getString(R.string.password_validation)
                } else {
                    binding.etPasswordInputLayout.error = null
                    val loadingDialog = showLoadingDialog(requireContext())
                    loginViewModel.signInWithEmailAndPassword(email, password) { result ->
                        loadingDialog.dismiss()
                        when (result) {
                            is Result.Success -> {
                                when (result.data) {
                                    UserRole.EMPLOYEE.value -> {
                                        val action = LoginFragmentDirections.actionFragmentLoginToExpenseListFragment()
                                        findNavController().navigate(action)
                                    }
                                    UserRole.MANAGER.value -> {
                                        val action = LoginFragmentDirections.actionFragmentLoginToWaitingExpensesFragment()
                                        findNavController().navigate(action)
                                    }
                                    UserRole.ACCOUNTANT.value -> {
                                        val action = LoginFragmentDirections.actionFragmentLoginToApprovedExpenseListFragment()
                                        findNavController().navigate(action)
                                    }
                                    UserRole.ADMIN.value -> {
                                        val intent = Intent(requireContext(), AdminActivity::class.java)
                                        startActivity(intent)
                                        requireActivity().finish()
                                    }
                                }
                            }
                            is Result.Error -> {
                                GlobalScope.launch(Dispatchers.Main) {
                                    val errorMessage = result.message
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        }

        binding.tvRegister.setOnClickListener {
            val action = LoginFragmentDirections.actionFragmentLoginToRegisterFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    private fun showLoadingDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)

        builder.setView(dialogView)
        builder.setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.show()

        return alertDialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}