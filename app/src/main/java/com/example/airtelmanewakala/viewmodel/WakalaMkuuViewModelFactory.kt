package com.example.airtelmanewakala.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.airtelmanewakala.db.MobileRepository

class WakalaMkuuViewModelFactory(private val repository: MobileRepository,private val application: Application):ViewModelProvider.Factory {
    override fun <T : ViewModel?>create(modelClass: Class<T>):T{
        if(modelClass.isAssignableFrom(WakalaMkuuViewModel::class.java)){
            return  WakalaMkuuViewModel(repository,application) as T
        }
        throw  IllegalArgumentException("Unknown View Model Class")
    }
}