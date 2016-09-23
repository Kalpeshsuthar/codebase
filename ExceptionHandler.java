package com.woktowalk.util;

import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import com.woktowalk.R;
import com.woktowalk.api.ApiList;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Context mContext;
    private final String LINE_SEPARATOR = "\n";
    //String METHOD = "";
    String response;

    public ExceptionHandler(Context context) {
        mContext = context;
       // METHOD = method;
        response = response;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {

        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        Calendar cal = Calendar.getInstance();

        StringBuilder errorReport = new StringBuilder();
        errorReport.append("***** LOCAL CAUSE OF ERROR (" + mContext.getString(R.string.app_name) + ") Version: " + Utils.getInstance().getAppVersion() + " Date: " + cal.getTime() + " *****\n\n");
        errorReport.append("Localized Error Message: ");
        errorReport.append(exception.getLocalizedMessage());
        errorReport.append("Error Message: ");
        errorReport.append(exception.getMessage());
        errorReport.append("StackTrace");
        errorReport.append(stackTrace.toString());

        errorReport.append("\n" + "*******************************\n");
        errorReport.append(response);
        errorReport.append("\n" + "*******************************\n");

        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
        errorReport.append("Brand: ");
        errorReport.append(Build.BRAND);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Device: ");
        errorReport.append(Build.DEVICE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Model: ");
        errorReport.append(Build.MODEL);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Id: ");
        errorReport.append(Build.ID);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Product: ");
        errorReport.append(Build.PRODUCT);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("\n************ FIRMWARE ************\n");
        errorReport.append("SDK: ");
        errorReport.append(Build.VERSION.SDK);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Release: ");
        errorReport.append(Build.VERSION.RELEASE);
        errorReport.append(LINE_SEPARATOR);
        errorReport.append("Incremental: ");
        errorReport.append(Build.VERSION.INCREMENTAL);
        errorReport.append(LINE_SEPARATOR);


        try {

            URL url;
            String response = "";
            try {
                url = new URL(ApiList.EXCEPTION_HANDLING_URL);

                MyLog.v("Exception Handler","URL:" +ApiList.EXCEPTION_HANDLING_URL);

                JSONObject objParam = new JSONObject();
                objParam.put("errorText", errorReport.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(60000);
                conn.setConnectTimeout(60000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("User-Agent", "android");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("Content-Type", "application/json");

                if (Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                /*HashMap<String, String> postDataParams = new HashMap<String, String>();
                postDataParams.put("errorText",errorReport.toString());*/

                writer.write(objParam.toString());

                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

           // Debug.trace("Exception fire");
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}