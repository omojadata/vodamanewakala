package com.example.vodamanewakala

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.vodamanewakala.db.*
import com.romellfudi.ussdlibrary.USSDController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.util.*
import kotlin.collections.HashMap


class ForegroundSmsService : Service() {
    lateinit var smsreceiver: SmsBroadcastReceiver
    lateinit var receiver: BroadcastReceiver
    private val scope = CoroutineScope(Dispatchers.Main)
    private val TAG = "MyService"

    private val CHANNELID = "ForegroundService Kotlin"
//    private lateinit var floatInViewModel: FloatInViewModel
//    val dao = MoblieDatabase.getInstance(application).MobileDAO
//    val repository = MobileRepository(dao)

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNELID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNELID)
            .setContentTitle("MANE WAKALA IS ACTIVE")
            .setContentText("AIRTEL")
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        val smsAddress = intent?.getStringExtra("smsadress").toString()
        val smsbody = intent?.getStringExtra("smsbody").toString()
        val smsTime = intent?.getStringExtra("smstime").toString()

        val firstword = filterBody(smsbody, 1)
        val lastword = smsbody.substring(smsbody.lastIndexOf(" ") + 1)

        val floatinWord = "Umepokea"
        val floatoutWord = "Umetuma"

        val contactnumber = "+255714363623"

        val createdAt = System.currentTimeMillis()
        val modifiedAt = System.currentTimeMillis()
        val madeAt = smsTime.toLong()

        //do heavy work on a background thread
        scope.launch {

            val dao = MoblieDatabase.getInstance(application).MobileDAO
            val repository = MobileRepository(dao)
            val dataStorePreference = DataStorePreference(application)

            //CHECK IF SMS IF FROM AIRTELMONEY
            if (smsAddress == mtandao) {

                //CHECK IF FIRST WORD IS "UMEPOKEA"
                if (firstword == floatinWord) {

                    if (checkFloatIn(smsbody)) {

                        //GET VARIBLES
                        val (amount, name, balance, transid) = getFloatIn(smsbody)

                        //CHECK IF TRANSACTION EXISTS
                        val searchFloatInDuplicate = repository.searchFloatInDuplicate(transid)
                        if (searchFloatInDuplicate) {

                            //BALANCE FUNTION
                            checkbalancefunction(balance, amount, name, 1, createdAt,madeAt,repository)

                            //CHECK IF WAKALA EXISTS
                            val searchWakala = repository.searchWakala(name)

                            if (searchWakala != null) {

                                val timeDiff = createdAt - madeAt

                                val wakalaKeyId = searchWakala.wakalaid
                                val wakalacontact = searchWakala.contact
                                val fromwakalaname = searchWakala.airtelname
                                val fromwakalacode = searchWakala.airtelmoney
                                val maxamount = searchWakala.maxamount

                                val currentamount = amount.toInt()
                                val maxAmount = searchWakala.maxamount.toInt()

                                if (isTodayDate(madeAt)) {

                                    //CHECK IF FLOAT IN AMOUNT LESS OR EQUAL TO MAX AMOUNT
                                    if (currentamount <= maxAmount) {

                                        //INSERT FLOATIN STATUS 0( WAITING ORDER)
                                        launch {
                                            iFloatIn(
                                                transid,
                                                amount,
                                                maxamount,
                                                balance,
                                                wakalaKeyId,
                                                0,
                                                "WAITING ORDER",
                                                fromwakalacode,
                                                fromwakalaname,
                                                wakalacontact,
                                                smsbody,
                                                createdAt,
                                                modifiedAt,
                                                madeAt,
                                                repository
                                            )

                                            sendBroadcast(Intent().setAction("floatInReceiver"))

                                            val amounting = getComma(amount)
                                            val timeM= getTime(madeAt)
                                            val timeC= getTime(createdAt)
                                            var smsText =
                                                "Muamala No: ${transid}, Kiasi: Tsh $amounting, Muda uliotuma: $timeM, Muda ulioingia: $timeC, Mtandao: $fromnetwork itumwe wapi? Jibu Tigopesa, Mpesa au Halopesa"
                                            sendSms(wakalacontact, smsText)

                                        }
                                    } else {
                                        launch {
                                            //INSERT FLOATIN STATUS 2(LARGE ORDER)
                                            iFloatIn(
                                                transid,
                                                amount,
                                                maxamount,
                                                balance,
                                                wakalaKeyId,
                                                2,
                                                "LARGE/WAIT",
                                                fromwakalacode,
                                                fromwakalaname,
                                                wakalacontact,
                                                smsbody,
                                                createdAt,
                                                modifiedAt,
                                                madeAt,
                                                repository
                                            )

                                            sendBroadcast(Intent().setAction("floatInReceiver"))

                                        }
                                    }
                                } else {

                                    //INSERT FLOATIN STATUS 4(LATE ORDER)....SMS IMECHELIWA BY 300000 seconds
                                    launch {
                                        iFloatIn(
                                            transid,
                                            amount,
                                            maxamount,
                                            balance,
                                            wakalaKeyId,
                                            4,
                                            "LATE ORDER",
                                            fromwakalacode,
                                            fromwakalaname,
                                            wakalacontact,
                                            smsbody,
                                            createdAt,
                                            modifiedAt,
                                            madeAt,
                                            repository
                                        )

                                        sendBroadcast(Intent().setAction("floatInReceiver"))
                                    }
                                }
                            } else {
                                //INSERT FLOATIN STATUS 3(UNKWOWN WAKALA)..SIO WAKALA WETU

                                launch {
                                    iFloatIn(
                                        transid,
                                        amount,
                                        "",
                                        balance,
                                        "",
                                        3,
                                        "UNKNOWN WAKALA",
                                        "",
                                        "",
                                        "",
                                        smsbody,
                                        createdAt,
                                        modifiedAt,
                                        madeAt,
                                        repository
                                    )

                                    sendBroadcast(Intent().setAction("floatInReceiver"))
//
//                                    //SEND ERROR TEXT TO ERROR NUMBER IF FLOATIN UNKNOWN WAKALA INSERTED
//                                    var smsText =
//                                        "$fromnetwork ERROR = UNKWOWN WAKALA: FLOATIN: $smsbody"
//                                    sendSms(errornumber, smsText)
                                }
                            }
                        } else {
                            //UPDATE FLOAT IN TRANSACTION ALREADY EXISTS
                            launch {
                                iFloatIn(
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    3,
                                    "DUPLICATE SMS",
                                    "",
                                    "",
                                    "",
                                    smsbody,
                                    createdAt,
                                    modifiedAt,
                                    madeAt,
                                    repository
                                )
                                sendBroadcast(Intent().setAction("floatInReceiver"))
                            }
                        }
                    } else {

                        val float = floatinchange.toString()
                        launch {
                            iFloatIn(
                                "",
                                "",
                                "",
                                "",
                                "",
                                5,
                                "CHANGES IN $float",
                                "",
                                "",
                                "",
                                smsbody,
                                createdAt,
                                modifiedAt,
                                madeAt,
                                repository
                            )

                            sendBroadcast(Intent().setAction("floatInReceiver"))

                            val float = floatinchange.toString()
                            val sendText = "$fromnetwork ERROR = CHANGES: FLOATIN: $smsbody -Changes in $float"

                            sendSms(errornumber, sendText)
                        }
                    }
                } else if (firstword == floatoutWord) {

                    if (checkFloatOut(smsbody)) {

                        //GET VARIBLES
                        val (amount, name, balance, transid) = getFloatOut(smsbody)

                        //CHECK IF TRANSACTION(transactionid) EXISTS
                        val searchFloatOutDuplicate = repository.searchFloatOutDuplicate(transid)
                        if (searchFloatOutDuplicate) {

                            //BALANCE FUNTION
                            checkbalancefunction(balance, amount, name, 2, createdAt,madeAt,repository)

                            //CHECK IF WAKALA EXISTS
                            val searchWakala = repository.searchWakala(name)
                            if (searchWakala != null) {

                                // CHECK IF FLOATOUT WAKALA ORDER EXISTS (floatinid(sent bu wakala mkuu order)) EXISTS
                                val searchFloatOutWakalaOrder =
                                    repository.searchFloatOutWakalaOrder(name)
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
                                            smsbody,
                                            modifiedAt,
                                            repository
                                        )
                                        sendBroadcast(Intent().setAction("floatOutReceiver"))
                                    }
                                } else {
                                    launch {
                                        //INSERT FLOATOUT STATUS 3(UNKWOWN ORDER)
                                        iFloatOut(
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
                                            smsbody,
                                            createdAt,
                                            modifiedAt,
                                            madeAt,
                                            repository
                                        )

                                        sendBroadcast(Intent().setAction("floatOutReceiver"))
                                    }
                                }
                            } else {
                                launch {

                                    //INSERT FLOATOUT STATUS 4(UNKWOWN WAKALA)
                                    iFloatOut(
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
                                        smsbody,
                                        createdAt,
                                        modifiedAt,
                                        madeAt,
                                        repository
                                    )

                                    sendBroadcast(Intent().setAction("floatOutReceiver"))
                                }
                            }
                        } else {
                            launch {
                                iFloatOut(
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    3,
                                    "DUPLICATE SMS",
                                    "",
                                    smsbody,
                                    createdAt,
                                    modifiedAt,
                                    madeAt,
                                    repository
                                )

                                sendBroadcast(Intent().setAction("floatOutReceiver"))
                            }

                        }
                    } else {
                        val float = floatoutchange.toString()
                        launch {

                            iFloatOut(
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                4,
                                "CHANGES IN $float",
                                "",
                                smsbody,
                                createdAt,
                                modifiedAt,
                                madeAt,
                                repository
                            )

                            sendBroadcast(Intent().setAction("floatOutReceiver"))
//                            val float = floatinchange.toString()
                            val sendText = "$fromnetwork ERROR = CHANGES: FLOATOUT: $smsbody -Changes in $float"

                            sendSms(errornumber, sendText)
                        }
                    }
                }
            } else {
                if (firstword == "WAKALAMKUU" && lastword=="WAKALAMKUU" ) {
                    // "WAKALAMKUU $firstword $amount $towakalacode $[towakalaname] $fromfloatinid $fromtransid $wakalano $fromnetwork $wakalakeyid WAKALAMKUU"
                    val firstW = filterBody(smsbody, 2)
                    val amount = filterBody(smsbody, 3)
                    val wakalacode = filterBody(smsbody, 4)

                    val namedata = smsbody.substringAfter("[")
                    val wakalaname = namedata.substringBefore("]")

                    val smsbody2= smsbody.substringAfter("] ")
                    val fromfloatinid = filterBody(smsbody2, 1)
                    val fromtransid = filterBody(smsbody2, 2)
                    val wakalano = filterBody(smsbody2, 3)
                    val fromnetwork = filterBody(smsbody2, 4)
                    val wakalakeyid = filterBody(smsbody2, 5)

//                    val floatinstatus = filterBody(smsbody, 9)
                    val phone = filterNumber(smsAddress)
                    Log.i("floatout","${firstW+amount+wakalacode+fromnetwork+wakalakeyid+wakalaname+fromtransid+fromfloatinid+wakalano+phone}")
                    //CHECK IF TRANSACTION EXISTS
                    val searchFloatOutWakalaMkuuOrderDuplicate =
                        repository.searchFloatOutWakalaMkuuOrderDuplicate(
                            fromfloatinid,
                            fromtransid
                        )
                    Log.i("123456", searchFloatOutWakalaMkuuOrderDuplicate.toString()+fromfloatinid+fromtransid)
                    if (searchFloatOutWakalaMkuuOrderDuplicate) {
                        Log.i("floatout","${firstW}")
                        //CHECK IF WAKALA MKUU EXISTS AND GET ID
                        val searchWakalaMkuu = when (firstW) {
                            "Tigopesa" -> repository.searchWakalaMkuuTigo(phone).wakalamkuuid
                            "Mpesa" -> repository.searchWakalaMkuuVoda(phone).wakalamkuuid
                            "Halopesa" -> repository.searchWakalaMkuuHalotel(phone).wakalamkuuid
                            else -> ""
                        }

                        if (!searchWakalaMkuu.isNullOrBlank()) {
                            Log.i("floatout","${firstW}")
                            //CHECK IF WAKALA EXISTS AND GET CODE
                            Log.i("floatout","${firstW+wakalacode+wakalakeyid}")
                            val searchWakalaCode = when (firstW) {
                                "Tigopesa" -> repository.searchWakalaTigo(
                                    wakalacode,
                                    wakalakeyid
                                ).tigopesa
                                "Mpesa" -> repository.searchWakalaVoda(
                                    wakalacode,
                                    wakalakeyid
                                ).mpesa
                                "Halopesa" -> repository.searchWakalaHalotel(
                                    wakalacode,
                                    wakalakeyid
                                ).halopesa
                                else -> ""
                            }
                            //CHECK IF WAKALA EXISTS AND GET NAME
                            val searchWakalaName = when (firstW) {
                                "Tigopesa" -> repository.searchWakalaTigo(
                                    wakalacode,
                                    wakalakeyid
                                ).tigoname
                                "Mpesa" -> repository.searchWakalaVoda(
                                    wakalacode,
                                    wakalakeyid
                                ).vodaname
                                "Halopesa" -> repository.searchWakalaHalotel(
                                    wakalacode,
                                    wakalakeyid
                                ).haloname
                                else -> ""
                            }

                            if (searchWakalaCode!=null && searchWakalaName!=null) {
                                //INSERT FLOATOUT STATUS 0(PENDING)
                                launch {
                                    iFloatOut(
                                        "",
                                        amount,
                                        wakalaname,
                                        wakalacode,
                                        fromnetwork,
                                        "",
                                        searchWakalaMkuu,
                                        fromfloatinid,
                                        fromtransid,
                                        0,
                                        "PENDING",
                                        wakalano,
                                        "",
                                        createdAt,
                                        modifiedAt,
                                        madeAt,
                                        repository
                                    )
                                    sendBroadcast(Intent().setAction("floatOutReceiver"))
                                    //CHECK BALANCE
                                    val balanci = repository.getBalance().balance.toInt()
                                    if (balanci >= amount.toInt()) {
                                        // CHECK IF AUTO ON
                                        val checkAuto = dataStorePreference.autoMode.first()
                                        if (checkAuto) {
                                            //DAILL USSD
                                            dialUssd(
                                                "*150*01#",
                                                wakalacode,
                                                wakalaname,
                                                amount,
                                                modifiedAt,
                                                fromfloatinid,
                                                fromtransid,
                                                repository
                                            )
                                        }
                                    } else {
                                        val SmsText = "HAMNA SALIO BALANCE NI:{$balanci}"
                                        sendSms(errornumber, SmsText)
                                    }
                                }
                            }
                        }
                    }

                } else if (firstword == "Tigopesa" || firstword == "Mpesa" || firstword == "Halopesa") {

                    val phone = filterNumber(smsAddress)

                    val wakalaorder =firstword
                    //CHECK IF WAKALACONTACT EXISTS AND GET WAKALA
                    val searchWakala = repository.searchWakalaContact(phone)
                    if (searchWakala != null) {

                        //CHECK IF THERE IS UNSERVED FLOATIN ORDER(check if wakala id exists and madeAt is greater than 12 hrs
                        val searchFloatInOrder =
                            repository.searchFloatInOrder(searchWakala.wakalaid)
                        if (searchFloatInOrder != null) {

                            val wakalamkuunumber = when (firstword) {
                                "Tigopesa" -> repository.getWakalaMkuu().tigopesa
                                "Mpesa" -> repository.getWakalaMkuu().mpesa
                                "Halopesa" -> repository.getWakalaMkuu().halopesa
                                else -> ""
                            }

                            val towakalacode = when (firstword) {
                                "Tigopesa" -> searchWakala.tigopesa
                                "Mpesa" -> searchWakala.mpesa
                                "Halopesa" -> searchWakala.halopesa
                                else -> ""
                            }

                            val towakalaname = when (firstword) {
                                "Tigopesa" -> searchWakala.tigoname
                                "Mpesa" -> searchWakala.vodaname
                                "Halopesa" -> searchWakala.haloname
                                else -> ""
                            }

                            val wakalano = searchFloatInOrder.wakalacontact
                            val fromfloatinid = searchFloatInOrder.floatinid
                            val amount = searchFloatInOrder.amount
                            val fromtransid = searchFloatInOrder.transid
                            val wakalaidkey = searchFloatInOrder.wakalaidkey

                            if (searchFloatInOrder.status == 0) {

                                //UPDATE FLOATIN WITH ORDER(STATUS 1= ODERSENT)
                                launch {
                                    repository.updateFloatIn(
                                        1,
                                        fromfloatinid,
                                        wakalaorder,
                                        "PENDING->DONE",
                                        towakalacode,
                                        wakalamkuunumber,
                                        towakalaname,
                                        modifiedAt
                                    )

                                    sendBroadcast(Intent().setAction("floatInReceiver"))
                                    //"WAKALAMKUU $wakalaorder $amount $towakalacode $towakalaname $fromfloatinid $fromtransid $wakalano $fromnetwork wakalaidkey WAKALAMKUU"


                                    val smsText = "WAKALAMKUU $wakalaorder $amount $towakalacode [$towakalaname] $fromfloatinid $fromtransid $wakalano $fromnetwork $wakalaidkey WAKALAMKUU"
                                    sendSms(wakalamkuunumber, smsText)
                                }
                            }else if(searchFloatInOrder.status == 2 && searchFloatInOrder.comment == "LARGE"){
                                //UPDATE FLOATIN WITH ORDER(STATUS 1= ODERSENT)
                                launch {
                                    repository.updateFloatIn(
                                        1,
                                        fromfloatinid,
                                        wakalaorder,
                                        "LARGE->DONE",
                                        towakalacode,
                                        wakalamkuunumber,
                                        towakalaname,
                                        modifiedAt
                                    )

                                    sendBroadcast(Intent().setAction("floatInReceiver"))

                                    val smsText = "WAKALAMKUU $wakalaorder $amount $towakalacode [$towakalaname] $fromfloatinid $fromtransid $wakalano $fromnetwork $wakalaidkey WAKALAMKUU"
                                    sendSms(wakalamkuunumber, smsText)
                                }
                            }
                        }
                    }
                }
// (firstword == "USSD") {
//                    dialUssd(
//                        "*150*01#",
//                        "wakalacode",
//                        "wakalaname",
//                        "amount",
//                        modifiedAt,
//                        "fromfloatinid",
//                        "fromtransid",
//                        repository
//                    )
//                }
            }
        }
        stopForeground(true)
        stopSelf()
        return START_NOT_STICKY;
    }


    private suspend fun checkbalancefunction(
        balance: String,
        amount: String,
        name: String,
        status: Int,
        createdAt: Long,
        madeAt: Long,
        repository: MobileRepository
    ) {
        scope.launch {
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

            sendBroadcast(Intent().setAction("balanceReceiver"))

            //CHECK BALANCE
            val balancecheck = if(repository.getBalance()==null) 0 else repository.getBalance().balance.toInt();
            if (balancecheck > 100000) {
                val smsText = "$fromnetwork SALIO = : CHINI CHA ${getComma("100000")}"

                sendSms(errornumber, smsText)
            }
        }
    }

    private suspend fun iFloatIn(
        transid: String,
        amount: String,
        maxamount: String,
        balance: String,
        wakalaidkey: String,
        status: Int,
        comment: String,
        fromwakalacode: String,
        fromwakalaname: String,
        wakalacontact: String,
        networksms: String,
        createdAt: Long,
        modifiedAt: Long,
        madeAt: Long,
        repository: MobileRepository
    ) {
        repository.insertFloatIn(
            FloatIn(
                0,
                transid,
                amount,
                maxamount,
                balance,
                wakalaidkey,
                status,
                fromnetwork,
                "",
                comment,
                fromwakalacode,
                "",
                "",
                fromwakalaname,
                "",
                wakalacontact,
                networksms,
                createdAt,
                modifiedAt,
                madeAt
            )
        )
    }

    private suspend fun iFloatOut(
        transid: String,
        amount: String,
        name: String,
        code: String,
        network: String,
        wakalaKeyId: String,
        wakalamkuu: String,
        fromfloatinid: String,
        fromtransid: String,
        status: Int,
        comment: String,
        wakalanumber: String,
        networksms: String,
        createdAt: Long,
        modifiedAt: Long,
        madeAt: Long,
        repository: MobileRepository
    ) {
        repository.insertFloatOut(
            FloatOut(
                0,
                transid,
                amount,
                name,
                code,
                network,
                wakalaKeyId,
                wakalamkuu,
                fromfloatinid,
                fromtransid,
                status,
                comment,
                wakalanumber,
                networksms,
                createdAt,
                modifiedAt,
                madeAt
            )
        )
    }

    private suspend fun uFloatOut(
        status: Int,
        amount: String,
        wakalaKeyId: String,
        transid: String,
        comment: String,
        networksms: String,
        modifiedAt: Long,
        repository: MobileRepository
    ) {
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

    private fun dialUssd(
        ussdCode: String,
        wakalacode: String,
        wakalaname: String,
        amount: String,
        modifiedAt: Long,
        fromfloatinid: String,
        fromtransid: String,
        repository: MobileRepository,
    ) {
        val map = HashMap<String, List<String>>()
        map["KEY_LOGIN"] = Arrays.asList("USSD code running...")
        map["KEY_ERROR"] = Arrays.asList("problema", "problem", "error", "null")
        val ussdApi = USSDController
        USSDController.callUSSDOverlayInvoke(
            this,
            ussdCode,
            map,
            object : USSDController.CallbackInvoke {
                override fun responseInvoke(message: String) {
                    // message has the response string data
                    ussdApi.send("1") {
                        ussdApi.send(wakalacode) {
                            ussdApi.send(amount) { message3 ->
                                if (message3.contains(wakalaname)) {
                                    ussdApi.send("6499") {
                                        scope.launch {
                                            repository.updateFloatOutUSSD(
                                                1,
                                                amount,
                                                fromfloatinid,
                                                fromtransid,
                                                "USSD",
                                                modifiedAt
                                            )
                                        }
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
}

