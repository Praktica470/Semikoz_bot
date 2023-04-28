package org.example;

import java.util.Arrays;
import java.util.List;

public class URLToImage {
    private String IMG;

    public static String IMGConverter(String URL)
    {
        String IMG;
        final List<String> URLList = Arrays.asList(URL.split("="));
        final List<String> URLList2 = Arrays.asList(URLList.get(1).split("&"));
        IMG = "https://img.youtube.com/vi/" + URLList2.get(0) + "/0.jpg";
        return IMG;
    }
}
