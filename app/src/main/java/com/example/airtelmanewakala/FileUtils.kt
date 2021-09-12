package com.example.airtelmanewakala

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.NumberFormat
import android.os.Build
import android.telephony.SmsManager
import android.text.format.DateUtils
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.example.airtelmanewakala.db.MobileRepository
import com.example.airtelmanewakala.db.MoblieDatabase
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

var fromnetwork = "Airtelmoney"
const val mtandao = "+255714363729"
const val errornumber = "+245683071757"
val contactnumber = "+255714363627"
var floatinchange = StringBuilder()
var floatoutchange = StringBuilder()
fun generateFile(context: Context?, fileName: String): File? {
        val csvFile = File(context?.filesDir, fileName)
        csvFile.createNewFile()

        return if (csvFile.exists()) {
            csvFile
        } else {
            null
        }
    }

    fun goToFileIntent(context: Context?, file: File): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        val contentUri = context?.let { FileProvider.getUriForFile(it, "${context.packageName}.fileprovider", file) }
        val mimeType = contentUri?.let { context?.contentResolver.getType(it) }
        intent.setDataAndType(contentUri, mimeType)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        return intent
    }

fun sendSms(number: String, smstext: String) {
    val sms = SmsManager.getDefault()
    val parts: ArrayList<String> = sms.divideMessage(smstext)

    sms.sendMultipartTextMessage(
        number,
        null,
        parts,
        null,
        null
    )
}

fun containsWords(inputString: String, items: Array<String>): Boolean {
    var found = true
    for (item in items) {
        if (!inputString.contains(item)) {
            found = false
            break
        }
    }
    return found
}

fun filterBody(str: String, n: Int): String {
    return str.split(" ")[n - 1]
}

fun filterNumber(str: String): String {
    return str.replace("+255", "0")
}

fun filterMoney(str: String): String {
    val i = str.substringBefore(".00")
    val word = Regex("[^0-9]")
    return word.replace(i, "")
}


@RequiresApi(Build.VERSION_CODES.N)
fun getComma(i: String): String {
    val ans = NumberFormat.getNumberInstance(Locale.US).format(i.toInt())
    return ans.toString()
}

@RequiresApi(Build.VERSION_CODES.O)
fun getDate(created:Long): String? {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    val instant = Instant.ofEpochMilli(created.toLong())
    val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return formatter.format(date).toString()
}

