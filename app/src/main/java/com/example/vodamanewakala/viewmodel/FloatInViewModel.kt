package com.example.vodamanewakala.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.*
import com.example.vodamanewakala.*
import com.example.vodamanewakala.db.Balance
import com.example.vodamanewakala.db.FloatIn
import com.example.vodamanewakala.db.MobileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FloatInViewModel(private val repository: MobileRepository) : ViewModel(), Observable {


    @Bindable
    val allButton = MutableLiveData<String>()
    val zeroButton = MutableLiveData<String>()
    val oneButton = MutableLiveData<String>()
    val twoButton = MutableLiveData<String>()
    val threeButton = MutableLiveData<String>()
    val fourButton = MutableLiveData<String>()
    val fiveButton = MutableLiveData<String>()
    val uploadButton = MutableLiveData<String>()

    init {
        allButton.value = "All"
    }

    init {
        zeroButton.value = "Pending"
    }

    init {
        oneButton.value = "Done"
    }

    init {
        twoButton.value = "Large"
    }

    init {
        threeButton.value = "Invalid"
    }

    init {
        fourButton.value = "Late"
    }

    init {
        fiveButton.value = "Change"
    }

    init {
        uploadButton.value = "UPL"
    }

    @Bindable
    val getButtonText = MutableLiveData<String>()

    init {
        getButtonText.value = "FETCH FLOATIN"
    }

    val modifiedAt = System.currentTimeMillis()


    fun floatInFilter(status: Int) = liveData {
        repository.floatInFilter(status).collect {
            emit(it)
        }
    }

    fun floatIn() = liveData {
        repository.floatIn.collect {
            emit(it)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun floatInUpdate(floatIn: FloatIn): Job =
        viewModelScope.launch {
            if (checkFloatIn(floatIn.networksms)) {

                //GET VARIBLES
                val (amount, name, balance, transid) = getFloatIn(floatIn.networksms)

                //CHECK IF TRANSACTION EXISTS
                val searchFloatInDuplicate = repository.searchFloatInDuplicate(transid)
                if (!searchFloatInDuplicate) {

                    //BALANCE FUNTION
                    checkbalancefunction(
                        balance,
                        amount,
                        name,
                        1,
                        floatIn.createdAt,
                        floatIn.madeAt
                    )

                    //CHECK IF WAKALA EXISTS
                    val searchWakala = repository.searchWakala(name)

                    if (searchWakala != null) {
                        val timeDiff = floatIn.createdAt - floatIn.madeAt

                        val wakalaKeyId = searchWakala.wakalaid
                        val wakalacontact = searchWakala.contact
                        val fromwakalaname = searchWakala.airtelname
                        val fromwakalacode = searchWakala.airtelmoney

                        val currentamount = amount.toInt()

                        val maxamount = searchWakala.maxamount
                        val maxAmount = maxamount.toInt()
                        if (isTodayDate(floatIn.madeAt)) {
                            //CHECK IF FLOAT IN AMOUNT LESS OR EQUAL TO MAX AMOUNT
                            if (currentamount <= maxAmount) {

                                //INSERT FLOATIN STATUS 0( WAITING ORDER)
                                launch {

                                    uFloatInChange(
                                        floatIn.floatinid,
                                        transid,
                                        amount,
                                        maxamount,
                                        balance,
                                        wakalaKeyId,
                                        0,
                                        "",
                                        "WAITING ORDER",
                                        fromwakalacode,
                                        "",
                                        "",
                                        fromwakalaname,
                                        "",
                                        wakalacontact,
                                        modifiedAt,
                                    )

                                    val amounting=getComma(amount)
                                    val timeM= getTime(floatIn.madeAt)
                                    val timeC= getTime(floatIn.createdAt)

                                    var smsText =
                                        "Muamala No: ${transid}, Kiasi: Tsh $amounting, Muda uliotuma: $timeM, Muda ulioingia: $timeC, Mtandao: $fromnetwork itumwe wapi? Jibu Tigopesa, Mpesa au Halopesa"
                                    sendSms(wakalacontact, smsText)
                                }
                            } else {
                                launch {

                                    uFloatInChange(
                                        floatIn.floatinid,
                                        transid,
                                        amount,
                                        maxamount,
                                        balance,
                                        wakalaKeyId,
                                        2,
                                        "",
                                        "LARGE/WAIT",
                                        fromwakalacode,
                                        "",
                                        "",
                                        fromwakalaname,
                                        "",
                                        wakalacontact,
                                        modifiedAt,
                                    )
                                }
                            }
                        } else {

                            //INSERT FLOATIN STATUS 4(LATE ORDER)....SMS IMECHELIWA BY 300000 seconds/5 min
                            launch {
                                uFloatInChange(
                                    floatIn.floatinid,
                                    transid,
                                    amount,
                                    maxamount,
                                    balance,
                                    wakalaKeyId,
                                    4,
                                    "",
                                    "LATE ORDER",
                                    fromwakalacode,
                                    "",
                                    "",
                                    fromwakalaname,
                                    "",
                                    wakalacontact,
                                    modifiedAt,
                                )

                            }
                        }
                    } else {
                        //INSERT FLOATIN STATUS 3(UNKWOWN WAKALA)..SIO WAKALA WETU

                        launch {
                            uFloatInChange(
                                floatIn.floatinid,
                                transid,
                                amount,
                                "",
                                balance,
                                "",
                                3,
                                "",
                                "UNKNOWN WAKALA",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                modifiedAt,
                            )

                            //SEND ERROR TEXT TO ERROR NUMBER IF FLOATIN UNKNOWN WAKALA INSERTED
                            var smsText =
                                "$fromnetwork ERROR = UNKWOWN WAKALA: ${floatIn.networksms}"

                            sendSms(errornumber, smsText)
                        }
                    }
                } else {
                    //UPDATE FLOAT IN TRANSACTION ALREADY EXISTS

                    uFloatInChange(
                        floatIn.floatinid,
                        transid,
                        amount,
                        "",
                        balance,
                        "",
                        3,
                        "",
                        "DUPLICATE SMS",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        modifiedAt,
                    )
                }
            } else {
                val float = floatinchange.toString()
                val smsText = "$fromnetwork ERROR = CHANGES: ${floatIn.networksms} -Changes in $float"

                sendSms(errornumber, smsText)
            }
        }


    private fun uFloatInChange(
        floatinid: Int,
        transid: String,
        amount: String,
        maxamount: String,
        balance: String,
        wakalaidkey: String,
        status: Int,
        wakalaorder: String,
        comment: String,
        fromwakalacode: String,
        towakalacode: String,
        wakalamkuunumber: String,
        fromwakalaname: String,
        towakalaname: String,
        wakalacontact: String,
        modifiedAt: Long,
    ): Job =
        viewModelScope.launch {
            repository.updateFloatInChange(
                floatinid,
                transid,
                amount,
                maxamount,
                balance,
                wakalaidkey,
                status,
                fromnetwork,
                wakalaorder,
                comment,
                fromwakalacode,
                towakalacode,
                wakalamkuunumber,
                fromwakalaname,
                towakalaname,
                wakalacontact,
                modifiedAt,
            )
        }

    fun uFloatInLarge(
        floatinid: Int,
        comment: String,
        modifiedat: Long
    ): Job =
        viewModelScope.launch {
            repository.updateFloatInLarge(
                floatinid,
                comment,
                modifiedat,
            )
        }




    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun checkbalancefunction(
        balance: String,
        amount: String,
        name: String,
        status: Int,
        createdAt: Long,
        madeAt: Long
    ) {
        viewModelScope.launch {

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
                    if (repository.getBalance() == null) 0 else repository.getBalance().balance.toInt();
                if (balancecheck > 100000) {
                    val smsText = "$fromnetwork SALIO = : CHINI CHA ${getComma("100000")}"

                    sendSms(errornumber, smsText)
                    //send balance error
                    //turn off error
                }


        }
    }


    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {

    }
}