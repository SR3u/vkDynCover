package org.sr3u.vkDynCover;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class REST {

    public static String get(String url) throws IOException {
        return get(new URL(url));
    }

    public static String get(URL url) throws IOException {
        String readLine = null;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer response = new StringBuffer();
            while ((readLine = in.readLine()) != null) {
                response.append(readLine);
            }
            in.close();
            // print result
            String result = response.toString();
            System.out.println("JSON String Result " + result);
            return result;
            //GetAndPost.POSTRequest(response.toString());
        } else {
            System.out.println("GET NOT WORKED");
            throw new IOException("HTTP " + responseCode);
        }
    }


    public static String post(String url, File image) throws IOException {
        return post(new URL(url), image);
    }

    public static String post(URL url, File image) throws IOException {
        StringBuilder response_sb = new StringBuilder();
        MultipartUtility multipart = new MultipartUtility(url, "UTF-8");
        multipart.addFilePart("file", image);
        List<String> response = multipart.finish();

        for (String line : response) {
            response_sb.append(line);
        }
        return response_sb.toString();
    }


}
