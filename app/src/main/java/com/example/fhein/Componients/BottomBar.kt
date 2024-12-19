package com.example.fhein.Componients

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fhein.ui.theme.primaryColor
import com.example.fhein.ui.theme.secondaryColor
import com.example.fhein.R


@Composable
fun Footer(navController: NavController) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .size(75.dp)
            .background(Color.White)
            .padding( bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
            ,top = 2.dp)

    ) {
        BottomNavigationItem(
            navController = navController,
            icon = Icons.Default.Home,
            text = "Home",
            routeNavigate = "home",
            selected = navController.currentDestination?.route == "home"
        )
        BottomNavigationItem(
            navController = navController,
            icon = ImageVector.vectorResource(R.drawable.stats),
            text = "Stats",
            routeNavigate = "stats",
            selected = navController.currentDestination?.route == "stats"
        )
        BottomNavigationItem(
            navController = navController,
            icon = ImageVector.vectorResource(R.drawable.wallet),
            text = "budget",
            routeNavigate = "budget",
            selected = navController.currentDestination?.route == "budget"
        )
        BottomNavigationItem(
            navController = navController,
            icon = Icons.Default.AccountCircle,
            text = "Profile",
            routeNavigate = "profile",
            selected = navController.currentDestination?.route == "profile"
        )
    }
}

@Composable
fun BottomNavigationItem(
    navController: NavController,
    icon: ImageVector,
    text: String,
    routeNavigate: String,
    selected: Boolean,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
        navController.navigate(routeNavigate) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
        }
    }) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(24.dp),

            tint = if (selected) primaryColor else Color.Gray
        )
        Text(
            text = text,
            fontSize = 16.sp,
            letterSpacing = (-1).sp,
            color = if (selected) primaryColor else Color.Gray,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
