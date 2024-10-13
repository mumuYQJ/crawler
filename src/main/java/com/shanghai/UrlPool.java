package com.shanghai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlPool {
    public static void main(String[] args) {
        getUrl("https://www.nipic.com/");
    }

    private static void getUrl(String baseUrl) {
        Map<String, Boolean> oldMap = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("(https?://)?[^/\\s]*");
        Matcher matcher = pattern.matcher(baseUrl);
        String oldLinkHost = "";
        if (matcher.find()) {
            oldLinkHost = matcher.group();
        }
        oldMap.put(baseUrl, false);
        oldMap = crawlerLinks(oldLinkHost, oldMap);
        for (Map.Entry<String, Boolean> mapping : oldMap.entrySet()) {
            System.out.println("链接：" + mapping.getKey());
        }
    }

    private static Map<String, Boolean> crawlerLinks(String oldLinkHost, Map<String, Boolean> oldMap) {
        Map<String, Boolean> newMap = new LinkedHashMap<>();
        String oldLink = "";
        for (Map.Entry<String, Boolean> mapping : oldMap.entrySet()) {
            if (!mapping.getValue()) {
                try {
                    oldLink = mapping.getKey();
                    System.out.println("链接：" + mapping.getKey());
                    URL url = new URL(oldLink);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    if (httpURLConnection.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        Pattern pattern = Pattern.compile("<a.*?href=[\\\"']?((https?://)?/?[^\\\"']+)[\\\"']?.*?>(.+)</a>");
                        String line = "";
                        while ((line = reader.readLine()) != null) {
                            Matcher matcher = pattern.matcher(line);
                            if (matcher.find()) {
                                String newLink = matcher.group(1).trim();
                                if (!newLink.startsWith("http")) {
                                    if (newLink.startsWith("/")) {
                                        newLink = oldLinkHost + newLink;
                                    } else {
                                        newLink = oldLinkHost + "/" + newLink;
                                    }
                                }
                                if (newLink.endsWith("/")) {
                                    newLink = newLink.substring(0, newLink.length() - 1);
                                }
                                if (!newMap.containsKey(newLink) && !oldMap.containsKey(newLink) && newLink.contains(oldLinkHost)) {
                                    newMap.put(newLink, false);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        oldMap.replace(oldLink, false, true);
        if (!newMap.isEmpty()) {
            oldMap.putAll(newMap);
            oldMap.putAll(crawlerLinks(oldLinkHost,oldMap));
        }
        return oldMap;
    }
}
