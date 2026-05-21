package com.example.healthocr.nav

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.healthocr.AppViewModel
import com.example.healthocr.R
import com.example.healthocr.pages.AnalyzedImage
import com.example.healthocr.pages.ContentWithTopBar
import com.example.healthocr.pages.DeviceSetup
import com.example.healthocr.pages.DevicesList
import com.example.healthocr.pages.ExportPage
import com.example.healthocr.pages.statistics.Statistics
import com.example.healthocr.pages.camera.Camera
import com.example.healthocr.pages.sessionHistory.SessionPage
import com.example.healthocr.pages.sessionHistory.SessionsList
import com.example.healthocr.pages.statistics.MetricsPage
import com.example.healthocr.storage.Metrics
import com.example.healthocr.ui.theme.BarColor

sealed class NavRoutes(val route: String){
    object Statistics: NavRoutes("statistics")
    object DeviceSetup: NavRoutes("deviceSetup")
    object Camera: NavRoutes("camera")
    object AnalyzedImage: NavRoutes("analyzedImage")
    object SessionsList: NavRoutes("sessionsList")
    object SessionPage: NavRoutes("sessionPage")
    object MetricsPage: NavRoutes("chart")
    object Devices: NavRoutes("devices")
    object Export: NavRoutes("export")
}

data class BarItem(
    val title: String,
    val icon: Int,
    val route: String
)

object NavBarItems {
    val BarItems = listOf(
        BarItem(
            title = "Статистика",
            icon = R.drawable.statistics,
            route = NavRoutes.Statistics.route
        ),
        BarItem(
            title = "Сессии",
            icon = R.drawable.sessionshistory,
            route = NavRoutes.SessionsList.route
        ),
        BarItem(
            title = "Камера",
            icon = R.drawable.camera,
            route = NavRoutes.Camera.route
        ),
        BarItem(
            title = "Устройства",
            icon = R.drawable.tonometer,
            route = NavRoutes.Devices.route
        ),
    )
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier,
                  navController: NavHostController,
                  startDestination: String = NavRoutes.Camera.route,
                  viewModel: AppViewModel,
                  scaffoldPaddingValues: PaddingValues,
                  snackbarHostState: SnackbarHostState
)
{
    val toAnalyzedImage = { navController.navigate(NavRoutes.AnalyzedImage.route) }
    val toDeviceSetup = { navController.navigate(NavRoutes.DeviceSetup.route) }
    val toCamera = { navController.navigate(NavRoutes.Camera.route) }
    val toSession: (Long) -> Unit = { navController.navigate(NavRoutes.SessionPage.route + "/$it") }
    val toMetricsPage: (String) -> Unit = { navController.navigate(NavRoutes.MetricsPage.route + "/$it") }
    val toExport = { navController.navigate(NavRoutes.Export.route) }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    LaunchedEffect(currentRoute) {
        viewModel.clearAcceptWindow()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ){
        composable(
            NavRoutes.Export.route,
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            }
        ){
            LaunchedEffect(Unit) {
                viewModel.showBottomNavBar.value = false
            }
            ContentWithTopBar(viewModel, "Экспорт в CSV", scaffoldPaddingValues, toPrevious = { navController.popBackStack() }) {
                ExportPage(viewModel, { navController.popBackStack() }, snackbarHostState)
            }
        }
        composable(
            NavRoutes.Statistics.route,
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            }
        ){
            LaunchedEffect(Unit) {
                viewModel.showBottomNavBar.value = true
            }
            ContentWithTopBar(viewModel, "Статистика", scaffoldPaddingValues) {
                Statistics(viewModel, toMetricsPage, toExport)
            }
        }
        composable(
            NavRoutes.MetricsPage.route + "/{metrics}",
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            }
        ){ stackEntry ->
            val metrics = (stackEntry.arguments?.getString("metrics")?.split(",") ?: listOf())
                .map { Metrics.getTypeByMetricCode(it) }
            ContentWithTopBar(viewModel, "Статистика", scaffoldPaddingValues, toPrevious = { navController.popBackStack() }) {
                MetricsPage(viewModel, metrics)
            }
        }
        composable(
            NavRoutes.Camera.route,
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            }
        ){
            LaunchedEffect(Unit) {
                viewModel.showBottomNavBar.value = true
                viewModel.loadDevices()
            }
            Camera(
                viewModel,
                toAnalyzedImage,
                toDeviceSetup,
                true
            )
        }
        composable(
            NavRoutes.AnalyzedImage.route,
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            }
        ) {
            LaunchedEffect(Unit) {
                viewModel.showBottomNavBar.value = false
            }
            AnalyzedImage(viewModel)
        }
        composable(NavRoutes.DeviceSetup.route){
            LaunchedEffect(Unit) {
                viewModel.showBottomNavBar.value = false
            }
            DeviceSetup(viewModel, toCamera)
        }
        composable(
            NavRoutes.SessionsList.route,
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            }
        ){
            ContentWithTopBar(viewModel, "Сессии", scaffoldPaddingValues, toPrevious = { navController.popBackStack() }, enableSorting = true){
                SessionsList(viewModel, toSession)
            }
        }
        composable(
            NavRoutes.SessionPage.route + "/{sessionID}",
            arguments = listOf(navArgument("sessionID") { type = NavType.LongType }),
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            }
        ){ stackEntry ->
            val sessionID = stackEntry.arguments?.getLong("sessionID") ?: -1L
            ContentWithTopBar(viewModel, "Запись", scaffoldPaddingValues, toPrevious = { navController.popBackStack() }){
                SessionPage(viewModel, sessionID)
            }
        }
        composable(
            NavRoutes.Devices.route,
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            }
        ){
            LaunchedEffect(Unit) {
                viewModel.showBottomNavBar.value = true
            }
            ContentWithTopBar(viewModel, "Устройства", scaffoldPaddingValues) {
                DevicesList(viewModel, toDeviceSetup)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, selectedDestination: MutableState<String>){
    BoxWithConstraints() {
        val barWidth = maxWidth
        val barHeight = maxHeight * 0.12f

        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .shadow(5.dp),
            containerColor = BarColor
        ) {
            NavBarItems.BarItems.forEach { item ->
                val isSelected = selectedDestination.value == item.route
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    navController.navigate(item.route)
                                    selectedDestination.value = item.route
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = item.title,
                            tint = if(isSelected) Color.Red else Color.Gray,
                            modifier = Modifier
                                .size(barHeight * 0.25f)
                        )
                        Text(
                            text = item.title,
                            fontSize = 12.sp,
                            color = if(isSelected) Color.Red else Color.Gray
                        )
                    }
                }
            }
        }
    }

}

//            NavigationBarItem(
//                selected = isSelected,
//                icon = {
//                    Box(){
//                        Icon(
//                            painter = painterResource(item.icon),
//                            contentDescription = item.title
//                        )
//                    }
//                },
//                onClick = {
//                    navController.navigate(item.route)
//                    selectedDestination.value = item.route
//                },
//                modifier = Modifier,
//                colors = NavigationBarItemDefaults.colors(
//                    indicatorColor = Color.Transparent
//                ),
//            )