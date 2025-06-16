package com.example.weatherapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        var cityName by remember { mutableStateOf("") }
        var temperature by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var iconUrl by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = cityInput,
                onValueChange = { cityInput = it },
                label = { Text("Enter city name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val city = cityInput.text.trim()
                    if (city.isEmpty()) {
                        Toast.makeText(this@MainActivity, "Please enter a city", Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            fetchWeather(city) { response ->
                                cityName = response.name
                                temperature = "${response.main.temp} °C"
                                description = response.weather[0].description.replaceFirstChar { it.uppercase() }
                                iconUrl = "https://openweathermap.org/img/wn/${response.weather[0].icon}@2x.png"
                            }
                        }
                    }
                }
            ) {
                Text("Get Weather")
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = cityName,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (iconUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(iconUrl),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(100.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = temperature,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    private suspend fun fetchWeather(city: String, onSuccess: (WeatherResponse) -> Unit) {
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getWeather(city, apiKey, "metric")
            }
            onSuccess(response)
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}