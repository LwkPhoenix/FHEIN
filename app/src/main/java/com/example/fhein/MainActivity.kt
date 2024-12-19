package com.example.fhein

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fhein.Database.*
import com.example.fhein.Screen.BudgetScreen
import com.example.fhein.Screen.HomeScreen
import com.example.fhein.Screen.ProfileScreen
import com.example.fhein.Screen.StatsScreen
import com.example.fhein.ui.theme.FHEINTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MoneyTrackerViewModel
    private lateinit var transactionDao: TransactionDao
    private lateinit var accountDao: AccountDao
    private lateinit var goalDao: GoalDao
    private lateinit var database: MoneyTrackerDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        // init db and DAOs
        database = MoneyTrackerDatabase.getDatabase(this)
        transactionDao = database.transactionDao()
        accountDao = database.accountDao()
        goalDao = database.goalDao()
//        val databaseInitializer = DatabaseInitializer(transactionDao, accountDao)
//        CoroutineScope(Dispatchers.IO).launch {
//            databaseInitializer.populateDatabase()
//        }


        // factory
        val factory = MoneyTrackerViewModelFactory(transactionDao, accountDao, goalDao, this)
        viewModel = ViewModelProvider(this, factory)[MoneyTrackerViewModel::class.java]

        setContent {
            FHEINTheme {

                Log.d("Money", "Setting content view")
                val navController = rememberNavController()
                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.navigation.compose.NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {

                        composable("home") {
                            Log.d("Money", "Navigating to home screen")
                            HomeScreen(navController = navController)
                        }
                        composable("stats") {
                            StatsScreen(navController = navController)
                        }
                        composable("budget") {
                            BudgetScreen(navController = navController)
                        }
                        composable("profile") {
                            ProfileScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }

}
