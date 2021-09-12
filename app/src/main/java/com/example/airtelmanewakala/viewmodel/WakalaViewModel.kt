package com.example.airtelmanewakala.viewmodel

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.airtelmanewakala.db.CSVWriter
import com.example.airtelmanewakala.db.MobileRepository
import com.example.airtelmanewakala.db.MoblieDatabase
import com.example.airtelmanewakala.db.Wakala
import com.example.airtelmanewakala.network.RetroInstance
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.romellfudi.ussdlibrary.USSDController.context
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileWriter


class WakalaViewModel (private val repository: MobileRepository): ViewModel(), Observable {

    val wakala=repository.wakala
    val modifiedAt = System.currentTimeMillis()

    fun updatewakala(tigopesa:String,wakalaid:String): Job =
        viewModelScope.launch {
            repository.updateWakala(tigopesa,wakalaid)
        }

    @Bindable
    val getButtonText=MutableLiveData<String>()

    init {
        getButtonText.value= "FETCH WAKALA"
    }


    fun insert(wakala: List<Wakala>): Job =
        viewModelScope.launch {
            repository.insertWakala(wakala)

        }

    fun wakalaSearch(text: String) = liveData {
        repository.searchViewWakala(text).collect {
            emit(it)
        }
    }


    fun wakala() = liveData {
        repository.wakala.collect {
            emit(it)
        }
    }


     fun onGetButton() {
        val service= RetroInstance.getRetroInstance()
        val retrofitData = service?.getDataWakala()

        if (retrofitData != null) {
            retrofitData.enqueue(object : Callback<List<Wakala>> {
                override fun onResponse(
                    call: Call<List<Wakala>>,
                    response: Response<List<Wakala>>
                ) {
                    val responseBody =response.body()
                    if (responseBody != null) {
                        insert(responseBody)
                    }

                    Log.d("WAKALAmy", "oKEY")
                }


                override fun onFailure(call: Call<List<Wakala>>, t: Throwable) {

                }
            })
        }
        else{
            Log.d("WAKALA", "omNull")
        }
    }



    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

}