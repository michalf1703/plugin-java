import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class WeatherInfoAction extends AnAction {
    private static final String API_KEY = "f3191140b440c99368f11911b8764f27";
    private static final String GEO_DB_API_URL = "http://geodb-free-service.wirefreethought.com/v1/geo/cities?hateoasMode=off";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        JSONObject weatherData;
        try {
            weatherData = fetchWeatherInfo();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        showWeatherInfoDialog(weatherData);

    }

    private JSONObject fetchWeatherInfo() throws IOException {
        int totalCount = getTotalCityCount();
        int randomOffset = new Random().nextInt(totalCount);
        String[] randomCityLocation = getRandomCityLocation(randomOffset);
        if (randomCityLocation.length == 2) {
            double latitude = Double.parseDouble(randomCityLocation[0]);
            double longitude = Double.parseDouble(randomCityLocation[1]);
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=metric&appid=" + API_KEY;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            } finally {
                connection.disconnect();
            }
            return new JSONObject(response.toString());
        } else {
            throw new IOException("Could not find location for the city.");
        }
    }

    private int getTotalCityCount() throws IOException {
        URL url = new URL(GEO_DB_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject metadata = jsonResponse.getJSONObject("metadata");
            return metadata.getInt("totalCount");
        }
    }

    private String[] getRandomCityLocation(int offset) throws IOException {
        String apiUrl = GEO_DB_API_URL + "&limit=1&offset=" + offset + "&types=CITY";
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray dataArray = jsonResponse.getJSONArray("data");
            if (!dataArray.isEmpty()) {
                JSONObject cityData = dataArray.getJSONObject(0);
                double latitude = cityData.getDouble("latitude");
                double longitude = cityData.getDouble("longitude");
                return new String[]{String.valueOf(latitude), String.valueOf(longitude)};
            }
            int newRandomOffset = new Random().nextInt(getTotalCityCount());
            return getRandomCityLocation(newRandomOffset);
        }
    }

    private void showWeatherInfoDialog(@NotNull JSONObject weatherInfo) {
        StringBuilder message = new StringBuilder();
        message.append("Weather Information:\n");
        String cityName;
        cityName = URLEncoder.encode(weatherInfo.getString("name"), StandardCharsets.UTF_8).replace("+", " ");
        double temperature = weatherInfo.getJSONObject("main").getDouble("temp");
        double humidity = weatherInfo.getJSONObject("main").getDouble("humidity");
        double pressure = weatherInfo.getJSONObject("main").getDouble("pressure");
        double feels_like = weatherInfo.getJSONObject("main").getDouble("feels_like");
        double windDeg = weatherInfo.getJSONObject("wind").getDouble("deg");
        double windSpeed = weatherInfo.getJSONObject("wind").getDouble("speed");
        String description = weatherInfo.getJSONArray("weather").getJSONObject(0).getString("description");
        message.append("City: ").append(cityName).append("\n");
        message.append("Temperature: ").append(temperature).append(" \u00B0C\n");
        message.append("Perceived Temperature: ").append(feels_like).append(" \u00B0C\n");
        message.append("Humidity: ").append(humidity).append(" %\n");
        message.append("Pressure: ").append(pressure).append(" hPa\n");
        message.append("Wind direction: ").append(windDeg).append(" \u00B0\n");
        message.append("Wind Speed: ").append(windSpeed).append(" m/s\n");
        message.append("Description: ").append(description);
        JOptionPane.showMessageDialog(null, message.toString(), "Weather Information", JOptionPane.INFORMATION_MESSAGE);
    }
}
