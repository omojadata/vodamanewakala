package com.example.airtelmanewakala.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.airtelmanewakala.db.MobileRepository

class WakalaViewModelFactory(private val repository: MobileRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?>create(modelClass: Class<T>):T{
        if(modelClass.isAssignableFrom(WakalaViewModel::class.java)){
            return  WakalaViewModel(repository) as T
        }
        throw  IllegalArgumentException("Unknown View Model Class")
    }
}

