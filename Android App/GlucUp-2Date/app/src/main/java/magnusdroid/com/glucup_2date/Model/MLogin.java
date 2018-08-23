package magnusdroid.com.glucup_2date.Model;

import android.content.res.Resources;
import android.util.Log;

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
import java.util.LinkedHashMap;
import java.util.Map;

import magnusdroid.com.glucup_2date.Class.Global;

/**
 * Model to connect Android App to the server. Use HtppURLConnection class to build the request and
 * add the headers with the data.
 * Get the response from the server and pass it to the controller
 */
public class MLogin {

    private JSONObject jsonObject;
    private String response;
    private Global global;

    public JSONObject validateLogin(String document, String password) throws JSONException {

        jsonObject = new JSONObject();

        global = new Global();

        String urlServer = global.getUrl()+"LoginServlet";
        Map<String, Object> map = new LinkedHashMap<>();
        Log.w("urlServer ", ""+urlServer);

        try {
            map.put("user", document);
            map.put("password", password);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : map.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "ISO-8859-1"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "ISO-8859-1"));
            }
            byte[] postDataBytes = postData.toString().getBytes("ISO-8859-1");


            URL url = new URL(urlServer);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(10000 /* milliseconds */);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
                Log.w("Response ", ""+response);
                jsonObject = new JSONObject(response);
            }else {
                jsonObject.put("status",3);
            }

        } catch (MalformedURLException | JSONException e) {
            jsonObject.put("status",3);
            e.printStackTrace();
        } catch (ConnectException | SocketTimeoutException e){
            jsonObject.put("status",3);
        } catch (IOException e) {
            jsonObject.put("status",3);
            e.printStackTrace();
        }

        return jsonObject;
    }
}
