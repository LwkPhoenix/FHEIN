package com.example.fhein.Database

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale


class MoneyTrackerViewModelFactory(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val goalDao: GoalDao,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d("ViewModelFactory", "Creating MoneyTrackerViewModel with DAOs")
        if (modelClass.isAssignableFrom(MoneyTrackerViewModel::class.java)) {
            Log.d("ViewModelFactory", "MoneyTrackerViewModel created")
            return MoneyTrackerViewModel(transactionDao, accountDao, goalDao, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MoneyTrackerViewModel(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val goalDao: GoalDao,
    private val context: Context
) : ViewModel() {

    init {
        Log.d("MoneyTrackerViewModel", "ViewModel created with DAOs")
    }

    private val _totalIncome = MutableLiveData(0.0)
    val totalIncome: LiveData<Double> = _totalIncome

    private val _totalExpense = MutableLiveData(0.0)
    val totalExpense: LiveData<Double> = _totalExpense

    private val _userBalance = MutableLiveData(0.0)
    val userBalance: LiveData<Double> = _userBalance

    val transactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()
    val accounts: LiveData<List<Account>> = accountDao.getAllAccounts()
    val goal: LiveData<Goal> = goalDao.getGoal()

    /*
    604800000 is 7 days in milliseconds
     */

    private val dayTracker = DayTracker(context)

    private val _isNewDay = MutableLiveData(false)
    val isNewDay: LiveData<Boolean> = _isNewDay

    init {
        checkForNewDay()
        Log.d("todate", "today: ${dayTracker.getToday()} || previous: ${dayTracker.getPreviousDay()} || isNewDay: ${dayTracker.isNewDay()}")    }

    private fun checkForNewDay() {
        dayTracker.checkAndUpdateDate()
        _isNewDay.value = dayTracker.isNewDay()
    }

    fun resetNewDayFlag() {
        dayTracker.resetNewDayFlag()
        _isNewDay.value = false
    }

    fun getLast7DaysTransactions(startDate: Long): LiveData<List<Transaction>> {
        return transactionDao.getLast7DaysTransactions(startDate - 604800000)
    }

    fun getLast7DaysIncome(startDate: Long): LiveData<List<Transaction>> {
        Log.d("budget", transactionDao.getLast7DaysIncome(startDate).value.toString())
        Log.d("budget", startDate.toString() )
        return transactionDao.getLast7DaysIncome(startDate - 604800000)
    }

    fun getLast7DaysExpense(startDate: Long): LiveData<List<Transaction>> {
        return transactionDao.getLast7DaysExpense(startDate - 604800000)
    }

    suspend fun getUserBalance(): Double = withContext(Dispatchers.IO) {
        val balance = accountDao.getUserBalance()
        _userBalance.postValue(balance)
        balance
    }

    suspend fun getTotalIncome(): Double = withContext(Dispatchers.IO) {
        val income = transactionDao.getTotalIncome()
        _totalIncome.postValue(income)
        income
    }

    suspend fun getTotalExpense(): Double = withContext(Dispatchers.IO) {
        val expense = transactionDao.getTotalExpense()
        _totalExpense.postValue(expense)
        expense
    }

    suspend fun addTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        try {
            val accountId = accountDao.getAccountIdByName(transaction.source)
            Log.d("MoneyTrackerViewModel", "Account ID: $accountId")
            Log.d("MoneyTrackerViewModel", "Transaction amount: ${transaction.amount}")
            if (accountId != null) {
                accountDao.updateAccountBalance(accountId, transaction.amount)
                transactionDao.insertTransaction(transaction)
            } else {
                Log.e("MoneyTrackerViewModel", "Account not found for source: ${transaction.source}")
            }
        } catch (e: Exception) {
            Log.e("MoneyTrackerViewModel", "Error updating account balance", e)
        }
    }

    suspend fun addAccount(account: Account) = withContext(Dispatchers.IO) {
        accountDao.insertAccount(account)
    }

    suspend fun deleteAccount(accountId: Int) = withContext(Dispatchers.IO) {
        accountDao.deleteAccount(accountId)
    }

    suspend fun addGoal(goal: Goal) = withContext(Dispatchers.IO) {
        goalDao.insertGoal(goal)
        dayTracker.setNewDay()
    }

    suspend fun deleteGoal(goal: Goal) = withContext(Dispatchers.IO) {
        goalDao.deleteGoal(goal)
    }

    suspend fun calculateGoalMoneyPerDay(goal: Goal?): Double = withContext(Dispatchers.Default) {
        if(goal != null){
            val startDate = goal.startingDate *1000
            val endDate = goal.endingDate*1000
            val startLocalDate = Instant.ofEpochMilli(startDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val endLocalDate = Instant.ofEpochMilli(endDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val daysInBetween = ChronoUnit.DAYS.between(startLocalDate, endLocalDate)
            Log.d("goal", startLocalDate.toString())
            Log.d("goal", endLocalDate.toString())
            Log.d("goal", daysInBetween.toString())
            if (daysInBetween > 0) goal.amount / daysInBetween else goal.amount
        }
        else 100.0
    }

    suspend fun handleNewDayLogic(
        accounts: List<Account>,
        moneyToSave: Double,
        goal: Goal?,
        database: MoneyTrackerDatabase
    ) {
        Log.d("todate", "call new day logic")
        try {
            val today = dayTracker.getToday() ?: return
            val yesterday = dayTracker.getPreviousDay() ?: return
            val isNewDay = dayTracker.isNewDay()
            val cashAccount = accounts.find { it.name.lowercase() == "cash" }
            Log.d("todate", "trying new day")

            // Save money every day
            if (isNewDay && cashAccount != null && goal != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    database.accountDao().updateAccountBalance(cashAccount.id, -moneyToSave)
                    dayTracker.resetNewDayFlag()
                    Log.d("todate", "save money new day")

                }
            }

            // Check if goal end date is today
            goal?.endingDate?.let { endingDate ->
                val goalEndDate = Instant.ofEpochSecond(endingDate)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                if (today >= goalEndDate.toString()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        deleteGoal(goal)
                        Log.d("todate", "Goal '${goal.name}' automatically deleted on its ending date.")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("todate", "Error Newday", e)
        }
    }


}


class DayTracker(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences("DayTrackerPrefs", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    companion object {
        private const val KEY_TODAY = "date_today"
        private const val KEY_PREVIOUS_DAY = "previous_day"
        private const val KEY_IS_NEW_DAY = "is_new_day"
    }

    private fun getTodayDate(): String {
        return dateFormat.format(Date())
    }

    fun checkAndUpdateDate() {
        val todayDate = getTodayDate()
        val previousDay = preferences.getString(KEY_PREVIOUS_DAY, null)

        if (previousDay == null) {
            // First time opening the app
            preferences.edit().apply {
                putString(KEY_PREVIOUS_DAY, todayDate)
                putString(KEY_TODAY, todayDate)
                putBoolean(KEY_IS_NEW_DAY, true)
                apply()
            }
        } else {
            val storedToday = preferences.getString(KEY_TODAY, "")
            if (storedToday != todayDate) {
                // A new day has started
                preferences.edit().apply {
                    putString(KEY_PREVIOUS_DAY, storedToday)
                    putString(KEY_TODAY, todayDate)
                    putBoolean(KEY_IS_NEW_DAY, true)
                    apply()
                }
            }
        }
        Log.d("date", {"today:${todayDate} || previous:${previousDay}"}.toString())
    }

    fun resetNewDayFlag() {
        preferences.edit().putBoolean(KEY_IS_NEW_DAY, false).apply()
    }

    fun isNewDay(): Boolean {
        return preferences.getBoolean(KEY_IS_NEW_DAY, false)
    }

    fun getToday(): String? {
        return preferences.getString(KEY_TODAY, null)
    }

    fun getPreviousDay(): String? {
        return preferences.getString(KEY_PREVIOUS_DAY, null)
    }
    fun setNewDay(){
        preferences.edit().putBoolean(KEY_IS_NEW_DAY, true).apply()
    }
}