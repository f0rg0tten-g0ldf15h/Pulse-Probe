package com.pulseprobe.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtil {

    public static String extractString(String json, String key) {
        String pattern = "\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]+)\"";
        Matcher matcher = Pattern.compile(pattern).matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static int extractInt(String json, String key, int defaultValue) {
        String pattern = "\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)";
        Matcher matcher = Pattern.compile(pattern).matcher(json);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return defaultValue;
    }

}
