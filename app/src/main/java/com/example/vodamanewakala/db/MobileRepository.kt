package com.example.vodamanewakala.db

import kotlinx.coroutines.flow.Flow

class MobileRepository(private val dao: MobileDAO) {

    //FLOATIN
    val floatIn = dao.floatIn()

    fun floatInFilter(status: Int): Flow<List<FloatIn>> {
        return dao.floatInFilter(status)
    }

    suspend fun insertFloatIn(floatin: FloatIn) {
        return dao.insertFloatIn(floatin)
    }

    suspend fun updateFloatInChange(
        floatinid: Int,
        transid: String,
        amount: String,
        maxamount: String,
        balance: String,
        wakalaidkey: String,
        status: Int,
        fromnetwork: String,
        wakalaorder: String,
        comment: String,
        fromwakalacode: String,
        towakalacode: String,
        wakalamkuunumber: String,
        fromwakalaname: String,
        towakalaname: String,
        wakalacontact: String,
        modifiedAt: Long
    ): Int {
        return dao.updateFloatInChange(
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
            modifiedAt
        )
    }

    suspend fun updateFloatIn(
        status: Int,
        floatinid: Int,
        wakalaorder: String,
        comment: String,
        towakalacode: String,
        wakalamkuunumber: String,
        towakalaname: String,
        modifiedat: Long
    ): Int {
        return dao.updateFloatIn(
            status,
            floatinid,
            wakalaorder,
            comment,
            towakalacode,
            wakalamkuunumber,
            towakalaname,
            modifiedat
        )
    }

     suspend fun updateFloatInLarge(
         floatinid: Int,
        comment: String,
        modifiedat: Long
    ): Int {
        return dao.updateFloatInLarge(
            floatinid,
            comment,
            modifiedat
        )
    }

    suspend fun searchFloatInDuplicate(transid: String): Boolean {
        return dao.searchFloatInDuplicate(transid)
    }

    suspend fun searchFloatInOrder(wakalaid: String): FloatIn {
        return dao.searchFloatInOrder(wakalaid)
    }


    //FLOATOUT
    val floatOut = dao.floatOut()

    fun floatOutFilter(status: Int): Flow<List<FloatOut>> {
        return dao.floatOutFilter(status)
    }

    suspend fun insertFloatOut(floatout: FloatOut) {
        return dao.insertFloatOut(floatout)
    }

    suspend fun updateFloatOutChange(
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
        modifiedAt: Long
    ): Int {
        return dao.updateFloatOutChange(
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
            modifiedAt
        )
    }

    suspend fun updateFloatOut(
        status: Int,
        amount: String,
        wakalaidkey: String,
        fromfloatinid: String,
        comment: String,
        networksms: String,
        modifiedat: Long
    ) {
        return dao.updateFloatOut(
            status,
            amount,
            wakalaidkey,
            fromfloatinid,
            comment,
            networksms,
            modifiedat
        )
    }

    suspend fun updateFloatOutUSSD(
        status: Int,
        amount: String,
        fromfloatinid: String,
        fromtransid: String,
        comment: String,
        modifiedat: Long
    ) {
        return dao.updateFloatOutUSSD(
            status,
            amount,
            fromfloatinid,
            fromtransid,
            comment,
            modifiedat
        )
    }

    suspend fun searchFloatOutDuplicate(transid: String): Boolean {
        return dao.searchFloatOutDuplicate(transid)
    }

    suspend fun searchFloatOutWakalaOrder(wakalaname: String): Boolean {
        return dao.searchFloatOutWakalaOrder(wakalaname)
    }

    suspend fun searchFloatOutWakalaMkuuOrderDuplicate(
        fromfloatinid: String,
        fromtransid: String
    ): Boolean {
        return dao.searchFloatOutWakalaMkuuOrderDuplicate(fromfloatinid, fromtransid)
    }


    //BALANCE
    val balance = dao.balance()

    suspend fun insertBalance(balance: Balance) {
        dao.insertBalance(balance)
    }

    suspend fun getBalance(): Balance {
        return dao.getBalance()
    }


    //WAKALAMKUU
    val wakalaMkuu = dao.WakalaMkuu()

    suspend fun insertWakalaMkuu(wakalaMkuu: List<WakalaMkuu>) {
        dao.insertWakalaMkuu(wakalaMkuu)
    }
//    @Query("SELECT * FROM wakalamkuu_table WHERE tigopesa = :columnvalue AND status = 1 LIMIT 1")
//    suspend fun searchWakalaMkuuTigo( columnvalue:String):WakalaMkuu

//    suspend fun searchWakalaMkuu(column:String, columnvalue:String): WakalaMkuu{
//        return dao.searchWakalaMkuu(column, columnvalue)
//    }

    suspend fun searchWakalaMkuuTigo(columnvalue: String): WakalaMkuu {
        return dao.searchWakalaMkuuTigo(columnvalue)
    }
//    suspend fun searchWakalaMkuuTtcl(columnvalue:String): WakalaMkuu{
//        return dao.searchWakalaMkuuTtcl(columnvalue)
//    }

    suspend fun searchWakalaMkuuVoda(columnvalue: String): WakalaMkuu {
        return dao.searchWakalaMkuuVoda(columnvalue)
    }

    suspend fun searchWakalaMkuuHalotel(columnvalue: String): WakalaMkuu {
        return dao.searchWakalaMkuuHalotel(columnvalue)
    }

//    suspend fun searchWakalaMkuuAirtel(columnvalue:String): WakalaMkuu{
//        return dao.searchWakalaMkuuAirtel(columnvalue)
//    }

    suspend fun getWakalaMkuu(): WakalaMkuu {
        return dao.getWakalaMkuu()
    }

    //WAKALA
    val wakala = dao.wakala()

//    val wakalaCSV=dao.wakalaCSV()

//    val getWakalaCount=dao.getWakalaCount()

    suspend fun insertWakala(wakala: List<Wakala>) {
        dao.insertWakala(wakala)
    }

    fun searchViewWakala(text: String): Flow<List<Wakala>> {
        return dao.searchViewWakala(text)
    }


    suspend fun searchWakala(columnvalue: String): Wakala {
        return dao.searchWakala(columnvalue)
    }

    suspend fun searchWakalaContact(columnvalue: String): Wakala {
        return dao.searchWakalaContact(columnvalue)
    }

    suspend fun searchWakalaTigo(columnvalue: String, wakalaidkey: String): Wakala {
        return dao.searchWakalaTigo(columnvalue, wakalaidkey)
    }

    suspend fun searchWakalaVoda(columnvalue: String, wakalaidkey: String): Wakala {
        return dao.searchWakalaVoda(columnvalue, wakalaidkey)
    }

    suspend fun searchWakalaAirtel(columnvalue: String, wakalaidkey: String): Wakala {
        return dao.searchWakalaAirtel(columnvalue, wakalaidkey)
    }

    suspend fun searchWakalaTtcl(columnvalue: String, wakalaidkey: String): Wakala {
        return dao.searchWakalaTtcl(columnvalue, wakalaidkey)
    }

    suspend fun searchWakalaHalotel(columnvalue: String, wakalaidkey: String): Wakala {
        return dao.searchWakalaHalotel(columnvalue, wakalaidkey)
    }

    suspend fun updateWakala(tigopesa: String, wakalaid: String): Int {
        return dao.updateWakala(tigopesa, wakalaid)
    }


    //balance

}