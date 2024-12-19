package com.example.fhein.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fhein.Componients.Footer
import com.example.fhein.Database.MoneyTrackerDatabase
import com.example.fhein.Database.MoneyTrackerViewModel
import com.example.fhein.Database.MoneyTrackerViewModelFactory
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import kotlinx.coroutines.delay

@Composable
fun StatsScreen(
    moneyViewModel: MoneyTrackerViewModel = viewModel(
        factory = MoneyTrackerViewModelFactory(
            transactionDao = MoneyTrackerDatabase.getDatabase(LocalContext.current).transactionDao(),
            accountDao = MoneyTrackerDatabase.getDatabase(LocalContext.current).accountDao(),
            goalDao = MoneyTrackerDatabase.getDatabase(LocalContext.current).goalDao(),
            context = LocalContext.current
        )
    ),
    navController: NavController
) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1000)
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Fetching data...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    } else {
        Scaffold(
            bottomBar = { Footer(navController) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding)
            ) {
                item {
                    Text(
                        text = "Weekly Overview",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    IncomeExpensePieCharts(moneyViewModel)
                }
            }
        }
    }
}

@Composable
fun IncomeExpensePieCharts(viewModel: MoneyTrackerViewModel) {
    val transactions by viewModel.transactions.observeAsState(emptyList())

    val totalIncome = transactions.filter { it.amount > 0.0 }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.amount < 0.0 }.sumOf { -it.amount }

    val incomeData = transactions.filter { it.amount > 0.0 }
        .groupBy { it.category }
        .map { (category, transactions) ->
            val amount = transactions.sumOf { it.amount }
            Pie(
                label = "$category: ${String.format("%.2f", (amount / totalIncome) * 100)}% (${String.format("%.2f", amount)})",
                data = amount,
                color = getRandomColor(),
                style = Pie.Style.Fill
            )
        }

    val expenseData = transactions.filter { it.amount < 0.0 }
        .groupBy { it.category }
        .map { (category, transactions) ->
            val amount = transactions.sumOf { -it.amount }
            Pie(
                label = "$category: ${String.format("%.2f", (amount / totalExpense) * 100)}% (${String.format("%.2f", amount)})",
                data = amount,
                color = getRandomColor(),
                style = Pie.Style.Fill
            )
        }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Income Categories (Total: ${String.format("%.2f", totalIncome)})",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        if (incomeData.isNotEmpty()) {
            PieChartWithLegend(pieData = incomeData)
        } else {
            Text(text = "No Income Data", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(modifier = Modifier.padding( horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Expense Categories (Total: ${String.format("%.2f", totalExpense)})",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        if (expenseData.isNotEmpty()) {
            PieChartWithLegend(pieData = expenseData)
        } else {
            Text(text = "No Expense Data", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}

@Composable
fun PieChartWithLegend(pieData: List<Pie>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PieChart(
            modifier = Modifier
                .size(300.dp)
                .padding(bottom = 16.dp),
            data = pieData,
            onPieClick = {
                println("${it.label} Clicked")
            },
            style = Pie.Style.Fill
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            pieData.forEach { pie ->
                Row(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(pie.color, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pie.label ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}