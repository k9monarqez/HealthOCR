package com.example.healthocr.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthocr.AppViewModel
import com.example.healthocr.R
import com.example.healthocr.ui.theme.BarColor

@Composable
fun ContentWithTopBar(viewModel: AppViewModel, name: String, paddingValues: PaddingValues, toPrevious: (() -> Unit)? = null, enableSorting: Boolean = false, content: @Composable (() -> Unit)){
    val sortingOrder by viewModel.sortDescending.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding())
    ) {
        val pageWidth = maxWidth
        val pageHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val topBarHeight = pageHeight * 0.125f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarHeight)
                    .background(BarColor),
                contentAlignment = Alignment.Center
            ){
                //val displayCutoutPadding = with(LocalDensity.current){ WindowInsets.displayCutout.asPaddingValues().calculateTopPadding().toPx() }
                BoxWithConstraints(
                    modifier = Modifier
                        .displayCutoutPadding()
                        .fillMaxWidth()
                        .height(topBarHeight * 0.5f)
                        .padding(start = 10.dp, end = 10.dp),
                    contentAlignment = Alignment.Center
                ){
                    val accessibleBarHeight = maxHeight
                    toPrevious?.let { toPrevious ->
                        IconButton(
                            onClick = { toPrevious() },
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .align(Alignment.CenterStart),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.back),
                                contentDescription = "go back",
                                tint = Color.Black,
                                modifier = Modifier
                                    .scale(1.5f)
                            )
                        }
                    }

                    Text(
                        name,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    if(enableSorting){
                        IconButton(
                            onClick = { viewModel.flipSortingOrder() },
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .align(Alignment.CenterEnd),
                        ) {
                            val sortIcon = if(sortingOrder) R.drawable.desc else R.drawable.asc
                            Icon(
                                painter = painterResource(sortIcon),
                                contentDescription = "sort",
                                tint = Color.Black,
                                modifier = Modifier
                                    .scale(1.5f)
                            )
                        }
                    }
                }
            }
            content()
        }
    }
}