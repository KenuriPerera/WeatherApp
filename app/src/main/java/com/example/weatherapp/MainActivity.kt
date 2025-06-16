package com.example.weatherapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {

    private val apiKey = "16e2d9860d0cffb5bdc0b8c608db9c06"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherScreen()
                }
            }
        }
    }

    @Composable
    fun WeatherScreen() {
        var cityInput by remember { mutableStateOf(TextFieldValue("")) }
        var weatherData by remember { mutableStateOf<WeatherResponse?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0288D1),
                            Color(0xFFBBDEFB)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(8.dp, MaterialTheme.shapes.large)
                        .clip(MaterialTheme.shapes.large),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xEEFFFFFF)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedTextField(
                            value = cityInput,
                            onValueChange = { cityInput = it },
                            label = { Text("Enter city name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        AnimatedContent(
                            targetState = isLoading,
                            label = "loading_transition", // Added for Animation Preview
                            transitionSpec = {
                                ContentTransform(
                                    targetContentEnter = fadeIn(animationSpec = tween(300)),
                                    initialContentExit = fadeOut(animationSpec = tween(300))
                                )
                            }
                        ) { loading ->
                            if (loading) {
                                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                            } else {
                                Button(
                                    onClick = {
                                        val city = cityInput.text.trim()
                                        if (city.isEmpty()) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Please enter a city",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            coroutineScope.launch {
                                                isLoading = true
                                                errorMessage = null
                                                fetchWeather(city, { newError -> errorMessage = newError }) { response ->
                                                    weatherData = response
                                                }
                                                isLoading = false
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Get Weather")
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        fetchWeather(cityInput.text.trim(), { newError -> errorMessage = newError }) { response ->
                                            weatherData = response
                                        }
                                        isLoading = false
                                    }
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                weatherData?.let { data ->
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedVisibility(
                        visible = weatherData != null,
                        enter = fadeIn(animationSpec = tween(500)),
                        exit = fadeOut(animationSpec = tween(500))
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .shadow(8.dp, MaterialTheme.shapes.large)
                                .clip(MaterialTheme.shapes.large),
                            colors = CardDefaults.cardColors(
                                containerColor = getWeatherBackground(data.weather[0].description)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = data.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        "https://openweathermap.org/img/wn/${data.weather[0].icon}@4x.png"
                                    ),
                                    contentDescription = "Weather Icon",
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0x33FFFFFF))
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "${data.main.temp} °C",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = data.weather[0].description.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun getWeatherBackground(description: String): Color {
        return when {
            description.contains("clear", ignoreCase = true) -> Color(0xFF0288D1)
            description.contains("cloud", ignoreCase = true) -> Color(0xFF546E7A)
            description.contains("rain", ignoreCase = true) -> Color(0xFF01579B)
            description.contains("snow", ignoreCase = true) -> Color(0xFF78909C)
            else -> Color(0xFF0288D1)
        }
    }

    private suspend fun fetchWeather(
        city: String,
        updateError: (String?) -> Unit,
        onSuccess: (WeatherResponse) -> Unit
    ) {
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getWeather(city, apiKey, "metric")
            }
            withContext(Dispatchers.Main) {
                onSuccess(response)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                updateError(when (e.message?.lowercase() ?: "") {
                    in listOf("", "404", "city not found") -> "City not found"
                    in listOf("401", "invalid api key") -> "Invalid API key"
                    else -> "Error: ${e.message ?: "Unknown error"}"
                })
            }
        }
    }
}