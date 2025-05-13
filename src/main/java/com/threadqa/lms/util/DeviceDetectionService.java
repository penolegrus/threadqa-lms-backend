package com.threadqa.lms.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сервис для определения типа устройства, браузера и операционной системы по User-Agent
 */
@Slf4j
@Service
public class DeviceDetectionService {

    private final Map<String, String> deviceCache = new HashMap<>();
    private final Map<String, String> browserCache = new HashMap<>();
    private final Map<String, String> osCache = new HashMap<>();

    /**
     * Получает тип устройства по User-Agent
     */
    public String getDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        // Проверяем кэш
        if (deviceCache.containsKey(userAgent)) {
            return deviceCache.get(userAgent);
        }

        String deviceType;
        String userAgentLower = userAgent.toLowerCase();

        if (userAgentLower.contains("mobile") || 
            userAgentLower.contains("android") && !userAgentLower.contains("tablet") ||
            userAgentLower.contains("iphone")) {
            deviceType = "Mobile";
        } else if (userAgentLower.contains("tablet") || 
                  userAgentLower.contains("ipad") || 
                  userAgentLower.contains("kindle")) {
            deviceType = "Tablet";
        } else {
            deviceType = "Desktop";
        }

        // Сохраняем в кэш
        deviceCache.put(userAgent, deviceType);
        return deviceType;
    }

    /**
     * Получает название браузера по User-Agent
     */
    public String getBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        // Проверяем кэш
        if (browserCache.containsKey(userAgent)) {
            return browserCache.get(userAgent);
        }

        String browser;
        String userAgentLower = userAgent.toLowerCase();

        if (userAgentLower.contains("firefox")) {
            browser = "Firefox";
        } else if (userAgentLower.contains("chrome") && !userAgentLower.contains("edg")) {
            browser = "Chrome";
        } else if (userAgentLower.contains("safari") && !userAgentLower.contains("chrome")) {
            browser = "Safari";
        } else if (userAgentLower.contains("edg")) {
            browser = "Edge";
        } else if (userAgentLower.contains("opera") || userAgentLower.contains("opr")) {
            browser = "Opera";
        } else if (userAgentLower.contains("msie") || userAgentLower.contains("trident")) {
            browser = "Internet Explorer";
        } else {
            browser = "Unknown";
        }

        // Получаем версию браузера
        String version = getBrowserVersion(userAgent, browser);
        if (!version.isEmpty()) {
            browser += " " + version;
        }

        // Сохраняем в кэш
        browserCache.put(userAgent, browser);
        return browser;
    }

    /**
     * Получает версию браузера
     */
    private String getBrowserVersion(String userAgent, String browser) {
        try {
            Pattern pattern = null;
            
            switch (browser) {
                case "Chrome":
                    pattern = Pattern.compile("Chrome/(\\d+\\.\\d+)");
                    break;
                case "Firefox":
                    pattern = Pattern.compile("Firefox/(\\d+\\.\\d+)");
                    break;
                case "Safari":
                    pattern = Pattern.compile("Version/(\\d+\\.\\d+)");
                    break;
                case "Edge":
                    pattern = Pattern.compile("Edg/(\\d+\\.\\d+)");
                    break;
                case "Opera":
                    pattern = Pattern.compile("OPR/(\\d+\\.\\d+)");
                    if (pattern.matcher(userAgent).find()) {
                        break;
                    }
                    pattern = Pattern.compile("Opera/(\\d+\\.\\d+)");
                    break;
                case "Internet Explorer":
                    pattern = Pattern.compile("MSIE (\\d+\\.\\d+)");
                    if (pattern.matcher(userAgent).find()) {
                        break;
                    }
                    pattern = Pattern.compile("rv:(\\d+\\.\\d+)");
                    break;
            }
            
            if (pattern != null) {
                Matcher matcher = pattern.matcher(userAgent);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка при определении версии браузера: {}", userAgent, e);
        }
        
        return "";
    }

    /**
     * Получает название операционной системы по User-Agent
     */
    public String getOperatingSystem(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown";
        }

        // Проверяем кэш
        if (osCache.containsKey(userAgent)) {
            return osCache.get(userAgent);
        }

        String os;
        String userAgentLower = userAgent.toLowerCase();

        if (userAgentLower.contains("windows")) {
            os = "Windows";
            if (userAgentLower.contains("windows nt 10")) {
                os = "Windows 10";
            } else if (userAgentLower.contains("windows nt 6.3")) {
                os = "Windows 8.1";
            } else if (userAgentLower.contains("windows nt 6.2")) {
                os = "Windows 8";
            } else if (userAgentLower.contains("windows nt 6.1")) {
                os = "Windows 7";
            }
        } else if (userAgentLower.contains("macintosh") || userAgentLower.contains("mac os x")) {
            os = "macOS";
        } else if (userAgentLower.contains("linux") && !userAgentLower.contains("android")) {
            os = "Linux";
        } else if (userAgentLower.contains("android")) {
            os = "Android";
        } else if (userAgentLower.contains("iphone") || userAgentLower.contains("ipad") || userAgentLower.contains("ipod")) {
            os = "iOS";
        } else {
            os = "Unknown";
        }

        // Сохраняем в кэш
        osCache.put(userAgent, os);
        return os;
    }
}
