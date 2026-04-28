package com.example.swagaapp.nav

import android.annotation.SuppressLint
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.swagaapp.AppViewModel
import com.example.swagaapp.R
import com.example.swagaapp.pages.AnalyzedImage
import com.example.swagaapp.pages.DeviceSetup
import com.example.swagaapp.pages.MetricsRecords
import com.example.swagaapp.pages.Statistics
import com.example.swagaapp.pages.camera.Camera

sealed class NavRoutes(val route: String){
    object Statistics: NavRoutes("statistics")
    object DeviceSetup: NavRoutes("deviceSetup")
    object Camera: NavRoutes("camera")
    object MetricsRecords: NavRoutes("metricsRecords")
    object AnalyzedImage: NavRoutes("analyzedImage")
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
            icon = R.drawable.home,
            route = NavRoutes.Statistics.route
        ),
        BarItem(
            title = "Камера",
            icon = R.drawable.camera,
            route = NavRoutes.Camera.route
        ),
        BarItem(
            title = "Записи",
            icon = R.drawable.settings,
            route = NavRoutes.MetricsRecords.route
        )
    )
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier,
                  navController: NavHostController,
                  startDestination: String = NavRoutes.Camera.route,
                  viewModel: AppViewModel,
                  scaffoldPaddingValues: PaddingValues
)
{
    val toAnalyzedImage = { navController.navigate(NavRoutes.AnalyzedImage.route) }
    val toDeviceSetup = { navController.navigate(NavRoutes.DeviceSetup.route) }
    val toCamera = { navController.navigate(NavRoutes.Camera.route) }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ){
        composable(
            NavRoutes.Statistics.route,
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            }
        ){
            viewModel.showBottomNavBar.value = true
            Statistics(viewModel)
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
            viewModel.showBottomNavBar.value = true
            Camera(
                viewModel,
                toAnalyzedImage,
                toDeviceSetup,
                true
            )
        }
        composable(
            NavRoutes.MetricsRecords.route,
            enterTransition = {
                EnterTransition.None
            },
            exitTransition = {
                ExitTransition.None
            }
        ){
            viewModel.showBottomNavBar.value = true
            MetricsRecords(viewModel)
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
            viewModel.showBottomNavBar.value = false
            AnalyzedImage(viewModel)
        }
        composable(NavRoutes.DeviceSetup.route){
            viewModel.showBottomNavBar.value = false
            DeviceSetup(viewModel, toCamera)
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BottomNavigationBar(navController: NavHostController, selectedDestination: MutableState<String>){
    BoxWithConstraints() {
        val barWidth = maxWidth
        var barHeight = maxHeight * 0.12f

        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .shadow(5.dp),
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