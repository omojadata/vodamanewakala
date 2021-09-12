package com.example.airtelmanewakala.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.airtelmanewakala.db.MobileRepository

class FloatInViewModelFactory (private val repository: MobileRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel?>create(modelClass: Class<T>):T{
        if(modelClass.isAssignableFrom(FloatInViewModel::class.java)){
            return  FloatInViewModel(repository) as T
        }
        throw  IllegalArgumentException("Unknown View Model Class")
    }
}