package com.devslopes.funshinedev;

import android.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.devslopes.funshinedev.model.DailyWeatherReport;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    final int PERMISSION_LOCATION = 111;

    private GoogleApiClient mGoogleApiClient;
    private ArrayList<DailyWeatherReport> reports = new ArrayList<>();
    private WeatherAdapter adapter;
    private ImageView weatherImgSmall;
    private ImageView weatherImgBig;
    private TextView todaysDate;
    private TextView temperature;
    private TextView location;
    private TextView weatherDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this,this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_list);

        adapter = new WeatherAdapter(reports);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
    }

    public void updateUI(DailyWeatherReport report) {

    }

    public void downloadWeatherData(@NonNull Location location) {
        String baseURL = "http://api.openweathermap.org/data/2.5/forecast";
        String units = "&units=imperial";
        String forecastURL = "/?lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
        String apiKey = "&APPID=2fd44235cfb54f0a821f0f7573fac2ad";

        baseURL += forecastURL + units + apiKey;

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, baseURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //Log.v("KAT", "Rez: " + response.toString());
                try {

                    JSONObject city = response.getJSONObject("city");
                    String cityName = city.getString("name");
                    String country = city.getString("country");

                    JSONArray list = response.getJSONArray("list");

                    for (int x = 0; x < 5; x++) {

                        JSONObject obj = list.getJSONObject(x);
                        JSONObject main = obj.getJSONObject("main");
                        Double currentTemp = main.getDouble("temp");
                        Double maxTemp = main.getDouble("temp_max");
                        Double minTemp = main.getDouble("temp_min");

                        JSONArray weatherList = obj.getJSONArray("weather");
                        JSONObject weather = weatherList.getJSONObject(0);
                        String weatherType = weather.getString("main");

                        String rawDate = obj.getString("dt_txt");

                        DailyWeatherReport report = new DailyWeatherReport(cityName,currentTemp.intValue(),maxTemp.intValue(),minTemp.intValue(),weatherType,country,rawDate);

                        reports.add(report);
                    }

                } catch (JSONException e) {
                    Log.v("KEY", "ERR: " + e.getLocalizedMessage());
                }

                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("KAT", "ERR: " + error.toString());
            }
        });

        Volley.newRequestQueue(this).add(jsonRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        } else {
            startLocationServices();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null)
            downloadWeatherData(location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationServices();
                } else {
                    //show a dialog saying something like, "I can't run your location dummy - you denied permission!"
                }
            }
        }
    }

    public void startLocationServices() {
        try {
            LocationRequest req = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,req,this);
        } catch (SecurityException exception) {

        }
    }

    public class WeatherViewHolder extends RecyclerView.ViewHolder {

        private ImageView weatherImg;
        private TextView weatherDescription;
        private TextView weatherDay;
        private TextView tempHigh;
        private TextView tempLow;

        public WeatherViewHolder(View itemView) {
            super(itemView);

            weatherImg = (ImageView)itemView.findViewById(R.id.weatherImg);
            weatherDescription = (TextView)itemView.findViewById(R.id.weatherDescription);
            weatherDay = (TextView)itemView.findViewById(R.id.weatherDay);
            tempHigh = (TextView)itemView.findViewById(R.id.tempHigh);
            tempLow = (TextView)itemView.findViewById(R.id.tempLow);
        }

        public void updateUI(DailyWeatherReport report) {


            Log.v("KEY", "Weather: " + report.getWeather());

            switch (report.getWeather()) {
                case DailyWeatherReport.WEATHER_TYPE_CLEAR:
                    weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.rainy_mini));;
                    break;
                default:
                    weatherImg.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
            }

            weatherDescription.setText(report.getWeather());
            weatherDay.setText(report.getDateString());
            tempHigh.setText(Integer.toString(report.getMaxTemp()));
            tempLow.setText(Integer.toString(report.getMinTemp()));
        }
    }

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherViewHolder> {

        private ArrayList<DailyWeatherReport> list;

        public WeatherAdapter(ArrayList<DailyWeatherReport> list) {
            this.list = list;
        }

        @Override
        public void onBindViewHolder(WeatherViewHolder holder, int position) {
            DailyWeatherReport report = list.get(position);
            holder.updateUI(report);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public WeatherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_weather, parent, false);
            return new WeatherViewHolder(card);
        }
    }
}
