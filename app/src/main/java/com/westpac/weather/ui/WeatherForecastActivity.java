package com.westpac.weather.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.westpac.weather.utils.GPSTracker;
import com.westpac.weather.R;
import com.westpac.weather.model.ServiceCallSource;
import com.westpac.weather.utils.NetworkConnectivityHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by admin on 12-Aug-16.
 */
public class WeatherForecastActivity extends Activity{

    ServiceCallSource serviceCallObj;
    private static final String SERVICE_URL = "https://api.forecast.io/forecast/3fbe393476a086378f50578aa2cf1179/";
    // GPSTracker class
    GPSTracker gps;
    double latitude;
    double longitude;
    TextView tv_weather_forecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_forecast_activity);
        tv_weather_forecast = (TextView) findViewById(R.id.tv_weather_forecast);

        // create class object
        gps = new GPSTracker(WeatherForecastActivity.this);

        // check if GPS enabled
        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            new LoadWeatherForecastInfoTask().execute();

        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    /**
     * LoadWeatherForecastInfoTask async task to call the web service and fetches the JSON response
     */
    private class LoadWeatherForecastInfoTask extends AsyncTask<String, Void, String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(WeatherForecastActivity.this);
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.setMessage("Loading...");
            pd.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            String response = null;
             try {
                 serviceCallObj = new ServiceCallSource();
                 //Appending current Latitude and Longitude to the base url
                 String url = SERVICE_URL + latitude + "," + longitude;
                 if(NetworkConnectivityHelper.isOnline(WeatherForecastActivity.this)) {
                     response = serviceCallObj.getResponseFromServiceURL(url);
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if(pd.isShowing()) {
                    pd.dismiss();
                    if (result != null) {
                        JSONObject resultJsonObject = new JSONObject(result).getJSONObject("currently");
                        tv_weather_forecast.setText("Forecast: " + resultJsonObject.optString("summary"));
                    } else {
                        Toast.makeText(WeatherForecastActivity.this, R.string.error_no_connection, Toast.LENGTH_LONG).show();
                    }
                }
           } catch (JSONException jex) {
                jex.printStackTrace();
            }
        }
    }
}
