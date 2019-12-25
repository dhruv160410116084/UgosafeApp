package droidad.contract.carrental.Activities;

import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.JsonReader;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import droidad.contract.carrental.R;

public class ShowCentersActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private  LatLngBounds.Builder builder;
    private LatLngBounds bounds;
    private CameraUpdate cu;

    LatLng firstMarker;
    LatLng lastMarker;

    JSONArray cast;

    SharedPreferences sharedPreferences;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_centers);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPreferences = getSharedPreferences("user_metadata", Context.MODE_PRIVATE);


        builder = new LatLngBounds.Builder();

        FetchCenters();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(this);

        progressDialog = new ProgressDialog(ShowCentersActivity.this);

    }

    public void FetchCenters() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setTitle("Loading");
                        progressDialog.setMessage("Please, wait...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });

                try {
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/user/centers");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoInput(true);
                    //httpURLConnection.setDoOutput(true);
                    httpURLConnection.setConnectTimeout(10000);
                    //httpURLConnection.setRequestProperty("Content-Type", "application/json");

                    int response_code = httpURLConnection.getResponseCode();
                    if (response_code == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = httpURLConnection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuffer stringBuffer = new StringBuffer();
                        String line = "";

                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuffer.append(line).append("\n");
                        }
                        JSONObject jsonObject = new JSONObject(stringBuffer.toString());

                        inputStream.close();
                        httpURLConnection.disconnect();
                        System.out.println("Response of Centers: "+jsonObject);


                        cast=(JSONArray)jsonObject.getJSONArray("centers");

                        System.out.println("Cast Length: "+cast.length());
                        if(cast.length()!=0){
                            for(int i=0; i<cast.length(); i++){
                                JSONObject response=cast.getJSONObject(i);
                                final String name=response.getString("name");
                                final String email=response.getString("email");
                                JSONObject location=response.getJSONObject("location");
                                String lat=location.getString("lat");
                                String lon=location.getString("lon");

                                final double latDouble=Double.parseDouble(lat);
                                final double lonDouble=Double.parseDouble(lon);

//                                MarkersList.add(new LatLng(latDouble, lonDouble));
//                                MarkerDetails.add(name+"\n"+email);

                                final int finalI = i;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();


                                        if(finalI ==0){
                                            firstMarker=new LatLng(latDouble, lonDouble);
                                        }else if((finalI+1) ==cast.length()){
                                            lastMarker=new LatLng(latDouble, lonDouble);

                                            builder.include(firstMarker); //Taking Point A (First LatLng)
                                            builder.include(lastMarker); //Taking Point B (Second LatLng)
                                            bounds = builder.build();
                                            cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);

                                            mMap.moveCamera(cu);
                                            mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 20, null);
                                        }

                                        MarkerOptions markerOptions=new MarkerOptions().position(new LatLng(latDouble, lonDouble)).title(email);
                                        //mMarkerOptions=new MarkerOptions().position(mDestination).title("Employee").icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.green_turtle_location_logo));
//                                        mMap.addMarker(mMarkerOptions);
                                        mMap.addMarker(markerOptions);

                                    }
                                });

                            }

                        }
                        else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    Toast.makeText(getApplicationContext(), "No Records Found.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }



                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "No Records Found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        //Toast.makeText(getApplicationContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
        System.out.println(marker.getTitle());
        System.out.println("LatLng: "+marker.getPosition());

        LatLng latLng=marker.getPosition();

        String position_type=getIntent().getExtras().getString("position_type");

        if(position_type.equals("Start Position")){
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("start_owner", marker.getTitle());
            editor.putString("start_lat", String.valueOf(latLng.latitude));
            editor.putString("start_lng", String.valueOf(latLng.longitude));
            editor.commit();
        }else {
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("end_owner", marker.getTitle());
            editor.putString("end_lat", String.valueOf(latLng.latitude));
            editor.putString("end_lng", String.valueOf(latLng.longitude));
            editor.commit();
        }
        Intent intent=new Intent(ShowCentersActivity.this, BookRequestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        return false;
    }
}
