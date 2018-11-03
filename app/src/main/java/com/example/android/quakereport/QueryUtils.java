package com.example.android.quakereport;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 * 這個類的作用像工具箱，主要是用來要求和接收網路數據。包括抓取地震數據(fetchEarthquakeData)、創建網址(createUrl)、
 * 建立連線(makeHttpRequest)、讀取數據串(readFromStream)和擷取JASON(extractFeatureFromJson)方法
 */
public final class QueryUtils {

    /** Tag for the log messages */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();  //取得這個類的簡稱(即QueryUtils)並帶入LOG_TAG

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the USGS dataset and return a list of {@link Earthquake} objects.
     *
     * Add in the fetchEarthquakeData() helper method that ties all the steps together - creating a URL, sending the request, processing the response.
     * Since this is the only “public” QueryUtils method that the EarthquakeAsyncTask needs to interact with, make all other helper methods in QueryUtils “private”.
     * 這個方法是要用來打包下面的輔助方法(helper methods)
     */
    public static List<Earthquake> fetchEarthquakeData(String requestUrl) {    //方法中的argument可以自行命名(requestUrl)
        // Create URL object。利用下面的createUrl方法來創建網址
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Earthquake}s
        List<Earthquake> earthquakes = extractFeatureFromJson(jsonResponse);