fun isTodayDate(x:Long) : Boolean {
    return DateUtils.isToday(x)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getTime(created:Long): String? {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    val instant = Instant.ofEpochMilli(created.toLong())
    val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return formatter.format(date).toString()
}

 fun filterName(str: String): String {
    val i = str.substringBefore(".00")
    val word = Regex("[^0-9 ]")
    return word.replace(i, "")
}

 fun filter(str: String): String {
    val word = Regex("[^0-9 ]")
    val words = word.replace(str, "")
    return words.substring(0, words.length - 2)
}

 fun checkFloatIn(str: String): Boolean {

    val i = str.contains("Umepokea")

    //amount
    val amount = filterBody(str, 2)
    val amountRegex = Regex("^Tsh\\d+(,\\d{3})*(\\.00)?\$")
    val checkAmount = amount.matches(amountRegex)

    //name
    val namedata = str.substringAfter("kutoka ")
    val namedata2 = namedata.substring(9)
    val namedata3 = namedata2.substringBefore(" Salio")
    val nameRegex = Regex("^,[A-Za-z0-9 ]+.")
    val checkName = namedata3.matches(nameRegex)

    //balance
    val balancedata = str.substringAfter("jipya ")
    val balancedata2 = balancedata.substringBefore(".Muamala")
    val balanceRegex = Regex("^Tsh\\d+(,\\d{3})*(\\.00)?\$")
    val checkBalance = balancedata2.matches(balanceRegex)

    //Transid
    val transiddata = str.substringAfter("Muamala")
    val transiddata2 = transiddata.replace("\\s+".toRegex(), "")
    val transidRegex = Regex("^No:PP\\d{6}.\\d{4}.[A-Z0-9]{6}")
    val checkTransid = transiddata2.matches(transidRegex)

    if (!checkName) {
        floatinchange.append("Name ")
    }

    if (!checkAmount) {
        floatinchange.append("Amount ")
    }

    if (!checkTransid) {
        floatinchange.append("Transid ")
    }

    if (!checkBalance) {
        floatinchange.append("Balance ")
    }

    return checkName && checkAmount && checkTransid && checkBalance && i
}

 fun getFloatIn(str: String): Array<String> {

    //amount
    val amountdata = filterBody(str, 2)
    val amount = filterMoney(amountdata)

    //name
    val namedata = str.substringAfter("kutoka ")
    val namedata2 = namedata.substring(9)
    val namedata3 = namedata2.substringBefore(" Salio")
    val nameRegex = Regex("[^A-Za-z0-9 _]")
    val name = nameRegex.replace(namedata3, "").trim()

    //balance
    val balancedata = str.substringAfter("jipya ")
    val balancedata2 = balancedata.substringBefore(".Muamala")
    val balance = filterMoney(balancedata2)

    //transid
    val transiddata = str.substringAfter("Muamala")
    val transiddata2 = transiddata.replace("\\s+".toRegex(), "")
    val transid = transiddata2.removeRange(0, 3)

    return arrayOf(amount, name, balance, transid)
}


fun checkFloatOut(str: String): Boolean {

    val i = str.contains("Umetuma")

    //amount
    val amount = filterBody(str, 2)
    val amountRegex = Regex("^\\d+(,\\d{3})*(\\.00)?\$")
    val checkAmount = amount.matches(amountRegex)

    //name
    val namedata = str.substringAfter("kwenda ")
    val namedata2 = namedata.substring(9)
    val namedata3 = namedata2.substringBefore("Makato")
    val nameRegex = Regex("^,[A-Za-z0-9 ]+.")
    val checkName = namedata3.matches(nameRegex)

    //balance
    val balancedata = str.substringAfter("Salio Jipya Tsh ")
    val balancedata2 = balancedata.substringBefore(".Muamala")
    val balanceRegex = Regex("^\\d+(,\\d{3})*(\\.00)?\$")
    val checkBalance = balancedata2.matches(balanceRegex)

    //Transid
    val transiddata = str.substringAfter("Muamala")
    val transiddata3 = transiddata.replace("\\s+".toRegex(), "")
    val transidRegex = Regex("^No.PP\\d{6}.\\d{4}.[A-Z0-9]{6}")
    val checkTransid = transiddata3.matches(transidRegex)

    if (!checkName) {
        floatoutchange.append("Name ")
    }

    if (!checkAmount) {
        floatoutchange.append("Amount ")
    }

    if (!checkTransid) {
        floatoutchange.append("Transid ")
    }

    if (!checkBalance) {
        floatoutchange.append("Balance ")
    }

    return checkName && checkAmount && checkTransid && checkBalance && i
}



 fun getFloatOut(str: String): Array<String> {
    //amount
    val amountdata = filterBody(str, 2)
    val amount = filterMoney(amountdata)

    //name
    val namedata = str.substringAfter("kwenda ")
    val namedata2 = namedata.substring(9)
    val namedata3 = namedata2.substringBefore("Makato")
    val nameRegex = Regex("[^A-Za-z0-9 _]")
    val name = nameRegex.replace(namedata3, "").trim()

    //balance
    val balancedata = str.substringAfter("Salio Jipya Tsh ")
    val balancedata2 = balancedata.substringBefore(".Muamala")
    val balance = filterMoney(balancedata2)

    //transid
    val transiddata = str.substringAfter("Muamala")
    val transiddata3 = transiddata.replace("\\s+".toRegex(), "")
    val transid = transiddata3.removeRange(0, 3)


    return arrayOf(amount, name, balance, transid)
}
