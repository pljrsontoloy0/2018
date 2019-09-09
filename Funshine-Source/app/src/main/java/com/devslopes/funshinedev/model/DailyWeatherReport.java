package com.devslopes.funshinedev.model;

/**
 * Created by Devslopes on 6/16/16.
 */
public class DailyWeatherReport {

    public static final String WEATHER_TYPE_CLOUDS = "Clouds";
    public static final String WEATHER_TYPE_CLEAR = "Clear";
    public static final String WEATHER_TYPE_RAIN = "Rain";
    public static final String WEATHER_TYPE_WIND = "Wind";
    public static final String WEATHER_TYPE_SNOW =  "Snow";

    private String cityName;
    private int currentTemp;
    private int maxTemp;
    private int minTemp;
    private String weather;
    private String country;

    public String getDateString() {
        return dateString;
    }

    public String getCityName() {
        return cityName;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public String getWeather() {
        return weather;
    }

    public String getCountry() {
        return country;
    }

    private String dateString;

    public DailyWeatherReport(String cityName, int currentTemp, int maxTemp, int minTemp, String weather, String country, String dateRaw) {
        this.cityName = cityName;
        this.currentTemp = currentTemp;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.weather = weather;
        this.country = country;
        this.dateString = formatDate(dateRaw);
    }

    private String formatDate(String rawDate) {
        return "";
    }
}
