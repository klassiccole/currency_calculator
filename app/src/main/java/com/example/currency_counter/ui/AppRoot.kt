package com.example.currency_counter.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.currency_counter.ui.theme.CurrencyCalculatorTheme
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.currency_counter.data.ThemePreferences
import com.example.currency_counter.ui.screens.ConverterScreen
import com.example.currency_counter.ui.screens.CounterScreen
import com.example.currency_counter.ui.screens.HomeScreen
import com.example.currency_counter.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val COUNTER = "counter"
    const val CONVERTER = "converter"
    const val SETTINGS = "settings"
}

private fun titleFor(route: String): String = when (route) {
    Routes.COUNTER -> "Currency Counter"
    Routes.CONVERTER -> "Currency Converter"
    Routes.SETTINGS -> "Settings"
    else -> "Currency Calculator"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: Routes.HOME
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var darkModeOverride by remember { mutableStateOf(ThemePreferences.load(context)) }
    val onDarkModeChange: (Boolean?) -> Unit = { newValue ->
        darkModeOverride = newValue
        ThemePreferences.save(context, newValue)
    }
    val darkTheme = darkModeOverride ?: isSystemInDarkTheme()

    CurrencyCalculatorTheme(darkTheme = darkTheme) {

    val open: (String) -> Unit = { destination ->
        scope.launch { drawerState.close() }
        navController.navigate(destination) { launchSingleTop = true }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Currency Calculator",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Currency Counter") },
                    icon = { Icon(Icons.Filled.AttachMoney, contentDescription = null) },
                    selected = route == Routes.COUNTER,
                    onClick = { open(Routes.COUNTER) }
                )
                NavigationDrawerItem(
                    label = { Text("Currency Converter") },
                    icon = { Icon(Icons.Filled.SwapHoriz, contentDescription = null) },
                    selected = route == Routes.CONVERTER,
                    onClick = { open(Routes.CONVERTER) }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    selected = route == Routes.SETTINGS,
                    onClick = { open(Routes.SETTINGS) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(titleFor(route)) },
                    navigationIcon = {
                        if (route == Routes.HOME) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                            }
                        } else {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(padding)
            ) {
                composable(Routes.HOME) {
                    HomeScreen(onNavigate = { destination -> open(destination) })
                }
                composable(Routes.COUNTER) { CounterScreen() }
                composable(Routes.CONVERTER) { ConverterScreen() }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        darkModeOverride = darkModeOverride,
                        onDarkModeChange = onDarkModeChange
                    )
                }
            }
        }
    }
    }
}
