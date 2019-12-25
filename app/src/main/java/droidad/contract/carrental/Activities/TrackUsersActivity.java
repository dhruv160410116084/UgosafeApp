package droidad.contract.carrental.Activities;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import droidad.contract.carrental.Holders.RentRequestData;
import droidad.contract.carrental.R;

public class TrackUsersActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    double lat=0, lng=0;

    String customer_id;

    HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);
    Handler handler;

    ProgressDialog progressDialog;

    boolean zoom_status=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_users);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        progressDialog = new ProgressDialog(TrackUsersActivity.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        } else {
            handler = new Handler();
        }

        customer_id=getIntent().getExtras().getString("customer_id");

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(customer_id.length()>0){
                            RetrieveCustomerLocation(customer_id);
                        }

                    }
                }).start();

                handler.postDelayed(this, 10000);
            }

        }, 5000);

    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorDrawableResourceId) {
//        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_location_on_black_24dp);
//        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void RetrieveCustomerLocation(final String data) {

        new Thread(new Runnable() {
            @Override
            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        progressDialog.setTitle("Loading");
//                        progressDialog.setMessage("Please, wait...");
//                        progressDialog.setCancelable(false);
//                        progressDialog.show();
//                    }
//                });
                try {
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/location/live/"+ URLEncoder.encode(data));
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoInput(true);
                    //httpURLConnection.setDoOutput(true);
                    httpURLConnection.setConnectTimeout(10000);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");

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
                        System.out.println(jsonObject); //{"money":1000,"_id":"5dbd602e90c09f00178cec1e","name":"User2","email":"user2@gmail.com","password":"User2","isOwner":false,"__v":0}
                        System.out.println("length: "+jsonObject.length());

                        lat=(double)jsonObject.get("lan");
                        lng=(double)jsonObject.get("lon");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //progressDialog.cancel();
                                mMap.clear();
                                LatLng customer = new LatLng(lat, lng);
                                mMap.addMarker(new MarkerOptions().position(customer).title(customer_id).icon(bitmapDescriptorFromVector(TrackUsersActivity.this, R.drawable.car)));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(customer));
                                if(zoom_status){
                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(20));
                                    zoom_status=false;
                                }

                            }
                        });

                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
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
}
