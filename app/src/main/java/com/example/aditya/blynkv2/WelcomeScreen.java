package com.example.aditya.blynkv2;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WelcomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
    }

    public void connector(View view){
        isConnected isConnectedObj = new isConnected();
        isConnectedObj.execute();
    }

    public class isConnected extends AsyncTask<Void,Void,Character>{
        @Override
        protected Character doInBackground(Void... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String forecast_url = "http://blynk-cloud.com/" + "4707453111104dea98a1118249b7de34" + "/isHardwareConnected";
                Uri builtURi = Uri.parse(forecast_url).buildUpon().build();
                Log.v("URL", "URL is: " + builtURi.toString());
                URL url = new URL(builtURi.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            Log.v("Tag", "Forecast JSON String" + forecastJsonStr);
            if (forecastJsonStr != null) {
                String substring =forecastJsonStr.substring(0,1);
                Log.v("Tag","The substring is: "+substring);
                return substring.charAt(0);
            }
            else
                return null;
        }

        @Override
        protected void onPostExecute(Character character) {
            Log.v("WelcomeOnPost","The Obtained character is: "+ character);
            super.onPostExecute(character);
            if (character=='t'){
                intention();
            }
            else{
                toaster();
            }
        }
    }

    public void intention(){
        Intent intent=new Intent(this,PlottingActivity.class);
        startActivity(intent);
    }

    public void toaster(){
        Toast.makeText(this,"The board is not connected",Toast.LENGTH_LONG).show();
    }
}
