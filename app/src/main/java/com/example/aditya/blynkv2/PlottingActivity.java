package com.example.aditya.blynkv2;

import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlottingActivity extends AppCompatActivity {
    public static TextView valueText;
    public static Button plotButton;
    public static LineGraphSeries<DataPoint> mSeries1;
    public static double graph2LastXValue;
    public static double timeInterval,numberOfValues;
    public static boolean clickStatus;
    private static boolean connectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plotting);
        //---mapping all the objects,Initialising Values
        valueText=(TextView)findViewById(R.id.valueText);
        plotButton=(Button)findViewById(R.id.plotButton);
        GraphView graph = (GraphView) findViewById(R.id.graph);
        mSeries1 = new LineGraphSeries<DataPoint>();
        int xMax=10;
        timeInterval=250*0.001;//ms
        numberOfValues=xMax/timeInterval;
        graph2LastXValue=0;
        clickStatus=false;
            //---GraphView parameters-----------
            graph.addSeries(mSeries1);
            graph.getViewport().setXAxisBoundsManual(true);;
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(xMax);
            graph.getGridLabelRenderer().setGridColor(Color.GREEN);
            graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
            graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
            mSeries1.setColor(Color.GREEN);
        //-------MISC--------------
        plotButton.setText("Start Plotting");
        valueText.setText("");
    }
//initially since we have not clicked, clickStatus = false
    public void plotClick(View view){
        if (!clickStatus){
            clickStatus=true;//to indicate we have clicked it
            keepOnExec();
        }
        else{
            clickStatus=false;
        }
    }

    public class httppart extends AsyncTask<Void,Void,String>{
        @Override
        protected String doInBackground(Void... params) {
            if (clickStatus)
            {
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
                    final String forecast_url = "http://blynk-cloud.com/" + "4707453111104dea98a1118249b7de34" + "/pin/v1";
                    //final String isConnected_url="http://blynk-cloud.com/" + "4707453111104dea98a1118249b7de34" + "/isHardwareConnected";
                    Uri builtURi = Uri.parse(forecast_url).buildUpon().build();
                    //Uri builtURi2 = Uri.parse(isConnected_url).buildUpon().build();
                    Log.v("URL", "URL is: " + builtURi.toString());
                    //Log.v("URL2","URL2 is: "+ builtURi2.toString());
                    URL url = new URL(builtURi.toString());
                    //URL url2= new URL(builtURi2.toString());
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
                //-------------MyPart Starts-------------
                if (forecastJsonStr!=null){
                    String substr = null;
                    int i = 0;
                    i = forecastJsonStr.length();
                    substr = forecastJsonStr.substring(2, i - 3);
                    Log.v("PlotDoIn","The substring in Background is: "+substr);
                    return substr;
                }
                else
                    return null;
            }
            else
                return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //----MainCode of OnPost-----
            if (result!=null)//implies we did get an output number
            {
                double ddata = Double.parseDouble(result);
                Log.v("PlotOnPostExecute", ""+ddata);
                valueText.setText(""+ddata);
                mSeries1.appendData(new DataPoint(graph2LastXValue, ddata), true, (int) numberOfValues);
                graph2LastXValue+=timeInterval;
                plotButton.setText("Stop Plotting");
            }
            else{
                plotButton.setText("Start Plotting");
            }
        }
    }

    public void keepOnExec(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                httppart httppartObj = new httppart();
                httppartObj.execute();
                handler.postDelayed(this,50);
            }
        },50);
    }

}