package com.natemacleod.android.steps

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import com.natemacleod.android.steps.model.MainViewModel
import com.natemacleod.android.steps.theme.WearAppTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var steps: Sensor? = null
    private var vm: MainViewModel? = null

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == steps) vm?.addStep()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onResume() {
        super.onResume()
        steps?.also { steps ->
            // register for updates
            sensorManager.registerListener(this, steps, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // sensor setup
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        steps = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        setContent {
            val owner = LocalViewModelStoreOwner.current

            // create ViewModel and send it to the UI
            owner?.let {
                val viewModel: MainViewModel = viewModel(
                    it,
                    "MainViewModel",
                    MainViewModelFactory(
                        LocalContext.current.applicationContext
                            as Application
                    )
                )
                vm = viewModel
                WearApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun WearApp(vm: MainViewModel) {
    WearAppTheme {
        // database
        val data by vm.days.observeAsState(listOf())
        val buffer by vm.buffer.observeAsState()

        // Page State
        val maxPages = 2
        var ps = rememberPagerState(0)
        var selectedPage by remember { mutableStateOf(0) }
        var finalValue by remember { mutableStateOf(0) }
        val animatedSelectedPage by animateFloatAsState(
            targetValue = selectedPage.toFloat(),
        ) {
            finalValue = it.toInt()
        }
        var pg by remember {
            mutableStateOf(
                object : PageIndicatorState {
                    override val pageOffset: Float
                        get() = animatedSelectedPage - finalValue
                    override val selectedPage: Int
                        get() = finalValue
                    override val pageCount: Int
                        get() = maxPages
                }
            )
        }

        // Picker State
        val items = listOf(
            "-10%", "-9%", "-8%", "-7%", "-6%", "-5%", "-4%", "-3%", "-2%", "-1%",
            "+0%", "+1%", "+2%", "+3%", "+4%", "+5%", "+6%", "+7%", "+8%", "+9%", "+10%"
        )

        // Clock at top of screen
        TimeText()

        // App
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            HorizontalPageIndicator(pageIndicatorState = pg)
        }
        HorizontalPager(count = maxPages, state = ps, userScrollEnabled = true) {
            selectedPage = ps.currentPage
            var state = rememberPickerState(initialNumberOfOptions = items.size,
                initiallySelectedOption = vm.prefs.getInt("percent", 10))

            when (ps.currentPage) {
                // Page 1 (default)
                0 -> Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            text = "${if (data.isEmpty()) "0"
                            else data[data.size - 1].steps + buffer!!}",
                            fontSize = 48.sp
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            text = "/ ${if (data.isEmpty()) "0"
                            else data[data.size - 1].goal} steps",
                            fontSize = 18.sp,
                        )
                    }
                }

                // Page 2
                1 -> {
                    val textStyle = MaterialTheme.typography.display1
                    if (vm.prefs.getInt("percent", 10) != state.selectedOption) {
                        vm.editor.putInt("percent", state.selectedOption).commit()
                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(0.dp, 0.dp, 0.dp, 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                textAlign = TextAlign.Center,
                                color = Color.White,
                                text = "Select Difficulty",
                                fontSize = 18.sp,
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Picker(
                                state = state,
                                modifier = Modifier.size(120.dp, 120.dp).padding(0.dp),
                                option = { item: Int ->
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        Text(
                                            text = items[item], style = textStyle,
                                            color = Color.White,
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .wrapContentSize()
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

class MainViewModelFactory(val application: Application) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(application) as T
    }
}
