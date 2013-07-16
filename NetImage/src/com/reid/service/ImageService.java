package com.reid.service;

import com.reid.utils.StreamTool;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageService {

    public static byte[] getImage(String path) throws IOException {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setReadTimeout(5000);
        conn.setRequestMethod("GET");
        InputStream inputStream = conn.getInputStream();
        return StreamTool.read(inputStream);
    }

}
