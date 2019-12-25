package droidad.contract.carrental.Activities;

import androidx.fragment.app.FragmentActivity;
import droidad.contract.carrental.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class SelectLocationMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Button ConfirmLocationButton;

    String Lat="";
    String Lng="";

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ConfirmLocationButton=(Button)findViewById(R.id.confirm_location_button);

        sharedPreferences = getSharedPreferences("user_metadata", Context.MODE_PRIVATE);

        ConfirmLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Lat.length()==0 && Lng.length()==0){
                    Toast.makeText(getApplicationContext(), "Select Location", Toast.LENGTH_SHORT).show();
                }else {
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putString("customer_lat", Lat);
                    editor.putString("customer_lng", Lng);
                    editor.commit();
                    String ActivityType=getIntent().getExtras().getString("activity_type");
                    Intent intent=new Intent(SelectLocationMapActivity.this, BookRequestActivity.class);
                    intent.putExtra("activity_type", ActivityType);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        HashMap cities = new HashMap();
        cities.put("Nadiad",new location(22.6916, 72.8634));
        cities.put("Mumbai",new location(19.0760, 72.8777));
        cities.put("Delhi",new location(28.7041, 77.1025));
        cities.put("Ahmedabad",new location(23.0225, 72.5714));
        cities.put("Baroda",new location(22.3072, 73.1812));
        cities.put("Banglore",new location(12.9716, 77.5946));
        cities.put("Gandhinagar",new location(23.2156, 72.6369));

        String cityIntent=getIntent().getExtras().getString("city");
        location cityObj = (location) cities.get(cityIntent);

        final LatLng city= new LatLng(cityObj.lat,cityObj.lan);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(city,15));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Car Location"));
                Lat=String.valueOf(latLng.latitude);
                Lng=String.valueOf(latLng.longitude);
            }
        });
    }
}

class  location{
    double lat;
    double lan;
    public location(double lat,double lan){
        this.lat = lat;
        this.lan = lan;
    }
}