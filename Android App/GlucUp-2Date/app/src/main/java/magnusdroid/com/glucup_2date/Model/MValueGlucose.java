package magnusdroid.com.glucup_2date.Model;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import magnusdroid.com.glucup_2date.Class.Global;

/**
 * Model to connect Android App to the server. Use HtppURLConnection class to build the request and
 * add the headers with the data.
 * Get the response from the server and pass it to the controller
 */
public class MValueGlucose {


    private String Fix, Min, Max, response;
    private JSONObject jsonObject;
    private Global global;

    public JSONObject getValue(String document, String fix, String min, String max, String mUnit) throws JSONException {

        global = new Global();
        String urlServer = global.getUrl()+"ValueFilterGlucose";
        Map<String,Object> map = new LinkedHashMap<>();

        if(mUnit.equalsIgnoreCase("mg/dl")){
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.CEILING);
            if(!(fix.isEmpty()) && min.isEmpty() && max.isEmpty()){
                Double dFix = Double.parseDouble(fix);
                Fix = String.valueOf(df.format(dFix/18));
                Min = "nn";
                Max = "nn";
            }else if(!(min.isEmpty()) && fix.isEmpty() && max.isEmpty()){
                Double dMin = Double.parseDouble(min);
                Fix = "nn";
                Min = String.valueOf(df.format(dMin/18));
                Max = "nn";
            }else if(!(min.isEmpty()) && fix.isEmpty() && !(max.isEmpty())){
                Double dMin = Double.parseDouble(min);
                Double dMax = Double.parseDouble(max);
                Fix = "nn";
                Min = String.valueOf(df.format(dMin/18));
                Max = String.valueOf(df.format(dMax/18));
            }else{
                Double dMax = Double.parseDouble(max);
                Fix = "nn";
                Max = String.valueOf(df.format(dMax/18));
                Min = "nn";
            }
            /*tFix = String.valueOf(df.format(dataFix/18));
            tMin = String.valueOf(df.format(dataMin/18));
            tMax = String.valueOf(df.format(dataMax/18));*/
            //value = String.valueOf(df.format(data/18));
            //cancel = false;
        }else{
            if(!(fix.isEmpty()) && min.isEmpty() && max.isEmpty()){
                Fix = fix;
                Min = "nn";
                Max = "nn";
            }else if(!(min.isEmpty()) && fix.isEmpty() && max.isEmpty()){
                Fix = "nn";
                Min = min;
                Max = "nn";
            }else if(!(min.isEmpty()) && fix.isEmpty() && !(max.isEmpty())){
                Fix = "nn";
                Min = min;
                Max = max;
            }else{
                Fix = "nn";
                Max = max;
                Min = "nn";
            }
        }

        try {
            map.put("user",document);
            map.put("fixvalue",Fix);
            map.put("minvalue",Min);
            map.put("maxvalue",Max);

            Log.i("Datos",""+map);

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String,Object> param : map.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "ISO-8859-1"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "ISO-8859-1"));
            }
            byte[] postDataBytes = postData.toString().getBytes("ISO-8859-1");

            URL url = new URL(urlServer);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(20000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
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
            }else {
                jsonObject.put("status",3);
            }

        } catch (MalformedURLException | JSONException e) {
            jsonObject.put("status",3);
            e.printStackTrace();
        } catch (SocketTimeoutException e){
            jsonObject.put("status",3);
        }catch (IOException e) {
            jsonObject.put("status",3);
            e.printStackTrace();
        }

        return jsonObject;
    }
}