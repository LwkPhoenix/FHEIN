package com.example.fhein.Screen

import android.util.Log
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fhein.Componients.FAB
import com.example.fhein.Componients.Footer
import com.example.fhein.Database.Account
import com.example.fhein.Database.Goal
import com.example.fhein.Database.MoneyTrackerDatabase
import com.example.fhein.Database.MoneyTrackerViewModel
import com.example.fhein.Database.MoneyTrackerViewModelFactory
import com.example.fhein.Database.Transaction
import com.example.fhein.R
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.abs
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetScreen(
    moneyViewModel: MoneyTrackerViewModel = viewModel(
        factory = MoneyTrackerViewModelFactory(
            transactionDao = MoneyTrackerDatabase.getDatabase(LocalContext.current).transactionDao(),
            accountDao = MoneyTrackerDatabase.getDatabase(LocalContext.current).accountDao(),
            goalDao = MoneyTrackerDatabase.getDatabase(LocalContext.current).goalDao(),
            context = LocalContext.current
        )
    ),
    navController: NavController
){
    val database = MoneyTrackerDatabase.getDatabase(LocalContext.current)
    val last7DaysIncome by moneyViewModel.getLast7DaysIncome(System.currentTimeMillis()).observeAsState(emptyList())
    val transactions by moneyViewModel.transactions.observeAsState(emptyList())
    val accounts by moneyViewModel.accounts.observeAsState(emptyList())
    val goal by moneyViewModel.goal.observeAsState()
    var moneyToSave by remember { mutableStateOf(0.0) }

    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showIncomeSheet by remember { mutableStateOf(false) }
    var showExpenseSheet by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showDeleteGoalDialog by remember { mutableStateOf(false) }
    var chosenAccount:Account by remember { mutableStateOf( Account(name = "", amount = 0.0)) }

    var lines  = generateLinesForAccounts(accounts, transactions)
    //for graph
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 5500)
    )

    LaunchedEffect(Unit) {
        moneyViewModel.getTotalIncome()
        moneyViewModel.getTotalExpense()
        moneyViewModel.getUserBalance()
        moneyToSave = moneyViewModel.calculateGoalMoneyPerDay(goal)
        Log.d("goal", goal.toString())
        visible = true
        moneyViewModel.handleNewDayLogic(
            accounts = accounts,
            moneyToSave = moneyToSave,
            goal = goal,
            database = database
        )
    }


    Log.d("budget", last7DaysIncome.toString())

    Scaffold(
        bottomBar = { Footer(navController) },
        floatingActionButton = {FAB({showIncomeSheet = true})}
    ) {innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            item{Text("My Accounts", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)}
            item{
                LazyRow(modifier = Modifier.padding(top = 32.dp)) {
                    items(accounts){account ->
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .shadow(1.dp, RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            getRandomColor(),
                                            getRandomColor()
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(1f, 1f)
                                    )
                                )
                                .size(230.dp, 260.dp)
                                .combinedClickable(
                                    onClick = {
                                    },
                                    onLongClick = {
                                        chosenAccount = account; showDeleteDialog = true
                                    }
                                )
                                .padding(16.dp)
                        ){
                            Column(modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)) {
                                Text("My balance:",fontSize = 16.sp, fontWeight = FontWeight.Normal)
                                Text("$${"%.2f".format(account.amount)}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .fillMaxWidth())
                            {
                                Text(
                                    account.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                )
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                            .height(32.dp)
                                            .clickable {
                                                chosenAccount = account; showDepositDialog = true
                                            }
                                            .padding(4.dp)
                                            .weight(1f)
                                    ) {
                                        Text("Deposit", textAlign = TextAlign.Center, modifier = Modifier.fillMaxSize())
                                    }
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                            .height(32.dp)
                                            .clickable {
                                                chosenAccount = account; showWithdrawDialog = true
                                            }
                                            .padding(4.dp)
                                            .weight(1f)
                                    ) {
                                        Text("Withdraw", textAlign = TextAlign.Center, modifier = Modifier.fillMaxSize())
                                    }
                                }
                            }
                        }
                    }
                    item{
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .shadow(1.dp, RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            getRandomColor(),
                                            getRandomColor()
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(1f, 1f)
                                    )
                                )
                                .size(230.dp, 260.dp)
                                .clickable { showAccountDialog = true }
                                .padding(16.dp)

                        ){
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                                ){
                                Icon(painterResource(R.drawable.plus), contentDescription = null, modifier = Modifier.size(64.dp))
                                Text("Add an account", fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                Text("(Long press an account to delete it)", fontSize = 10.sp, color = Color.DarkGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }

            }
            item{
                Column {
                    HorizontalDivider(modifier = Modifier.padding(16.dp))
                    Text(
                        "Account balance History",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(280.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                            .shadow(
                                1.dp,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(16.dp)
                    ) {
                        if (lines.isNotEmpty()) {
                            LineChart(
                                modifier = Modifier.alpha(alpha),
                                data = lines,
                            )
                        } else {
                            Text(
                                "No data available for the chart",
                                modifier = Modifier.align(Alignment.Center),
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your Goal",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.White,
                        disabledContentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if(goal != null){

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                         showDeleteGoalDialog = true
                                    }
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1.7f)
                                    .padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "Goal:",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                goal?.name?.let {
                                    Text(
                                        text = it,
                                        fontSize = 22.sp,
                                        color = Color(0xFFFEBC2E),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                goal?.endingDate?.let { timestamp ->
                                    val formattedDate = Instant.ofEpochSecond(timestamp)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                        .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) // Example: December 16, 2024
                                    Text(
                                        text = formattedDate,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Top

                            ) {
                                Text(
                                    text = "Saving:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "$${"%.2f".format(moneyToSave)}/day",

                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00BF00),
                                    textAlign = TextAlign.End
                                )
                                if(moneyViewModel.isNewDay.value == true){
                                    Text(
                                        text = "Not enough money on cash to save!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Red
                                    )
                                }
                                else if(moneyViewModel.isNewDay.value == false){
                                    Text(
                                        text = "Goal budget saved!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color(0xFF00BF00)
                                    )
                                }
                            }
                        }
                    }
                    else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showGoalDialog = true }
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFEBC2E),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Goal Set",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Click here to create a new goal to track your progress!",
                                fontSize = 14.sp,
                                color = Color(0xFF777777),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            item{
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        if (showDepositDialog) {
            DepositDialog(
                onConfirm = { amount ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.accountDao().updateAccountBalance(chosenAccount.id, amount)
                        withContext(Dispatchers.Main) {
                            showDepositDialog = false
                        }
                    }
                },
                onDismiss = { showDepositDialog = false }
            )
        }

        if (showWithdrawDialog) {
            WithdrawDialog(
                onConfirm = { amount ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.accountDao().updateAccountBalance(chosenAccount.id, -amount)
                        withContext(Dispatchers.Main) {
                            showWithdrawDialog = false
                        }
                    }
                },
                onDismiss = { showWithdrawDialog = false }
            )
        }

        if (showAccountDialog) {
            AccountNameAndMoneyDialog(
                onConfirm = { accountName, amount ->
                    CoroutineScope(Dispatchers.IO).launch {
                        database.accountDao().insertAccount(Account(name = accountName, amount = amount))
                        withContext(Dispatchers.Main) {
                            showAccountDialog = false
                        }
                    }
                },
                onDismiss = { showAccountDialog = false }
            )
        }
        if (showDeleteDialog) {
            ConfirmDeleteDialog(
                onConfirm = {
                    CoroutineScope(Dispatchers.IO).launch {
                        database.accountDao().deleteAccount(chosenAccount.id)
                        withContext(Dispatchers.Main) {
                            showDeleteDialog = false
                        }
                    }
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
        if (showIncomeSheet) {
            IncomeSheet(
                accounts = accounts,
                onDismissRequest = { showIncomeSheet = false },
                onSubmitExpense = { income ->
                    CoroutineScope(Dispatchers.Main).launch {
                        moneyViewModel.addTransaction(income)
                    }
                }
            )
        }

        if (showExpenseSheet) {
            ExpenseSheet(
                accounts = accounts,
                onDismissRequest = { showExpenseSheet = false },
                onSubmitExpense = { expense ->
                    CoroutineScope(Dispatchers.Main).launch {
                        moneyViewModel.addTransaction(expense)
                    }
                }
            )
        }

        if (showGoalDialog) {
            AddGoalDialog(
                onDismiss = { showGoalDialog = false },
                onConfirm = { goal ->
                    CoroutineScope(Dispatchers.Main).launch {
                        moneyViewModel.addGoal(goal)
                        showGoalDialog = false
                        moneyToSave = moneyViewModel.calculateGoalMoneyPerDay(goal)

                    }
                }
            )
        }
        if (showDeleteGoalDialog){
            ConfirmDeleteDialog(
                onDismiss = { showDeleteGoalDialog = false },
                onConfirm = {
                    CoroutineScope(Dispatchers.Main).launch {
                        moneyViewModel.deleteGoal(goal!!)
                        showDeleteGoalDialog = false
                    }
                }
            )
        }
    }
}


fun generateLinesForAccounts(
    accounts: List<Account>,
    transactions: List<Transaction>
): List<Line> {
    val lines = mutableListOf<Line>()
    val groupedTransactions = transactions.groupBy { it.source }

    accounts.forEach { account ->
        val cumulativeAmounts = mutableListOf<Double>()
        var currentBalance = account.amount.toFloat()
        cumulativeAmounts.add(currentBalance.toDouble())

        val accountTransactions = groupedTransactions[account.name] ?: emptyList()
        accountTransactions.forEach { transaction ->
            currentBalance -= transaction.amount.toFloat()
            cumulativeAmounts.add(currentBalance.toDouble())
        }

        cumulativeAmounts.reverse()

        val lineColor = getRandomColor()
        lines.add(
            Line(
                label = account.name,
                values = cumulativeAmounts,
                color = SolidColor(lineColor),
                firstGradientFillColor = lineColor.copy(alpha = .4f),
                secondGradientFillColor = Color.Transparent,
                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                gradientAnimationDelay = 1000,
                drawStyle = DrawStyle.Stroke(width = 2.dp)
            )
        )
    }

    return lines
}

fun getRandomColor(): Color {
    val red = Random.nextInt(100, 250)
    val green = Random.nextInt(100, 250)
    val blue = Random.nextInt(100, 250)
    return Color(red, green, blue)
}

@Composable
fun DepositDialog(
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Deposit Money") },
        text = {
            Column {
                Text(text = "Enter the amount to deposit:")
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val depositAmount = amount.toDoubleOrNull()?.let { abs(it) } ?: 0.0
                onConfirm(depositAmount)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun WithdrawDialog(
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Withdraw Money") },
        text = {
            Column {
                Text(text = "Enter the amount to withdraw:")
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val withdrawAmount = amount.toDoubleOrNull()?.let { abs(it) } ?: 0.0
                onConfirm(withdrawAmount)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }

    )
}


@Composable
fun AccountNameAndMoneyDialog(
    onConfirm: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var accountName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Account Details") },
        text = {
            Column {
                Text(text = "Enter the account name:")
                TextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Enter the amount:")
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val enteredAmount = amount.toDoubleOrNull() ?: 0.0
                onConfirm(accountName, enteredAmount)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Delete Confirmation") },
        text = { Text(text = "Are you sure you want to delete this item? This action cannot be undone.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = "Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (Goal) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val startDate = System.currentTimeMillis()/1000
    var endDate by remember { mutableStateOf(0L) }

    var showCalendar by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Add New Goal", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Goal Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { showCalendar = true }) {
                    Text(
                        text = if (endDate != 0L) {
                            "Selected: ${startDate.toReadableDate()} to ${endDate.toReadableDate()}"
                        } else {
                            "Select Goal Dates"
                        }
                    )
                }

                if (showCalendar) {
                    CalendarDialog(
                        state = rememberUseCaseState(visible = true),
                        config = CalendarConfig(
                            style = CalendarStyle.MONTH,
                            disabledDates = generateSequence(LocalDate.now().minusDays(1)) { it.minusDays(1) }
                                .takeWhile { it.isAfter(LocalDate.of(1970, 1, 1)) || it.isEqual(LocalDate.of(1970, 1, 1)) } // Stop at the startDate
                                .toList()
                        ),
                        selection = CalendarSelection.Date(
                        ) { date ->
                            endDate = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
                            showCalendar = false
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && amount.toDoubleOrNull() != null && startDate > 0 && endDate > 0) {
                        onConfirm(
                            Goal(
                                name = name,
                                startingDate = startDate,
                                endingDate = endDate,
                                amount = abs(amount.toDouble())
                            )
                        )
                    } else {
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
fun Long.toReadableDate(): String {
    val dateFormat = SimpleDateFormat("dd MMM yyyy")
    val date = Date(this*1000)
    return dateFormat.format(date)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeSheet(
    accounts: List<Account>,
    onDismissRequest: () -> Unit,
    onSubmitExpense: (Transaction) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf("Cash") }
    var selectedCategory by remember { mutableStateOf("Salary") }

    // Dropdown menu states
    var expandedAccount by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    val accountsname = accounts.map { it.name }
    val categories = listOf("Salary", "Bonus", "OT", "Others")

    val expense = Transaction(
        date = System.currentTimeMillis(),
        source = selectedAccount,
        category = selectedCategory,
        amount =  amount.toDoubleOrNull()?.let { abs(it) } ?: 0.0,
        description = comment
    )
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxHeight(.8f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add an Income", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedAccount,
                    onExpandedChange = { expandedAccount = !expandedAccount },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .weight(.95f)
                        .padding(end = 2.dp)
                ) {
                    TextField(
                        value = selectedAccount,
                        onValueChange = {}, // read-only
                        readOnly = true,
                        label = { Text("Account") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccount,
                        onDismissRequest = { expandedAccount = false },
                    ) {
                        accountsname.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(text = account) },
                                onClick = {
                                    selectedAccount = account
                                    expandedAccount = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(.05f))
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .weight(1f)
                        .padding(start = 2.dp)
                ) {
                    TextField(
                        value = selectedCategory,
                        onValueChange = {selectedCategory = it},
                        readOnly = false,
                        label = { Text("Category") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        },
                        maxLines = 1,
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(text = category) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.displayLarge.copy(
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                placeholder = {
                    Text(
                        "$0.00",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add comment...") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Text
                ),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()?.let { it * -1 } ?: 0.0
                    onSubmitExpense(expense)
                    onDismissRequest()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

