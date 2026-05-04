package com.ygl.strong.utils.videocache.strong;

import com.ygl.strong.utils.LogUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

// For robust JSON parsing, include the org.json library in your project.
// Maven dependency:
// <dependency>
//     <groupId>org.json</groupId>
//     <artifactId>json</artifactId>
//     <version>20240303</version> <!-- Use the latest version -->
// </dependency>
import org.json.JSONArray;
import org.json.JSONObject;

public class BilibiliVideoFetcher {

    // Unofficial Bilibili API endpoint for fetching video playback URLs.
    // This API is subject to change without notice by Bilibili.
    private static final String BILIBILI_PLAYURL_API = "https://api.bilibili.com/x/player/playurl";
    // Default quality: 80 for 1080P. Other common values: 64 (720P), 32 (480P), 16 (360P).
    private static final int DEFAULT_QUALITY = 80;

    /**
     * Retrieves the primary video playback URL for a Bilibili video.
     * This method fetches the video stream's base URL from Bilibili's unofficial API.
     * Note: Bilibili uses DASH streams, meaning video and audio are often separate.
     * This method returns only the video component's URL for simplicity.
     *
     * @param bvid The Bilibili Video ID (e.g., "BV1uv411q7Mv").
     * @param cid  The Chapter ID / Part ID of the video.
     * @return A String containing the video playback URL, or null if retrieval fails.
     * @throws Exception If an I/O error occurs or the API response is unexpected.
     */
    public static String getBilibiliVideoPlaybackLink(String bvid, String cid) throws Exception {
        // Construct the API URL with necessary parameters for DASH format.
        // 'fnval=16' and 'fnver=0' are commonly used for DASH streams.
        String urlString = String.format("%s?bvid=%s&cid=%s&qn=%d&fnval=16&fnver=0", BILIBILI_PLAYURL_API, bvid, cid, DEFAULT_QUALITY);

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set essential request headers to mimic a browser, crucial for API access.
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        connection.setRequestProperty("Referer", "https://www.bilibili.com/");

        // For higher quality or login-restricted content, a 'SESSDATA' cookie might be needed:
        // connection.setRequestProperty("C ookie", "SESSDATA=YOUR_SESSDATA_COOKIE_V ALUE");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read the API response.
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String response = in.lines().collect(Collectors.joining());
            in.close();

            // Parse the JSON response to extract the video URL.
            JSONObject jsonResponse = new JSONObject(response);

            LogUtil.e("PreloadUrlsTask","jsonResponse:"+jsonResponse);
            // Check if the API call was successful (code 0 indicates success).
            if (jsonResponse.getInt("code") == 0) {
                JSONObject data = jsonResponse.getJSONObject("data");
                if (data.has("dash")) {
                    JSONObject dash = data.getJSONObject("dash");
                    JSONArray videoArray = dash.getJSONArray("video");

                    if (videoArray.length() > 0) {
                        // Return the baseUrl of the first available video stream.
                        // In a more advanced scenario, you might select a stream based on 'id' or other properties.
                        return videoArray.getJSONObject(0).getString("baseUrl");
                    }
                }
            }
            return null; // Return null if video URL cannot be found in the response.

        } else {
            // Log HTTP error details for debugging.
            System.err.println("HTTP GET request failed with response code: " + responseCode);
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            String errorResponse = errorReader.lines().collect(Collectors.joining());
            System.err.println("Error response: " + errorResponse);
            errorReader.close();
            return null;
        }
    }
}
