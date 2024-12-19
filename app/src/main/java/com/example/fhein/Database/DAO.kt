package com.example.fhein.Database

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
@Dao
interface TransactionDao {

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE amount > 0")
    suspend fun getTotalIncome(): Double

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE amount < 0")
    suspend fun getTotalExpense(): Double

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate")
    fun getLast7DaysTransactions(startDate: Long): LiveData<List<Transaction>>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Int)

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND amount > 0")
    fun getLast7DaysIncome(startDate: Long): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND amount < 0")
    fun getLast7DaysExpense(startDate: Long): LiveData<List<Transaction>>
}

@Dao
interface AccountDao {
    @Upsert
    suspend fun insertAccount(account: Account)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccount(id: Int)

    @Query("UPDATE accounts SET amount = amount + :amount WHERE id = :id")
    suspend fun updateAccountBalance(id: Int, amount: Double)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM accounts")
    suspend fun getUserBalance(): Double

    @Query("SELECT id FROM accounts WHERE name = :accountName")
    fun getAccountIdByName(accountName: String): Int?
    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): LiveData<List<Account>>
}

@Dao
interface GoalDao {
    @Insert
    suspend fun insertGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM goals")
    fun getGoal(): LiveData<Goal>
}

@Database(entities = [Transaction::class, Account::class, Goal::class], version = 1)
abstract class MoneyTrackerDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: MoneyTrackerDatabase? = null

        fun getDatabase(context: Context): MoneyTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoneyTrackerDatabase::class.java,
                    "money_tracker_database"
                ).build()
                Log.d("MoneyTrackerDatabase", "Database created: $instance")  // Add this log
                INSTANCE = instance
                instance
            }
        }
        fun deleteDatabase(context: Context) {
            synchronized(this) {
                INSTANCE?.clearAllTables() // Clears all tables in the current database
                Log.d("MoneyTrackerDatabase", "Database cleared")
                INSTANCE = null // Reset the instance
                context.deleteDatabase("money_tracker_database") // Deletes the database file
                Log.d("MoneyTrackerDatabase", "Database file deleted")
            }
        }
    }
}


class DatabaseInitializer(private val transactionDao: TransactionDao, private val accountDao: AccountDao) {

    suspend fun populateDatabase() {
        // Insert Accounts
        accountDao.insertAccount(Account(name = "Cash", amount = 1900.0))
        accountDao.insertAccount(Account(name = "Agribank", amount = 1550.0))
        accountDao.insertAccount(Account(name = "Vietcombank", amount = 2000.0))

        // Insert Transactions (12 Transactions)
        transactionDao.insertTransaction(Transaction(date = System.currentTimeMillis(), source = "Cash", category = "OT", amount = 100.0, description = "Reimbursement"))
        transactionDao.insertTransaction(Transaction(date = System.currentTimeMillis(), source = "Vietcombank", category = "Entertainment", amount = -150.0, description = "Movie Tickets"))
        transactionDao.insertTransaction(Transaction(date = System.currentTimeMillis(), source = "Vietcombank", category = "School", amount = -500.0, description = "Books and materials"))
        transactionDao.insertTransaction(Transaction(date = System.currentTimeMillis(), source = "Agribank", category = "Salary", amount = 150.0, description = "Freelance work"))
        transactionDao.insertTransaction(Transaction(date = System.currentTimeMillis(), source = "Cash", category = "Other", amount = 50.0, description = "Small refund"))

        transactionDao.insertTransaction(Transaction(date = System.currentTimeMillis(), source = "Agribank", category = "Food", amount = -50.0, description = "Lunch"))
        transactionDao.insertTransaction(Transaction(date = System.currentTimeMillis(), source = "Cash", category = "Shopping", amount = -200.0, description = "Clothes"))
        transactionDao.insertTransaction(Transaction(date = System.currentTimeMillis(), source = "Agribank", category = "Salary", amount = 500.0, description = "Salary"))
        transactionDao.insertTransaction(Transaction(date = System.currentTimeMillis(), source = "Vietcombank", category = "Bonus", amount = 300.0, description = "Gift"))
        transactionDao.insertTransaction(Transaction(date = System.currentTimeMillis(), source = "Cash", category = "Other", amount = -75.0, description = "Miscellaneous expenses"))
        transactionDao.insertTransaction(Transaction(date = System.currentTimeMillis(), source = "Agribank", category = "Transportation", amount = -100.0, description = "Bus fare"))
        transactionDao.insertTransaction(Transaction(date = System.currentTimeMillis(), source = "Vietcombank", category = "Salary", amount = 200.0, description = "Refund from school"))
    }
}