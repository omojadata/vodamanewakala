package com.example.vodamanewakala.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.vodamanewakala.*
import com.example.vodamanewakala.db.*
import com.romellfudi.ussdlibrary.USSDController
import com.romellfudi.ussdlibrary.USSDController.context
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*


class FloatOutViewModel(private val repository: MobileRepository) : ViewModel(), Observable {

    @Bindable
    val allButton = MutableLiveData<String>()
    val zeroButton = MutableLiveData<String>()
    val oneButton = MutableLiveData<String>()
    val twoButton = MutableLiveData<String>()
    val threeButton = MutableLiveData<String>()
    val fourButton = MutableLiveData<String>()
    val uploadButton = MutableLiveData<String>()

    init {
        allButton.value = "A"
    }

    init {
        zeroButton.value = "Pending"
    }

    init {
        oneButton.value = "Ussd"
    }

    init {
        twoButton.value = "Done"
    }

    init {
        threeButton.value = "Invalid"
    }

    init {
        fourButton.value = "Change"
    }

    init {
        uploadButton.value = "UP"
    }

    var floatoutchange = StringBuilder()
    var mtandao = "+255714363727"
    val modifiedAt = System.currentTimeMillis()
    var mtandaoname = "AIRTEL"
    val contactnumber = "+255714363627"
    val errornumber = "+245683071757"

    @Bindable
    val getButtonText = MutableLiveData<String>()

    init {
        getButtonText.value = "FETCH FLOATOUT"
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
                if (searchFloatOutDuplicate) {

                    //BALANCE FUNTION
                    checkbalancefunction(
                        balance,
                        amount,
                        name,
                        2,
                        floatOut.createdat,
                        floatOut.madeatfloat
                    )

                    //CHECK IF WAKALA EXISTS
                    val searchWakala = repository.searchWakala(name)
                    if (searchWakala != null) {

                        // CHECK IF FLOATOUT WAKALA ORDER EXISTS (floatinid(sent bu wakala mkuu order)) EXISTS
                        val searchFloatOutWakalaOrder = repository.searchFloatOutWakalaOrder(name)
                        if (searchFloatOutWakalaOrder) {

                            //UPDATE FLOATOUT STATUS 2(DONE)
                            val wakalaKeyId = searchWakala.wakalaid
                            launch {
                                uFloatOut(
                                    2,
                                    amount,
                                    wakalaKeyId,
                                    transid,
                                    "DONE",
                                    floatOut.networksms,
                                    modifiedAt,
                                    floatOut.madeatfloat
                                )

                                dFloatOut(
                                    1,
                                    modifiedAt,
                                    floatOut.floatoutid
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

    private fun dFloatOut(
        deletestatus: Int,
        modifiedat:Long,
        floatoutid:Int
    ): Job =
        viewModelScope.launch {
            repository.deleteFloatOutChange(
                deletestatus,
                modifiedat,
                floatoutid
            )
        }

    private fun uFloatOut(
        status: Int,
        amount: String,
        wakalaKeyId: String,
        transid: String,
        comment: String,
        networksms: String,
        modifiedat: Long,
        madeatfloat: Long
    ): Job =
        viewModelScope.launch {
            repository.updateFloatOut(
                status,
                amount,
                wakalaKeyId,
                transid,
                comment,
                networksms,
                modifiedat,
                madeatfloat
            )
        }

    private fun uFloatOutChange(
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
    ): Job =
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkbalancefunction(
        balance: String,
        amount: String,
        name: String,
        status: Int,
        createdAt: Long,
        madeAt: Long
    ): Job = viewModelScope.launch {
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
            val balancecheck =
                if (repository.getBalance() == null) 0 else repository.getBalance()[0].toInt();
            if (balancecheck > 100000) {
                val smsText = "$fromnetwork SALIO = : CHINI CHA ${getComma("100000")}"
                sendSms(com.example.vodamanewakala.errornumber, smsText)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun USSD(floatOut: FloatOut): Job =
        viewModelScope.launch {
            var balanci = repository?.getBalance()[0].toInt()
            if (balanci != null) {
                if (balanci >= floatOut.amount.toInt()) {
                    dialUssd(
                        "*150*00#",
                        floatOut.wakalacode,
                        floatOut.wakalaname,
                        floatOut.amount,
                        modifiedAt,
                        floatOut.fromfloatinid,
                        floatOut.fromtransid,
                        repository,
                        context,
                        viewModelScope
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


