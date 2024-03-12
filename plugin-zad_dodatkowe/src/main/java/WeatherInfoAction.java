import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherInfoAction extends AnAction {
    private static final String API_KEY = "f3191140b440c99368f11911b8764f27";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            String weatherInfo = fetchWeatherInfo();
            showWeatherInfoDialog(weatherInfo);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error occurred while fetching weather information: " + ex.getMessage(), "Weather Info Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String fetchWeatherInfo() throws IOException {
        String apiUrl = "http://api.openweathermap.org/data/2.5/weather?q=Warsaw&appid="+ API_KEY;
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

        return response.toString();
    }

    private void showWeatherInfoDialog(String weatherInfo) {
        JOptionPane.showMessageDialog(null, weatherInfo, "Weather Information", JOptionPane.INFORMATION_MESSAGE);
    }
}
