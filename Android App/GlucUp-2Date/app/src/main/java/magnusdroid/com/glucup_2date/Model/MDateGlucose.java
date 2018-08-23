package magnusdroid.com.glucup_2date.Model;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import magnusdroid.com.glucup_2date.Class.Global;

/**
 * Model to connect Android App to the server. Use HtppURLConnection class to build the request and
 * add the headers with the data.
 * Get the response from the server and pass it to the controller
 */
public class MDateGlucose {
    private JSONObject jsonObject;
    private String response;
    private Global global;

    public JSONObject getDay(String document, String date, String flag) throws JSONException{

        jsonObject = new JSONObject();
        global = new Global();

        String urlServer = global.getUrl()+"DateFilterGlucose";
        Log.w("urlServer ", ""+urlServer);
        Map<String,Object> map = new LinkedHashMap<>();

        try {
            map.put("user",document);
            map.put("date",date);
            map.put("flag",flag);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : map.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), MAllGlucose.DECODE));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), MAllGlucose.DECODE));
            }
            byte[] postDataBytes = postData.toString().getBytes(MAllGlucose.DECODE);


            URL url = new URL(urlServer);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000 /* milliseconds */);
            conn.setConnectTimeout(20000 /* milliseconds */);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setRequestProperty("Accept-Charset", MAllGlucose.DECODE);
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (int c; (c = in.read()) >= 0; )
                    sb.append((char) c);
                response = sb.toString();
                jsonObject = new JSONObject(response);

                Log.w("response", "" + response);
            }else {
                jsonObject.put("status",3);
            }

        } catch (MalformedURLException e) {
            jsonObject.put("status",3);
            e.printStackTrace();
        } catch (SocketTimeoutException e){
            jsonObject.put("status",3);
        } catch (JSONException | IOException e) {
            jsonObject.put("status",3);
            e.printStackTrace();
        }

        return jsonObject;
    }
}
