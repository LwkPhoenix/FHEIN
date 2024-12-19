package com.example.fhein.Database

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt
import kotlin.random.Random

class MyApplication : Application() {
    lateinit var database: MoneyTrackerDatabase

    override fun onCreate() {
        super.onCreate()
        database = MoneyTrackerDatabase.getDatabase(this)

    }
}


//CoroutineScope(Dispatchers.IO).launch {
//    val transaction = Transaction(
//        date = Calendar.getInstance().timeInMillis,
//        source = "Cash",
//        category = "Shopping",
//        amount = 300.0,
//    )
//    database.transactionDao().insertTransaction(transaction)
//}