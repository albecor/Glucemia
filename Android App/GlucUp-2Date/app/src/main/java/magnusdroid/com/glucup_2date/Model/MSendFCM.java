package magnusdroid.com.glucup_2date.Model;

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
 * Update the Token FCM to recieve/send push notifications
 */
public class MSendFCM {

    private JSONObject jsonObject;
    private Global global;

    public JSONObject sendToken(String token, String document) throws JSONException {

        global = new Global();
        String urlServer = global.getUrl()+"FCM_Server";
        String error = "{'status':3}";
        Map<String, Object> map = new LinkedHashMap<>();

        try {
            map.put("token", token);
            map.put("document", document);

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
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            Log.w("url", "" + urlServer);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO-8859-1"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; )
                sb.append((char) c);
            String response = sb.toString();
            Log.i("rta", "" + response);
            jsonObject = new JSONObject(response);

        } catch (MalformedURLException | JSONException e) {
            e.printStackTrace();
        } catch (ConnectException e){
            jsonObject = new JSONObject(error);
            Log.d("No server", ""+jsonObject.toString());
        } catch (SocketTimeoutException e){
            jsonObject = new JSONObject(error);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

}
