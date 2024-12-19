package com.example.fhein.Screen

import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.painter.Painter
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
import com.example.fhein.Database.MoneyTrackerDatabase
import com.example.fhein.Database.MoneyTrackerViewModel
import com.example.fhein.Database.MoneyTrackerViewModelFactory
import com.example.fhein.Database.Transaction
import com.example.fhein.R
import com.example.fhein.ui.theme.primaryColor
import com.example.fhein.ui.theme.secondaryColor
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun HomeScreen(
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
    val userBalance by moneyViewModel.userBalance.observeAsState(0.0)
    val income by moneyViewModel.totalIncome.observeAsState(0.0)
    val expense by moneyViewModel.totalExpense.observeAsState(0.0)
    var showExpenseSheet by remember { mutableStateOf(false) }
    val transactions by moneyViewModel.transactions.observeAsState(emptyList())
    var chartData:List<Bars> = emptyList()
    val accounts by moneyViewModel.accounts.observeAsState(emptyList())


    //for graph
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 5500)
    )

    for(transaction in transactions){
        val chartBar = Bars(
            label = "",
            values = listOf(Bars.Data(
                value = abs(transaction.amount),
                color = if(transaction.amount > 0) SolidColor(Color.Green) else SolidColor(Color.Red)
            ))
        )
        chartData = chartData + chartBar
        if(chartData.size > 7) {
            chartData = chartData.reversed()
            break
        }
    }

    LaunchedEffect(Unit) {
        Log.d("Money", "LaunchedEffect triggered")
        moneyViewModel.getTotalIncome()
        moneyViewModel.getTotalExpense()
        moneyViewModel.getUserBalance()
        visible = true
    }


    Log.d("Money", "Num of Transactions:${transactions.size.toString()}")
    Log.d("Money", "Num of Chart:${chartData.size.toString()}")

    Scaffold(
        bottomBar = { Footer(navController)},
        floatingActionButton = {FAB({showExpenseSheet = true})},
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.End
    ) { innerPadding ->
        Log.d("Money", "Scaffold content")
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFf6f9ff))

        ) {
            item {
                Text(text = "Welcome Home!", fontSize = 32.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp, top = 8.dp))
            }
            item {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(84.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(primaryColor, Color(0xFF041F9B)),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 1000f)
                            )
                        )
                ) {
                    Text(
                        text = "Total Balance: ${"%.2f".format(userBalance)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(16.dp)
                    )
                }
            }
            item{
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f)
                            .height(98.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(secondaryColor, Color(0xFF041F9B)),
                                    start = Offset(0f, 0f),
                                    end = Offset(1000f, 1000f)
                                )
                            )
                            .padding(16.dp)
                    ){
                        Text(text = "Income", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopStart))
                        Text(text = "$${income}", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.BottomEnd))
                    }
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f)
                            .height(98.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(secondaryColor, Color(0xFF041F9B)),
                                    start = Offset(0f, 0f),
                                    end = Offset(1000f, 1000f)
                                )
                            )
                            .padding(16.dp)
                    ){
                        Text(text = "Expense", fontSize = 16.sp, modifier = Modifier.align(Alignment.TopStart))
                        Text(text = "$${-expense}", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.BottomEnd))
                    }
                }
            }
            item {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(16.dp))
                    Text(
                        "Last 7 Transactions Spending",
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
                        if (transactions.isNotEmpty()) {
                            ColumnChart(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 22.dp)
                                    .alpha(alpha),
                                data = chartData,
                                barProperties = BarProperties(
                                    cornerRadius = Bars.Data.Radius.Rectangle(
                                        topRight = 6.dp,
                                        topLeft = 6.dp
                                    ),
                                    spacing = 3.dp,
                                    thickness = 20.dp
                                ),
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                            )
                        } else {
                            Text(
                                "No data available yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    "History",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No history available yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(transactions.take(10)) { item ->
                    TransactionItem(item)
                }
            }
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
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
    }
}

@Composable
fun TransactionItem(transaction: Transaction){

    val date = Date(transaction.date)
    val placeholder: Painter =
        if(transaction.source == "Cash") {
            if(transaction.amount > 0) painterResource(id = R.drawable.addcash)
            else painterResource(id = R.drawable.delcash)
        }else{
            if(transaction.amount > 0) painterResource(id = R.drawable.addbank)
            else painterResource(id = R.drawable.delbank)
        }

    val formatter = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
    val formattedDate = formatter.format(date)

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardColors(
            containerColor = Color.White,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ){
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Image(
                painter = placeholder,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp)

            )
            Column {
                Text(transaction.category,fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                transaction.description?.let { Text(it, fontSize = 12.sp, color = Color.Gray) }
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(modifier = Modifier.width(intrinsicSize = IntrinsicSize.Max)) {
                Text(transaction.amount.toString(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, color = Color.Black, modifier = Modifier.fillMaxWidth())
                Text(formattedDate, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseSheet(
    accounts: List<Account>,
    onDismissRequest: () -> Unit,
    onSubmitExpense: (Transaction) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf("Cash") }
    var selectedCategory by remember { mutableStateOf("Shopping") }

    var expandedAccount by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    val accountsname = accounts.map { it.name }
    val categories = listOf("Shopping", "Food", "Transport", "Health", "School", "Others")

    val expense = Transaction(
        date = System.currentTimeMillis(),
        source = selectedAccount,
        category = selectedCategory,
        amount =  amount.toDoubleOrNull()?.let { abs(it).let { it * -1 } } ?: 0.0,
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
            Text("Add an Expense", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
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
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .weight(1f)
                        .padding(start = 2.dp) // Add some spacing
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
                onValueChange = { input ->
                    val sanitizedInput = input
                        .replace(',', '.')
                        .filter { it.isDigit() || it == '.' }
                    amount = sanitizedInput
                },
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
                    if(amount.toDouble() != null && amount.isNotEmpty()){
                        onSubmitExpense(expense)
                        onDismissRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

