package org.bzk.documentserver.utils;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @Author 2023/3/1 11:28 ly
 **/
public class HttpUtils {

    public static String post(String url, String params) {

        OutputStreamWriter out = null;
        HttpURLConnection conn;

        try {
            URL uri = new URL(url);
            conn = (HttpURLConnection) uri.openConnection();
            conn.setRequestMethod(HttpMethod.POST.name());
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(120000);

            conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            conn.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            out = new OutputStreamWriter(conn.getOutputStream());
            out.write(params);
            out.flush();

            conn.connect();

            if (conn.getResponseCode() == HttpStatus.OK.value()) {
                return IOUtils.toString(conn.getInputStream());
            }

            Log.error("request " + url + " failure, code: " + conn.getResponseCode());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return "";

    }

    public static void download(String url, Saver saver) {
        URL uri;
        HttpURLConnection conn = null;
        InputStream stream;
        try {
            uri = new URL(url);
            System.out.println("download url....." +url);
            conn = (HttpURLConnection) uri.openConnection();
            conn.setConnectTimeout(5000);
            stream = conn.getInputStream();
            int statusCode = conn.getResponseCode();

            conn.connect();

            if (statusCode == HttpStatus.OK.value()) {
                saver.processing(stream);
                return;
            }

            Log.error("request " + url + " failure, code: " + conn.getResponseCode());
        } catch (Exception e) {
            Log.error("request " + url + " failure, exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }


}