        // Return the list of {@link Earthquake}s
        return earthquakes;
    }

    /**
     * Returns new URL object from the given string URL.
     * 這個輔助方法是要用來創建網址
     */
    private static URL createUrl(String stringUrl) {     //方法中的argument可以自行命名(stringUrl)
        URL url = null;   //將網址初始化為空
        try {    //網址有可能會有問題，所以透過try/catch把網址的錯誤狀況都封裝到MalformedURLException，並記錄錯誤訊息:"QueryUtils, Problem building the URL"
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;   //把網址提交出去
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     * 這個輔助方法是要用來建立連線
     */
    private static String makeHttpRequest(URL url) throws IOException {   //建立連線可能會有連線失敗(回傳碼非200)的狀況，所以要丟出IOException來封裝錯誤訊息以免App當掉
        String jsonResponse = "";  //宣告jsonResponse字符並初始化為空白文字

        // If the URL is null, then return early. 若沒有網址，就別浪費時間執行下面的程式碼的，直接提交上面初始化的空白文字出來
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;  //宣告urlConnection並初始化為空，準備接著用來建立連線
        InputStream inputStream = null;          //宣告inputStream並初始化為空，準備接著用來接收數據
        try {
            urlConnection = (HttpURLConnection) url.openConnection();  //為網址開啟連線渠道，將此連線命名為urlConnection
            urlConnection.setReadTimeout(10000 /* milliseconds */);    //為此連線設置1萬毫秒的讀取超時限制
            urlConnection.setConnectTimeout(15000 /* milliseconds */); //為此連線設置1.5萬毫秒的連線超時限制
            urlConnection.setRequestMethod("GET");                     //為此連線設置要求數據的方式為GET()
            urlConnection.connect();                                   //啟動連線

            // If the request was successful (response code 200), then read the input stream and parse the response.
            //若回傳碼為200代表連線成功，則開始讀取與解析數據
            if (urlConnection.getResponseCode() == 200) {       //若此連線的回傳碼為200
                inputStream = urlConnection.getInputStream();   //就透過此連線接收數據，將收到的數據內容帶入前面宣告過的inputStream
                jsonResponse = readFromStream(inputStream);     //透過下面另外寫的readFromStream輔助方法來讀取該inputStream，並帶入前面宣告過的jsonResponse。
            } else {                                            //若回傳碼不是200代表連線有問題，則紀錄下錯誤訊息:"QueryUtils, Error response code: 200"
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {                               //抓錯誤。所有的數據輸入與輸出的錯誤則透過IOException記錄錯誤訊息:"QueryUtils, Problem retrieving the earthquake JSON results., e)
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {                                             //不論有無錯誤，最終必定執行的程式碼
            if (urlConnection != null) {                        //前面的動作都已完成(完成建立連線、讀取解數據與抓錯)之後，若還在連線(若此連線不是空)，就執行斷線
                urlConnection.disconnect();
            }
            if (inputStream != null) {                          //前面的動作都已完成(完成建立連線、讀取解數據與抓錯)之後，若還有數據(若數據不是空)，就關閉(清空)數據
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies that an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;                                    //以上動作都完成後，提交jsonResponse(透過連線取得的數據內容)出來
    }

    /**
     * Convert the {@link InputStream} into a String which contains the whole JSON response from the server.
     * 這個輔助方法是要用來讀取數據
     */
    private static String readFromStream(InputStream inputStream) throws IOException {  //讀取或輸出資訊時可能會發生錯誤，所以要丟出IOException來封裝錯誤訊息以免App當掉
        StringBuilder output = new StringBuilder();   //創建一個名叫output的StringBuilder
        if (inputStream != null) {                    //若有輸入的數據(inputStream不是空的)
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));  //則創建讀取數據的inputStreamReader，帶入輸入的數據並設定字元編碼為UTF-8
            BufferedReader reader = new BufferedReader(inputStreamReader);  //創建讀取數據的BufferedReader，命名為reader，並把inputStreamReader封裝進BufferedReader。這一行程式就只是宣告一個緩衝區來存放輸入的數據
            String line = reader.readLine();    //讀取緩衝區(reader)的其中一行，將讀取到的資訊命名為line字符
            while (line != null) {              //若有讀取到資訊(line字符不是空的)
                output.append(line);            //就把讀取到的line字符添加在所輸出資訊的後面
                line = reader.readLine();       //然後再去讀取緩衝區(reader)的下一行，更新line字符
            }                                   //因為數據很多，不知會需要執行append幾次，所以會使用while loop無限次執行。若知道是有限的次數就可以使用for loop (for how many given times)
        }
        return output.toString();               //將輸出的資訊轉換成字符String後提交出去
    }

    /**
     * Return a list of {@link Earthquake} objects that has been built up from parsing the given JSON response.
     * 這個輔助方法是要用來解析API
     */
    private static List<Earthquake> extractFeatureFromJson(String earthquakeJSON) {    //方法中的argument可以自行命名(earthquakeJSON)
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(earthquakeJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding earthquakes to
        List<Earthquake> earthquakes = new ArrayList<>();    //創建名為earthquakes的ArrayList，沿用自定義的Earthquake類。將屬性定為List是方便日後有需要時可以隨意把ArrayList更換成LinkedList或其他。

        // Try to parse the JSON response string. If there's a problem with the way the JSON is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(earthquakeJSON);

            // Extract the JSONArray associated with the key called "features", which represents a list of features (or earthquakes).
            JSONArray earthquakeArray = baseJsonResponse.getJSONArray("features");

            // For each earthquake in the earthquakeArray, create an {@link Earthquake} object
            for (int i = 0; i < earthquakeArray.length(); i++) {

                // Get a single earthquake at position i within the list of earthquakes
                JSONObject currentEarthquake = earthquakeArray.getJSONObject(i);

                // For a given earthquake, extract the JSONObject associated with the key called "properties",
                // which represents a list of all properties for that earthquake.
                JSONObject properties = currentEarthquake.getJSONObject("properties");

                // Extract the value for the key called "mag" as a Double value
                double magnitude = properties.getDouble("mag");

                // Extract the value for the key called "place"
                String location = properties.getString("place");

                // Extract the value for the key called "time"
                long time = properties.getLong("time");

                // Extract the value for the key called "url"
                String url = properties.getString("url");

                // Create a new {@link Earthquake} object called earthquake and extending from the Earthquake Class,
                // with the magnitude, location, time, and url from the JSON response.
                Earthquake earthquake = new Earthquake(magnitude, location, time, url);

                // Add the new {@link Earthquake} to the list of earthquakes.
                earthquakes.add(earthquake);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }

}