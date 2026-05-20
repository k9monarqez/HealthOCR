package com.example.healthocr.pages

import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthocr.AppViewModel
import com.example.healthocr.R
import com.example.healthocr.pages.sessionHistory.timestampToString
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

enum class ExportPeriod(
    val description: String
) {
    DAY("За день"), LAST_7_DAYS("За последние 7 дней"), LAST_30_DAYS("За последние 30 дней"), ANY_PERIOD("За период...")
}

sealed class ExportPeriodData{
    data object None: ExportPeriodData()

    data class DayData(
        val day: LocalDateTime
    ): ExportPeriodData()

    data class PeriodData(
        val period: Pair<LocalDateTime, LocalDateTime>
    ): ExportPeriodData()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportPage(viewModel: AppViewModel, toBack: () -> Unit, snackbarHostState: SnackbarHostState){
    var selectedExportPeriod by remember { mutableStateOf(ExportPeriod.DAY) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
    ) {
        val pageWidth = maxWidth
        val pageHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ){
            Text("Период времени")
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(10.dp),
            ){
                val fontSize = 20.sp
                var expanded by remember { mutableStateOf(false) }
                DropdownMenuContainer(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .height(pageHeight * 0.05f),
                    { Text(selectedExportPeriod.description, fontSize = fontSize)},
                    { expanded = true }
                ) {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        val now = LocalDateTime.now()
                        DropdownMenuItem(
                            text = { Text(ExportPeriod.DAY.description, fontSize = fontSize) },
                            onClick = {
                                selectedExportPeriod = ExportPeriod.DAY
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(ExportPeriod.LAST_7_DAYS.description, fontSize = fontSize) },
                            onClick = {
                                selectedExportPeriod = ExportPeriod.LAST_7_DAYS
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(ExportPeriod.LAST_30_DAYS.description, fontSize = fontSize) },
                            onClick = {
                                selectedExportPeriod = ExportPeriod.LAST_30_DAYS
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(ExportPeriod.ANY_PERIOD.description, fontSize = fontSize) },
                            onClick = {
                                selectedExportPeriod = ExportPeriod.ANY_PERIOD
                                expanded = false
                            }
                        )
                    }
                }

                when(selectedExportPeriod){
                    ExportPeriod.DAY -> {
                        Spacer(modifier = Modifier.height(10.dp))

                        val showDatePicker = remember { mutableStateOf(false) }
                        val date = remember { mutableStateOf(LocalDateTime.now()) }
                        DropdownMenuContainer(
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .fillMaxWidth()
                                .height(pageHeight * 0.05f),
                            { Text(timestampToString(date.value).first, fontSize = fontSize) },
                            { showDatePicker.value = true }
                        ){}
                        if(showDatePicker.value){
                            DatePickerContainer(
                                showDatePicker,
                                {
                                    currentDate -> date.value = currentDate
                                    viewModel.setExportPeriodData(
                                        ExportPeriodData.DayData(date.value)
                                    )
                                }
                            )
                        }
                    }
                    ExportPeriod.LAST_7_DAYS, ExportPeriod.LAST_30_DAYS -> { }
                    ExportPeriod.ANY_PERIOD -> {
                        Spacer(modifier = Modifier.height(10.dp))

                        val showDatePicker = remember { mutableStateOf(false) }
                        val dates = remember { mutableStateOf<Pair<LocalDateTime, LocalDateTime>?>(null) }
                        DropdownMenuContainer(
                            Modifier
                                .fillMaxWidth()
                                .height(pageHeight * 0.05f),
                            {
                                Text(
                                    dates.value?.let {
                                        "${timestampToString(it.first).first} - ${timestampToString(it.second).first}"
                                    } ?: "-",
                                    fontSize = fontSize
                                )
                            },
                            { showDatePicker.value = true }
                        ){}
                        if(showDatePicker.value){
                            DateRangePickerContainer(
                                { start, end ->
                                    dates.value = Pair(start, end)
                                    viewModel.setExportPeriodData(
                                        ExportPeriodData.PeriodData(dates.value!!)
                                    )
                                },
                                showDatePicker
                            )
                        }
                    }
                }
            }
            val scope = rememberCoroutineScope()
            Button(
                onClick = {
                    val fileName = "metrics_${System.currentTimeMillis()}.csv"
                    viewModel.exportMetricsByExportPeriod(selectedExportPeriod, fileName)
                    scope.launch {
                        snackbarHostState.showSnackbar(fileName)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Экспорт")
            }
        }
    }
}

@Composable
fun DropdownMenuContainer(modifier: Modifier = Modifier, text: @Composable () -> Unit, onClick: () -> Unit, content: @Composable () -> Unit){
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(10f))
            .clickable{ onClick() }
            .border(1.dp, Color.Black, RoundedCornerShape(10f))
            .padding(start = 10.dp),
        contentAlignment = Alignment.CenterStart
    ){
        text()
        Icon(painterResource(R.drawable.arrowdown), null, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp))
    }
    content()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerContainer(showDialog: MutableState<Boolean>, onSave: (LocalDateTime) -> Unit){
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = { showDialog.value = false },
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onSave(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime())
                    }
                    showDialog.value = false
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDialog.value = false }) {
                Text("Отмена")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerContainer(
    onSave: (LocalDateTime, LocalDateTime) -> Unit,
    showDatePicker: MutableState<Boolean>
) {
    val rangePickerState = rememberDateRangePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= LocalDate.now()
                    .atTime(LocalTime.MAX)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
        }
    )

    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = rangePickerState.selectedStartDateMillis?.let{
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
                        } ?: return@TextButton
                        val end = rangePickerState.selectedEndDateMillis?.let{
                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
                        } ?: return@TextButton
                        onSave(start, end)
                        showDatePicker.value = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker.value = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DateRangePicker(state = rangePickerState)
        }
    }
}