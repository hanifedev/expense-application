package com.kiliccambaz.expenseapp.ui.register

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.kiliccambaz.expenseapp.databinding.FragmentRegisterBinding
import com.kiliccambaz.expenseapp.data.Result
import com.kiliccambaz.expenseapp.utils.ValidationHelper

class RegisterFragment : Fragment() {

    private lateinit var registerViewModel: RegisterViewModel
    private var binding: FragmentRegisterBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater)
        binding!!.btnRegister.setOnClickListener {
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
                    val confirmPassword = binding!!.etConfirmPassword.text.toString()
                    if (confirmPassword.isEmpty()) {
                        binding!!.etConfirmPassword.error = "Parola alanı boş olamaz"
                    } else {
                        binding!!.etPasswordInputLayout.error = null
                        if(password == confirmPassword) {
                            registerViewModel.registerWithEmailAndPassword(email, password) { result ->
                                when (result) {
                                    is Result.Success -> {
                                        Toast.makeText(context, "Başarıyla kayıt olundu", Toast.LENGTH_LONG).show()
                                        requireActivity().supportFragmentManager.popBackStack()
                                    }
                                    is Result.Error -> {
                                        val errorMessage = result.message
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Parolalar eşleşmiyor", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
        return binding!!.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        registerViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
    }

}