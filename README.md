☁️ AtmosIQ
A Spring Boot weather intelligence service that fetches real-time weather data with multi-provider fallback, DB caching, and auto-refresh scheduling.

🚀 Features

Multi-Provider Fallback — OpenWeatherMap primary, WeatherAPI as backup
Response Validation — Invalid data (wrong temp/humidity range) triggers automatic fallback
DB Caching — Weather data stored in PostgreSQL, avoids redundant API calls
In-Memory Cache — Caffeine cache on top of DB for ultra-fast repeated requests
Auto Scheduler — Refreshes all cached cities every N minutes in the background
Swagger UI — Built-in API docs at /swagger-ui.html


🛠️ Tech Stack
LayerTechnologyFrameworkSpring Boot 4.0.6LanguageJava 21DatabasePostgreSQLHTTP ClientWebClient (WebFlux)CachingCaffeine + Spring CacheAPI DocsSpringDoc OpenAPI (Swagger)Build ToolMavenUtilitiesLombok

📁 Project Structure
src/main/java/com/xtechwala/AtmosIQ/
├── config/
│   └── WebClientConfig.java        # WebClient bean
├── controller/
│   └── WeatherController.java      # REST endpoint
├── dto/
│   └── WeatherResponse.java        # API response model
├── entity/
│   └── WeatherCache.java           # DB entity
├── repository/
│   └── WeatherCacheRepository.java # JPA repository
├── schedular/
│   └── WeatherCacheSchedular.java  # Auto-refresh scheduler
└── service/
├── WeatherClient.java          # Interface for all providers
├── OpenWeatherClient.java      # OpenWeatherMap implementation
├── WeatherApiClient.java       # WeatherAPI implementation
└── WeatherService.java         # Core business logic

⚙️ Setup & Configuration
1. Clone the repo
   bashgit clone https://github.com/xtechwala/AtmosIQ.git
   cd AtmosIQ
2. Create PostgreSQL database
   sqlCREATE DATABASE SkySyncDB;
3. Set environment variables
   bashexport DB_PASSWORD=your_postgres_password
   export OPENWEATHER_API_KEY=your_openweathermap_api_key
   export WEATHER_API=your_weatherapi_key

Get free API keys from:

OpenWeatherMap → https://openweathermap.org/api
WeatherAPI → https://www.weatherapi.com


4. Configure application.properties
   properties# PostgreSQL
   spring.datasource.url=jdbc:postgresql://localhost:5432/SkySyncDB
   spring.datasource.username=postgres
   spring.datasource.password=${DB_PASSWORD}

# OpenWeatherMap
weather.openweathermap.api-key=${OPENWEATHER_API_KEY}
weather.openweathermap.base-url=https://api.openweathermap.org/data/2.5/weather

# WeatherAPI
weather.weatherapi.api-key=${WEATHER_API}
weather.weatherapi.base-url=https://api.weatherapi.com/v1/current.json

# Scheduler interval (ms) — default 10 minutes
schedular.refresh-interval-ms=600000
5. Run the application
   bash./mvnw spring-boot:run

📡 API Endpoints
Get Weather
GET /api/weather?city={cityName}
Example:
bashcurl "http://localhost:8080/api/weather?city=Delhi"
Response:
json{
"city": "Delhi",
"temperature": 38.5,
"humidity": 45,
"windSpeed": 12.3,
"description": "clear sky",
"source": "OpenWeatherMap",
"cached": false,
"fetchedAt": "2025-06-08T14:30:00"
}

🔄 How It Works
Request → city="Delhi"
↓
In-Memory Cache hit? → YES → return instantly
↓ NO
DB Cache fresh? → YES → return from DB
↓ NO
OpenWeatherClient.fetchWeather()
↓
isValidResponse()? → YES → save to DB → return
↓ NO (invalid data or API failure)
WeatherApiClient.fetchWeather()
↓
isValidResponse()? → YES → save to DB → return
↓ NO
return null
Validation rules:

Temperature between -90°C and 60°C
Humidity between 0% and 100%
Description not null or blank


📖 Swagger UI
Once the app is running, visit:
http://localhost:8080/swagger-ui.html

🔧 Adding a New Weather Provider

Create a new class implementing WeatherClient
Annotate with @Service
Add API key and base URL in application.properties
Spring auto-injects it into the fallback chain — no other changes needed


📝 License
This project is open-source and available under the MIT License.