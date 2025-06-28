package com.example.docsproject.presentation.ui

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.docsproject.R
import com.example.docsproject.presentation.ui.theme.Background
import com.example.docsproject.presentation.ui.theme.BluePrimary
import com.example.docsproject.presentation.ui.theme.DocsProjectTheme
import com.example.docsproject.presentation.viewmodels.PhotoViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.viewmodel.ext.android.viewModel

sealed class Route(
    val route: String,
    @DrawableRes val icon: Int
) {

    data object ESign : Route("E-sign", R.drawable.e_sign)

    data object Settings : Route("Settings", R.drawable.settings)
}

val ROUTES = listOf(Route.ESign, Route.Settings)

class MainActivity : ComponentActivity() {
    val viewModel: PhotoViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            viewModel.onPhotoTaken(success)
        }
        enableEdgeToEdge()
        setContent {
            DocsProjectTheme {
                ChangeStatusBarAndNavBarColor()
                val navController = rememberNavController()
                val historyState = remember { mutableStateOf(false) }
                val documentState = remember { mutableStateOf(false) }
                fun isActive(): Boolean { return !historyState.value || !documentState.value}
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar(containerColor = Color.White) {
                                ROUTES.forEach { route ->

                                    val selected = navController.currentRoute() == route.route
                                    NavigationBarItem(
                                        selected = true,
                                        onClick = {
                                            navController.navigate(route.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(route.icon),
                                                    contentDescription = route.route,
                                                    modifier = Modifier.size(24.dp),
                                                    tint = if (selected) BluePrimary
                                                    else MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.6f
                                                    )
                                                )
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = route.route,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (selected) BluePrimary else MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.6f
                                                    )
                                                )
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = Color.Transparent,
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Route.ESign.route,
                            modifier = Modifier
                                .padding(innerPadding)
                                .padding(top = 20.dp)
                        ) {
                            composable(Route.ESign.route) {
                                ESignScreen(historyState = historyState, onTakePhotoClick = {
                                    val uri = viewModel.preparePhotoFile(this@MainActivity)
                                    cameraLauncher.launch(uri)
                                }, documentState)
                            }
                            composable(Route.Settings.route) {
                                SettingsScreen()
                            }
                        }
                    }

                    val alpha = animateFloatAsState(
                        targetValue = if (isActive()) 0f else 0.7f,
                        animationSpec = tween(durationMillis = 300)
                    )

                    if (alpha.value > 0.001f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = alpha.value))
                        )
                    }
                    History(historyState, viewModel.getAllPhotos(this@MainActivity))
                }
            }
        }
    }
}

@Composable
fun ChangeStatusBarAndNavBarColor() {
    val context = LocalContext.current
    val view = LocalView.current
    val window = (context as? ComponentActivity)?.window
        ?: throw Exception("Not in an Activity")

    WindowCompat.setDecorFitsSystemWindows(window, false)

    val insetsController = WindowInsetsControllerCompat(window, view)

    insetsController.isAppearanceLightStatusBars = true
    insetsController.isAppearanceLightNavigationBars = true

    window.statusBarColor = Background.toArgb()
    window.navigationBarColor = Color.White.toArgb()
}

@Composable
fun NavController.currentRoute(): String? {
    val navBackStackEntry = currentBackStackEntryAsState()
    return navBackStackEntry.value?.destination?.route
}
