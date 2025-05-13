package com.threadqa.lms.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для определения географического местоположения по IP-адресу
 */
@Slf4j
@Service
public class GeoLocationService {

    private final RestTemplate restTemplate;
    private final Map<String, GeoLocation> cache = new HashMap<>();

    public GeoLocationService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Получает город по IP-адресу
     */
    public String getCity(String ipAddress) {
        try {
            GeoLocation location = getGeoLocation(ipAddress);
            return location != null ? location.getCity() : "Unknown";
        } catch (Exception e) {
            log.error("Ошибка при определении города по IP: {}", ipAddress, e);
            return "Unknown";
        }
    }

    /**
     * Получает страну по IP-адресу
     */
    public String getCountry(String ipAddress) {
        try {
            GeoLocation location = getGeoLocation(ipAddress);
            return location != null ? location.getCountry() : "Unknown";
        } catch (Exception e) {
            log.error("Ошибка при определении страны по IP: {}", ipAddress, e);
            return "Unknown";
        }
    }

    /**
     * Получает полную информацию о местоположении по IP-адресу
     */
    private GeoLocation getGeoLocation(String ipAddress) {
        // Проверяем кэш
        if (cache.containsKey(ipAddress)) {
            return cache.get(ipAddress);
        }

        // Для локальных IP-адресов возвращаем заглушку
        if (isLocalIpAddress(ipAddress)) {
            GeoLocation localLocation = new GeoLocation("Local", "Local");
            cache.put(ipAddress, localLocation);
            return localLocation;
        }

        try {
            // В реальном приложении здесь был бы запрос к API геолокации
            // Например: ipstack, ipinfo.io, MaxMind GeoIP и т.д.
            // String url = "https://api.ipstack.com/" + ipAddress + "?access_key=YOUR_API_KEY";
            // GeoLocationResponse response = restTemplate.getForObject(url, GeoLocationResponse.class);
            
            // Для примера используем заглушку
            GeoLocation location = new GeoLocation("Moscow", "Russia");
            cache.put(ipAddress, location);
            return location;
        } catch (Exception e) {
            log.error("Ошибка при запросе геолокации для IP: {}", ipAddress, e);
            return null;
        }
    }

    /**
     * Проверяет, является ли IP-адрес локальным
     */
    private boolean isLocalIpAddress(String ipAddress) {
        return ipAddress.startsWith("127.") || 
               ipAddress.startsWith("192.168.") || 
               ipAddress.startsWith("10.") || 
               ipAddress.equals("0:0:0:0:0:0:0:1") ||
               ipAddress.equals("localhost");
    }

    /**
     * Внутренний класс для хранения информации о местоположении
     */
    private static class GeoLocation {
        private final String city;
        private final String country;

        public GeoLocation(String city, String country) {
            this.city = city;
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public String getCountry() {
            return country;
        }
    }
}
