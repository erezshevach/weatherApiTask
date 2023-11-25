package com.erezShevach.weatherApiTask;

import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

import static java.time.temporal.ChronoUnit.SECONDS;


public class WeatherApiTask {

    private final static String BASE_URL = "//api.openweathermap.org/data/2.5/weather";
    private final static String API_KEY = "a6919dd2e20c5ca67bcf1c727a0b36bf";
    private final static Map<String, Map<String, String>> citiesMap = new HashMap<>();

    public static void main(String[] args) {
        citiesMap.put("Tel-Aviv", new HashMap<>(){{put("units","metric"); put("country","IL");}});
        citiesMap.put("London", new HashMap<>(){{put("units","imperial"); put("country","GB");}});
        citiesMap.put("New York", new HashMap<>(){{put("units","imperial"); put("country","US");}});

        runAllTasks("Tel-Aviv");
        runAllTasks("London");
        runAllTasks("New York");
        runAllTasks("mistake");
    }

    private static void runAllTasks(String city) {
        String units = citiesMap.get(city)!= null ? citiesMap.get(city).get("units") : "metric";
        String expectedCountry = citiesMap.get(city)!= null ? citiesMap.get(city).get("country") : "missing expected data";
        HttpResponse<String> res = callWeatherApiByCity(city, units);

        System.out.println("RESPONSE FOR " + city.toUpperCase());
        System.out.print("DATA: ");
        printResponseData(res);
        if (res.statusCode() != 200) {
            System.out.println(new JSONObject(res.body()).get("message"));
        } else {
            System.out.print("RESPONSE CODE: ");
            printResponseCode(res);
            System.out.print("response code is 200: ");
            System.out.println(isResponseCode200(res));
            System.out.print("COUNTRY: ");
            System.out.println(getResponseCountry(res));
            System.out.print("response country is as expected: ");
            System.out.println(isResponseCountryAsExpected(res, expectedCountry));
            System.out.print("TEMPERATURE (" + units + "): ");
            System.out.println(getResponseTemp(res));
        }
        System.out.println("-------------------");
    }

    private static HttpResponse<String> callWeatherApiByCity(String city, String units)  {
        HttpResponse<String> response = null;
        try {
            URI url = new URIBuilder()
                    .setScheme("http")
                    .setPath(BASE_URL)
                    .setParameter("q", city)
                    .setParameter("appid", API_KEY)
                    .setParameter("units", units)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .timeout(Duration.of(10, SECONDS))
                    .build();
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private static void printResponseData(HttpResponse<String> res) {
        System.out.println(res.body());
    }

    private static void printResponseCode(HttpResponse<String> res) {
        System.out.println(res.statusCode());
    }

    private static boolean isResponseCode200(HttpResponse<String> res) {
        return res.statusCode() == 200;
    }

    private static String  getResponseCountry(HttpResponse<String> res) {
        JSONObject resObj = new JSONObject(res.body());
        JSONObject sysObj = new JSONObject(resObj.get("sys").toString());
        return (String) sysObj.get("country");
    }

    private static boolean isResponseCountryAsExpected(HttpResponse<String> res, String expectedCountry) {
        return getResponseCountry(res).equals(expectedCountry);
    }

    private static BigDecimal getResponseTemp(HttpResponse<String> res) {
        JSONObject resObj = new JSONObject(res.body());
        JSONObject mainObj = new JSONObject(resObj.get("main").toString());
        return (BigDecimal)mainObj.get("temp");
    }

}

