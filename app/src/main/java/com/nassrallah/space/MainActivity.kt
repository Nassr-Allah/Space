package com.nassrallah.space

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nassrallah.space.ui.theme.SpaceTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt

class MainActivity : ComponentActivity(), SensorEventListener {

    private var rotation = mutableStateOf(0f)
    private var flow: Flow<Float> = flowOf(0f)
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        setContent {
            SpaceTheme {
                // A surface container using the 'background' color from the theme

                var isMoved by remember {
                    mutableStateOf(false)
                }
                val pxToMove = with(LocalDensity.current) {
                    100.dp.toPx().roundToInt()
                }

                val offset by animateIntOffsetAsState(
                    if (isMoved) IntOffset(pxToMove, pxToMove) else IntOffset.Zero
                )

                val scrollState = rememberScrollState()

                var scrollValue by remember {
                    mutableFloatStateOf(0f)
                }

                LaunchedEffect(key1 = rotation.value) {

                    if (rotation.value !in -180f..180f) return@LaunchedEffect
                    if (rotation.value <= 0f) {
                        val scrollPercentage = ((rotation.value * 0.5f) / 60) + 0.5f
                        scrollValue = (scrollState.maxValue * scrollPercentage)
                    } else {
                        val scrollPercentage = ((rotation.value * 0.5f) / 60) + 0.5f
                        scrollValue = (scrollState.maxValue * scrollPercentage)
                    }

                    scrollState.animateScrollTo(
                        value = scrollValue.toInt(),
                        animationSpec = spring(
                            stiffness = Spring.StiffnessVeryLow
                        )
                    )
                }


//                LaunchedEffect(key1 = isMoved) {
//                    if (isMoved) {
//                        scrollValue += 500
//                        scrollState.animateScrollTo(scrollValue, animationSpec = tween(durationMillis = 2000))
//                        Log.d("ImageScroll", "Scroll is in: ${scrollState.value}")
//                    }
//                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.space_2),
                            contentDescription = null,
                            contentScale = ContentScale.FillHeight,
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(scrollState, enabled = false)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val percentage = x / 9.81f
            val currentRotation = percentage * 180
            rotation.value = currentRotation
            flow = flow {
                emit(currentRotation)
            }
            Log.d("SensorRotation", "percentage: $percentage ; rotation: $currentRotation")
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

//    override fun onResume() {
//        super.onResume()
//        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
//            sensorManager.registerListener(
//                this,
//                accelerometer,
//                SensorManager.SENSOR_DELAY_NORMAL,
//                SensorManager.SENSOR_DELAY_UI
//            )
//        }
//        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
//            sensorManager.registerListener(
//                this,
//                magneticField,
//                SensorManager.SENSOR_DELAY_NORMAL,
//                SensorManager.SENSOR_DELAY_UI
//            )
//        }
//    }
//
//    override fun onSensorChanged(event: SensorEvent) {
//        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
//            updateOrientationAngles()
//            Log.d("ACCEL_SENSOR", "${orientationAngles.get(1)}")
//        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
//            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
//            updateOrientationAngles()
//            Log.d("MAGNETIC_SENSOR", "${orientationAngles.get(1)}")
//        }
//    }
//
//    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//
//    }
//
//    private fun updateOrientationAngles() {
//        SensorManager.getRotationMatrix(
//            rotationMatrix,
//            null,
//            accelerometerReading,
//            magnetometerReading
//        )
//
//        SensorManager.getOrientation(rotationMatrix, orientationAngles)
//    }

}
