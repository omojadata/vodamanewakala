package com.example.airtelmanewakala.viewmodel

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.airtelmanewakala.*
import com.example.airtelmanewakala.db.*
import com.google.android.material.chip.Chip
import com.romellfudi.ussdlibrary.USSDController
import com.romellfudi.ussdlibrary.USSDController.context
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class FloatOutViewModel ( private val repository: MobileRepository): ViewModel(), Observable  {

    @Bindable
    val allButton = MutableLiveData<String>()
    val zeroButton = MutableLiveData<String>()
    val oneButton = MutableLiveData<String>()
    val twoButton = MutableLiveData<String>()
    val threeButton = MutableLiveData<String>()
    val fourButton = MutableLiveData<String>()
    val uploadButton = MutableLiveData<String>()

    init {
        allButton.value= "A"
    }
    init {
        zeroButton.value= "Pending"
    }
    init {
        oneButton.value= "Ussd"
    }
    init {
        twoButton.value= "Done"
    }
    init {
        threeButton.value= "Invalid"
    }

    init {
        fourButton.value= "Change"
    }

    init {
        uploadButton.value= "UP"
    }

    var floatoutchange = StringBuilder()
    var mtandao = "+255714363727"
    val modifiedAt = System.currentTimeMillis()
    var mtandaoname = "AIRTEL"
    val contactnumber = "+255714363627"
    val errornumber = "+245683071757"
    @Bindable
    val getButtonText=MutableLiveData<String>()
    init {
        getButtonText.value= "FETCH FLOATOUT"
    }


    fun floatOutFilter(status: Int) = liveData {
        repository.floatOutFilter(status).collect {
            emit(it)
        }
    }
    fun floatOut() = liveData {
        repository.floatOut.collect {
            emit(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun floatOutUpdate(floatOut: FloatOut): Job =
        viewModelScope.launch {

            if (checkFloatOut(floatOut.networksms)) {

                //GET VARIBLES
                val (amount, name, balance, transid) = getFloatOut(floatOut.networksms)

                //CHECK IF TRANSACTION(transactionid) EXISTS
                val searchFloatOutDuplicate = repository.searchFloatOutDuplicate(transid)
                if (!searchFloatOutDuplicate) {

                    //BALANCE FUNTION
                    checkbalancefunction(balance, amount, name, 2, floatOut.createdAt ,floatOut.madeAt)

                     //CHECK IF WAKALA EXISTS
                    val searchWakala = repository.searchWakala(name)
                    if (searchWakala != null) {

                        // CHECK IF FLOATOUT WAKALA ORDER EXISTS (floatinid(sent bu wakala mkuu order)) EXISTS
                        val searchFloatOutWakalaOrder = repository.searchFloatOutWakalaOrder(name)
                        if (searchFloatOutWakalaOrder) {

                            //UPDATE FLOATOUT STATUS 2(DONE)
                            val wakalaKeyId = searchWakala.wakalaid

                            launch{
                                uFloatOut(
                                    2,
                                    amount,
                                    wakalaKeyId,
                                    transid,
                                    "DONE",
                                    floatOut.networksms,
                                    modifiedAt
                                )
                            }

                        } else {
                            launch {
                                //INSERT FLOATOUT STATUS 3(UNKWOWN ORDER)
                                uFloatOutChange(
                                    floatOut.floatoutid,
                                    transid,
                                    amount,
                                    name,
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    3,
                                    "UNKNOWN ORDER",
                                    "",
                                    modifiedAt
                                )

                            }
                        }
                    } else {
                        launch {

                            //INSERT FLOATOUT STATUS 4(UNKWOWN WAKALA)
                            uFloatOutChange(
                                floatOut.floatoutid,
                                transid,
                                amount,
                                name,
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                3,
                                "UNKWOWN WAKALA",
                                "",
                                modifiedAt
                            )

                        }
                    }
                } else {
                    launch {
                        uFloatOutChange(
                            floatOut.floatoutid,
                            transid,
                            amount,
                            name,
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            3,
                            "DUPLICATE SMS",
                            "",
                            modifiedAt
                        )
                    }

                }
            }
        }


    private fun uFloatOut(
        status: Int,
        amount: String,
        wakalaKeyId: String,
        transid: String,
        comment: String,
        networksms:String,
        modifiedAt: Long
    ) : Job =
        viewModelScope.launch {
        repository.updateFloatOut(
            status,
            amount,
            wakalaKeyId,
            transid,
            comment,
            networksms,
            modifiedAt
        )
    }

    fun uFloatOutChange(
        floatoutid: Int,
        transid: String,
        amount: String,
        wakalaname: String,
        wakalacode: String,
        network: String,
        wakalaidkey: String,
        wakalamkuu: String,
        fromfloatinid: String,
        fromtransid: String,
        status: Int,
        comment: String,
        wakalanumber: String,
        modifiedAt: Long,
    ) : Job =
        viewModelScope.launch {
            repository.updateFloatOutChange(
                floatoutid,
        transid,
        amount,
        wakalaname,
        wakalacode,
        network,
        wakalaidkey,
        wakalamkuu,
        fromfloatinid,
        fromtransid,
        status,
        comment,
        wakalanumber,
        modifiedAt,
            )
        }

    private fun checkbalancefunction(
        balance: String,
        amount: String,
        name: String,
        status: Int,
        createdAt: Long,
        madeAt: Long
    ) : Job = viewModelScope.launch {
            async {
                //INSERT BALANCE
                repository.insertBalance(
                    Balance(
                        0,
                        balance,
                        amount,
                        name,
                        status,
                        createdAt,
                        madeAt
                    )
                )
                //CHECK BALANCE
                var balanci = repository.getBalance().balance.toInt()
                if (balanci < 100000) {
                    //send balance error
                    //turn off error
                }
            }
        }

   fun dialUSSD(
        ussdCode: String,
        wakalacode:String,
        wakalaname:String,
        amount: String,
        modifiedAt: Long,
        fromfloatinid:String,
        fromtransid:String
    )  : Job =
       viewModelScope.launch {
        val map = HashMap<String, List<String>>()
        map["KEY_LOGIN"] = Arrays.asList("USSD code running...")
        map["KEY_ERROR"] = Arrays.asList("problema", "problem", "error", "null")

       val ussdApi = USSDController
       USSDController.callUSSDOverlayInvoke(
           context,
           ussdCode,
           map,
           object : USSDController.CallbackInvoke {
               override fun responseInvoke(message: String) {
                   // message has the response string data
                   ussdApi.send("1") {  // it: response response

                       ussdApi.send(wakalacode) {   // it: response message

                           ussdApi.send(amount) { message3 -> // it: response message

                               if (message3.contains(wakalaname)) {
                                     launch {
                                         repository.updateFloatOutUSSD(
                                             1,
                                             amount,
                                             fromfloatinid,
                                             fromtransid,
                                             "USSD",
                                             modifiedAt
                                         )
                                     }

                                   ussdApi.send("6499") {
                                   // it: response message
                                   }
                               }
                           }

                       }
                   }
               }

               override fun over(message: String) {

               }
           })
    }

   @RequiresApi(Build.VERSION_CODES.N)
   fun USSD(floatOut: FloatOut)  : Job =
        viewModelScope.launch {
            var balanci = repository?.getBalance()?.balance?.toInt()
            if (balanci != null) {
                if (balanci >= floatOut.amount.toInt()) {
                      dialUSSD(
                        "*150*01#",
                        floatOut.wakalacode,
                          floatOut.wakalaname,
                        floatOut.amount,
                        modifiedAt,
                        floatOut.fromfloatinid,
                          floatOut.fromtransid
                    )
                } else {
                    val smsText = "$fromnetwork SALIO = : CHINI CHA ${getComma("100000")}"
                    sendSms(errornumber, smsText)
                }
            }
        }


    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }
}


