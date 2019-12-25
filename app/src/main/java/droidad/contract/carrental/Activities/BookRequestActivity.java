package droidad.contract.carrental.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncAdapterType;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import androidx.core.content.ContextCompat;
import droidad.contract.carrental.R;
import droidad.contract.carrental.Services.FindCurrentLocationService;

import static droidad.contract.carrental.Activities.SignUpActivity.ActivityForLocation;

public class BookRequestActivity extends AppCompatActivity {

    TextView StartDateTextView, EndDateTextView, StartPositionTextView, EndPositionTextView, CostTextView;
    Button StartPositionButton, EndPositionButton, SendRequestButton, SelectCarButton;
    ImageView StartDateButton, EndDateButton;
    CheckBox As1CheckBox, As2CheckBox, As3CheckBox, As4CheckBox, As5CheckBox;
    Boolean isHomeDelivery = true;
    Boolean isDriver = true;
    Boolean SharedAs1, SharedAs2, SharedAs3, SharedAs4, SharedAs5;
    RadioButton homeDelivery, pickup, driverYes, driverNo;
    RadioGroup radioGroup, driverRadioGroup;
    RelativeLayout MainLayout;
    Spinner startCitySpinner, endCitySpinner;
    private int mYear, mMonth, mDay, PerDayCost;

    String ActivityType;

    SharedPreferences sharedPreferences;

    String CustomerId, SharedRentId, SharedStartDate, SharedEndDate, SharedCost, SharedStartOwner, SharedStartLat,
            SharedStartLng, SharedEndOwner, SharedEndLat, SharedEndLng, SharedStartCity, SharedEndCity,
            SharedCustomerLat, SharedCustomerLng;
    Boolean SharedDriver, SharedHomeDelivery;

    ProgressDialog progressDialog;

//    public static String ActivityForLocationBookRequest;

    static BookRequestActivity instance;
    LocationRequest locationRequest;

    String CurrentLattitude = "", CurrentLongitude = "";

    FusedLocationProviderClient fusedLocationProviderClient;

    public static BookRequestActivity getInstance() {
        return instance;
    }


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_request);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Book Request");

        sharedPreferences = getSharedPreferences("user_metadata", Context.MODE_PRIVATE);

        progressDialog = new ProgressDialog(BookRequestActivity.this);

        instance = this;

        ActivityForLocation = "BookRequestActivity";


        StartDateTextView = (TextView) findViewById(R.id.start_date_textview);
        EndDateTextView = (TextView) findViewById(R.id.end_date_textview);
//        StartPositionTextView=(TextView)findViewById(R.id.starting_position_spinner);
//        EndPositionTextView=(TextView)findViewById(R.id.ending_position_spinner);
        CostTextView = (TextView) findViewById(R.id.cost_textview);
        StartDateButton = (ImageView) findViewById(R.id.start_date_button);
        EndDateButton = (ImageView) findViewById(R.id.end_date_button);
        StartPositionButton = (Button) findViewById(R.id.starting_position_button);
        EndPositionButton = (Button) findViewById(R.id.ending_position_button);
        SendRequestButton = (Button) findViewById(R.id.send_request_button);
        As1CheckBox = (CheckBox) findViewById(R.id.as1_checkbox);
        As2CheckBox = (CheckBox) findViewById(R.id.as2_checkbox);
        As3CheckBox = (CheckBox) findViewById(R.id.as3_checkbox);
        As4CheckBox = (CheckBox) findViewById(R.id.as4_checkbox);
        As5CheckBox = (CheckBox) findViewById(R.id.as5_checkbox);
        MainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        homeDelivery = (RadioButton) findViewById(R.id.radio_homeDelivery);
        pickup = (RadioButton) findViewById(R.id.radio_pickup);
        driverYes = (RadioButton) findViewById(R.id.radio_driver_yes);
        driverNo = (RadioButton) findViewById(R.id.radio_driver_no);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        driverRadioGroup = (RadioGroup) findViewById(R.id.radioGroup_driver);
        startCitySpinner = (Spinner) findViewById(R.id.starting_position_spinner);
        endCitySpinner = (Spinner) findViewById(R.id.ending_position_spinner);
        SelectCarButton = (Button) findViewById(R.id.select_car_button);

        String[] cities = {"Mumbai", "Delhi", "Ahmedabad", "Baroda", "Nadiad", "Banglore", "Gandhinagar"};

        ActivityType = getIntent().getExtras().getString("activity_type");
        System.out.println("Activity Type: " + ActivityType);

        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, cities);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        startCitySpinner.setAdapter(aa);
        endCitySpinner.setAdapter(aa);


        SelectCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookRequestActivity.this, SelectCarActivity.class);
                intent.putExtra("activity_type", ActivityType);
                startActivity(intent);
            }
        });

        CustomerId = sharedPreferences.getString("user_email", " ");
        SharedRentId = sharedPreferences.getString("rent_id", "000");
        SharedStartDate = sharedPreferences.getString("start_date", "Start Date");
        SharedEndDate = sharedPreferences.getString("end_date", "End Date");
        SharedCost = sharedPreferences.getString("cost", "0");
        System.out.println("Shared Start Date: " + SharedStartDate + " Shared End Date: " + SharedEndDate + " Shared Cost: " + SharedCost);
        SharedStartOwner = sharedPreferences.getString("start_owner", "Not Selected");//
        SharedStartLat = sharedPreferences.getString("start_lat", "0.0");//
        SharedStartLng = sharedPreferences.getString("start_lng", "0.0");//
        SharedEndOwner = sharedPreferences.getString("end_owner", "Not Selected");//
        SharedEndLat = sharedPreferences.getString("end_lat", "0.0");//
        SharedEndLng = sharedPreferences.getString("end_lng", "0.0");//
        SharedStartCity = sharedPreferences.getString("start_city", "Mumbai");
        SharedEndCity = sharedPreferences.getString("end_city", "Mumbai");
        System.out.println("SharedEndCity: "+ SharedEndCity);
        SharedCustomerLat = sharedPreferences.getString("customer_lat", "0");
        SharedCustomerLng = sharedPreferences.getString("customer_lng", "0");

        SharedDriver = sharedPreferences.getBoolean("driver", true);
        SharedHomeDelivery = sharedPreferences.getBoolean("home_delivery", true);
        SharedAs1 = sharedPreferences.getBoolean("as1", false);
        SharedAs2 = sharedPreferences.getBoolean("as2", false);
        SharedAs3 = sharedPreferences.getBoolean("as3", false);
        SharedAs4 = sharedPreferences.getBoolean("as4", false);
        SharedAs5 = sharedPreferences.getBoolean("as5", false);

        if (ActivityType.equals("Book")) {

        } else {
            isDriver = SharedDriver;
            if (isDriver) {
                driverRadioGroup.check(R.id.radio_driver_yes);
            } else {
                driverRadioGroup.check(R.id.radio_driver_no);
            }
            isHomeDelivery = SharedHomeDelivery;
            if (isHomeDelivery) {
                radioGroup.check(R.id.radio_homeDelivery);
            } else {
                radioGroup.check(R.id.radio_pickup);
            }
            for (int i = 0; i < cities.length; i++) {
                if (SharedStartCity.equals(cities[i])) {
                    startCitySpinner.setSelection(i);
                }
                if (SharedEndCity.equals(cities[i])) {
                    endCitySpinner.setSelection(i);
                }
            }
            StartDateTextView.setText(SharedStartDate);
            EndDateTextView.setText(SharedEndDate);

            CurrentLattitude=SharedCustomerLat;
            CurrentLongitude=SharedCustomerLng;
            System.out.println("Customer Lat: "+CurrentLattitude);
            System.out.println("Customer Lng: "+CurrentLongitude);

//            Toast.makeText(getApplicationContext(), "Cust Lat: "+CurrentLattitude, Toast.LENGTH_SHORT).show();




            As1CheckBox.setChecked(SharedAs1);
            As2CheckBox.setChecked(SharedAs2);
            As3CheckBox.setChecked(SharedAs3);
            As4CheckBox.setChecked(SharedAs4);
            As5CheckBox.setChecked(SharedAs5);




        }


        CostTextView.setText(CostTextView.getText().charAt(0) + SharedCost);

        String Lat = sharedPreferences.getString("customer_lat", "");
        String Lng = sharedPreferences.getString("customer_lng", "");
        System.out.println("Location for radio: Lat: " + Lat + " Lan: " + Lng);
        CurrentLattitude = Lat;
        CurrentLongitude = Lng;
        Toast.makeText(getApplicationContext(), "Radio Lat: " + Lat + " Lng: " + Lng, Toast.LENGTH_SHORT).show();

        homeDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isHomeDelivery = true;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("home_delivery", isHomeDelivery);
                editor.commit();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(BookRequestActivity.this);
                alertDialog.setTitle("Location Conformation");
                alertDialog.setCancelable(false);
                alertDialog.setMessage("Click Yes for use your current location?");
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "You clicked on YES", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        statusCheck();

                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "You clicked on NO. You have to provide Location.", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        Intent intent = new Intent(BookRequestActivity.this, SelectLocationMapActivity.class);
                        intent.putExtra("activity_type", ActivityType);
                        intent.putExtra("city", startCitySpinner.getSelectedItem().toString());
                        startActivity(intent);

                    }
                });
                alertDialog.show();

            }
        });

        pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isHomeDelivery = false;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("home_delivery", isHomeDelivery);
                editor.commit();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(BookRequestActivity.this);
                alertDialog.setTitle("Location Conformation");
                alertDialog.setCancelable(false);
                alertDialog.setMessage("Click Yes for selecting Pickup location?");
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "You clicked on YES", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        Intent intent = new Intent(BookRequestActivity.this, SelectLocationMapActivity.class);
                        intent.putExtra("activity_type", ActivityType);
                        intent.putExtra("city", startCitySpinner.getSelectedItem().toString());
                        startActivity(intent);

                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "You clicked on NO. You have to provide Location.", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        statusCheck();
                    }
                });
                alertDialog.show();
            }
        });

//        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                if (R.id.radio_homeDelivery == i)
//                    isHomeDelivery = true;
//                else
//                    isHomeDelivery = false;
//
//                System.out.println("isHomeDelivery: " + isHomeDelivery);
//
//
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putBoolean("home_delivery", isHomeDelivery);
//                editor.commit();
//
//                if(isHomeDelivery){
//                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(BookRequestActivity.this);
//                    alertDialog.setTitle("Location Conformation");
//                    alertDialog.setCancelable(false);
//                    alertDialog.setMessage("Click Yes for use your current location?");
//                    alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(getApplicationContext(), "You clicked on YES", Toast.LENGTH_SHORT).show();
//                            dialog.cancel();
//                            statusCheck();
//
//                        }
//                    });
//                    alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(getApplicationContext(), "You clicked on NO. You have to provide Location.", Toast.LENGTH_SHORT).show();
//                            dialog.cancel();
//                            Intent intent = new Intent(BookRequestActivity.this, SelectLocationMapActivity.class);
//                            intent.putExtra("activity_type", ActivityType);
//                            intent.putExtra("city", startCitySpinner.getSelectedItem().toString());
//                            startActivity(intent);
//
//                        }
//                    });
//                    alertDialog.show();
//                }else {
//                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(BookRequestActivity.this);
//                    alertDialog.setTitle("Location Conformation");
//                    alertDialog.setCancelable(false);
//                    alertDialog.setMessage("Click Yes for selecting Pickup location?");
//                    alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(getApplicationContext(), "You clicked on YES", Toast.LENGTH_SHORT).show();
//                            dialog.cancel();
//                            Intent intent = new Intent(BookRequestActivity.this, SelectLocationMapActivity.class);
//                            intent.putExtra("activity_type", ActivityType);
//                            intent.putExtra("city", startCitySpinner.getSelectedItem().toString());
//                            startActivity(intent);
//
//                        }
//                    });
//                    alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(getApplicationContext(), "You clicked on NO. You have to provide Location.", Toast.LENGTH_SHORT).show();
//                            dialog.cancel();
//                            statusCheck();
//                        }
//                    });
//                    alertDialog.show();
//                }
//
//
//
//            }
//        });

        driverRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (R.id.radio_driver_yes == i)
                    isDriver = true;
                else
                    isDriver = false;

                System.out.println("isDriver: " + isDriver);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("driver", isHomeDelivery);
                editor.commit();
            }
        });


        StartDateTextView.setText(SharedStartDate);
        EndDateTextView.setText(SharedEndDate);
        //CostTextView.setText(CostTextView.getText().charAt(0)+SharedCost);
//        StartPositionTextView.setText(SharedStartOwner);
//        EndPositionTextView.setText(SharedEndOwner);
        GetPerDayCost();
        System.out.println("Per Day Cost: " + PerDayCost);


        StartDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(BookRequestActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                StartDateTextView.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("start_date", StartDateTextView.getText().toString());
                                editor.commit();

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        EndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(BookRequestActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                EndDateTextView.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");
                                try {
                                    Date date1 = myFormat.parse(StartDateTextView.getText().toString());
                                    Date date2 = myFormat.parse(EndDateTextView.getText().toString());
                                    long diff = date2.getTime() - date1.getTime();
                                    System.out.println("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS));
                                    long Days = (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)) + 1;
                                    long Cost = Days * PerDayCost;
                                    CostTextView.setText(CostTextView.getText().charAt(0) + String.valueOf(Cost));
                                    System.out.println("End Date Selected: " + EndDateTextView.getText().toString());
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("end_date", EndDateTextView.getText().toString());
                                    editor.putString("cost", String.valueOf(Cost));
                                    editor.commit();


                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        StartPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookRequestActivity.this, ShowCentersActivity.class);
                intent.putExtra("position_type", "Start Position");
                startActivity(intent);

                System.out.println("Start Owner: " + SharedStartOwner + " Start Lat: " + SharedStartLat + " Start Lng: " + SharedStartLng);

                //StartPositionTextView.setText(SharedStartOwner);


            }
        });

        EndPositionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookRequestActivity.this, ShowCentersActivity.class);
                intent.putExtra("position_type", "End Position");
                startActivity(intent);

                System.out.println("End Owner: " + SharedEndOwner + " End Lat: " + SharedEndLat + " End Lng: " + SharedEndLng);

                //EndPositionTextView.setText(SharedEndOwner);
            }
        });

        startCitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("start_city", adapterView.getSelectedItem().toString());
                editor.commit();

//                Toast.makeText(getApplicationContext(), adapterView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        endCitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("end_city", adapterView.getSelectedItem().toString());
                editor.commit();

                Toast.makeText(getApplicationContext(), adapterView.getSelectedItem().toString(), Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        As1CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("as1", b);
                //int cost=0;
                if(b){
                     SharedCost = String.valueOf(Integer.parseInt(SharedCost)+10);

                }else{
//                    cost = Integer.parseInt(CostTextView.getText().toString().substring(1))-10;
                    SharedCost = String.valueOf(Integer.parseInt(SharedCost)-10);
                }
                editor.putString("cost",SharedCost);
                CostTextView.setText(CostTextView.getText().charAt(0) + SharedCost);

                editor.commit();
            }
        });
        As2CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("as2", b);
                //int cost=0;
                if(b){
                    SharedCost = String.valueOf(Integer.parseInt(SharedCost)+10);

                }else{
//                    cost = Integer.parseInt(CostTextView.getText().toString().substring(1))-10;
                    SharedCost = String.valueOf(Integer.parseInt(SharedCost)-10);
                }
                editor.putString("cost",SharedCost);
                CostTextView.setText(CostTextView.getText().charAt(0) + SharedCost);

                editor.commit();
            }
        });
        As3CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("as3", b);
                //int cost=0;
                if(b){
                    SharedCost = String.valueOf(Integer.parseInt(SharedCost)+10);

                }else{
//                    cost = Integer.parseInt(CostTextView.getText().toString().substring(1))-10;
                    SharedCost = String.valueOf(Integer.parseInt(SharedCost)-10);
                }
                editor.putString("cost",SharedCost);
                CostTextView.setText(CostTextView.getText().charAt(0) + SharedCost);

                editor.commit();
            }
        });
        As4CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("as4", b);
                //int cost=0;
                if(b){
                    SharedCost = String.valueOf(Integer.parseInt(SharedCost)+10);

                }else{
//                    cost = Integer.parseInt(CostTextView.getText().toString().substring(1))-10;
                    SharedCost = String.valueOf(Integer.parseInt(SharedCost)-10);
                }
                editor.putString("cost",SharedCost);
                CostTextView.setText(CostTextView.getText().charAt(0) + SharedCost);

                editor.commit();
            }
        });
        As5CheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("as5", b);
                //int cost=0;
                if(b){
                    SharedCost = String.valueOf(Integer.parseInt(SharedCost)+10);

                }else{
//                    cost = Integer.parseInt(CostTextView.getText().toString().substring(1))-10;
                    SharedCost = String.valueOf(Integer.parseInt(SharedCost)-10);
                }
                editor.putString("cost",SharedCost);
                CostTextView.setText(CostTextView.getText().charAt(0) + SharedCost);

                editor.commit();
            }
        });



        SendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Send Customer Lat: "+CurrentLattitude);
                System.out.println("Send Customer Lng: "+CurrentLongitude);
                if (sharedPreferences.getString("user_email", "").equals("")) {
                    Toast.makeText(getApplicationContext(), "Please, Login or Signup", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(BookRequestActivity.this, LogInActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else if (SharedStartDate.equals("Start Date")) {
                    Toast.makeText(getApplicationContext(), "Please, Select Start Date", Toast.LENGTH_SHORT).show();
                } else if (SharedEndDate.equals("End Date")) {
                    Toast.makeText(getApplicationContext(), "Please, Select End Date", Toast.LENGTH_SHORT).show();
                } else if (CurrentLattitude.equals("") && CurrentLongitude.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please, Confirm Location", Toast.LENGTH_SHORT).show();
                }
//                else if(SharedStartOwner.equals("Not Selected")){
//                    Toast.makeText(getApplicationContext(), "Please, Select Starting Position", Toast.LENGTH_SHORT).show();
//                }
//                else if(SharedEndOwner.equals("Not Selected")){
//                    Toast.makeText(getApplicationContext(), "Please, Select Ending Position", Toast.LENGTH_SHORT).show();
//                }
                else {

                    JSONObject request_data = new JSONObject();
                    JSONObject start_position = new JSONObject();
                    JSONObject end_position = new JSONObject();
                    JSONArray equipments = new JSONArray();
                    if (As1CheckBox.isChecked()) {
                        equipments.put(As1CheckBox.getText().toString());
                    }
                    if (As2CheckBox.isChecked()) {
                        equipments.put(As2CheckBox.getText().toString());
                    }
                    if (As3CheckBox.isChecked()) {
                        equipments.put(As3CheckBox.getText().toString());
                    }
                    if (As4CheckBox.isChecked()) {
                        equipments.put(As4CheckBox.getText().toString());
                    }
                    if (As5CheckBox.isChecked()) {
                        equipments.put(As5CheckBox.getText().toString());
                    }

                    try {
                        request_data.put("rentStartDate", SharedStartDate);
                        request_data.put("rentEndDate", SharedEndDate);
                        request_data.put("customerId", CustomerId);
                        request_data.put("startCity", startCitySpinner.getSelectedItem().toString());
                        request_data.put("endCity", endCitySpinner.getSelectedItem().toString());
                        JSONObject location = new JSONObject();
                        location.put("lat", CurrentLattitude);
                        location.put("lon", CurrentLongitude);
                        request_data.put("location", location);
                        request_data.put("cost", Integer.parseInt(CostTextView.getText().toString().substring(1)));
                        request_data.put("status", "notStarted");
                        request_data.put("isRequestAccepted", "pending");
                        request_data.put("isPaid", false);
                        request_data.put("isCarpool", false);
                        request_data.put("equipments", equipments);
                        request_data.put("isHomeDelivery", isHomeDelivery);
                        request_data.put("driver", isDriver);
                        System.out.println("Request Data: " + request_data);

                        if (ActivityType.equals("Book")||ActivityType.equals("Try")) {
                            CreateRentRequest(request_data);
                        } else {

                            JSONObject selection = new JSONObject();
                            selection.put("_id", SharedRentId);
                            JSONObject update = new JSONObject();
                            update.put("rentStartDate", StartDateTextView.getText().toString());
                            update.put("rentEndDate", EndDateTextView.getText().toString());
                            update.put("customerId", CustomerId);
                            update.put("startCity", startCitySpinner.getSelectedItem().toString());
                            update.put("endCity", endCitySpinner.getSelectedItem().toString());
                            JSONObject location1 = new JSONObject();
                            location1.put("lat", CurrentLattitude);
                            location1.put("lon", CurrentLongitude);
                            update.put("location", location1);
                            update.put("cost", Integer.parseInt(SharedCost));
                            update.put("status", "notStarted");
                            update.put("isRequestAccepted", "pending");
                            update.put("isPaid", false);
                            update.put("isCarpool", false);
                            update.put("equipments", equipments);
                            update.put("isHomeDelivery", isHomeDelivery);
                            update.put("driver", isDriver);

                            JSONObject update_data = new JSONObject();
                            update_data.put("selection", selection);
                            update_data.put("update", update);

                            UpdateRequest(update_data);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });

    }

    private void updateLocation() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitle("Loading");
                progressDialog.setMessage("Please, wait...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        });

        buildLocationRequest();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
    }

    private PendingIntent getPendingIntent() {

        Intent intent = new Intent(this, FindCurrentLocationService.class);
        intent.setAction(FindCurrentLocationService.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void buildLocationRequest() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);

    }

    public void retriveLatLang(String lat, String longi) {
        System.out.println("retrive Location called");
        CurrentLattitude = lat;
        CurrentLongitude = longi;

        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("customer_lat", lat);
        editor.putString("customer_lng", longi);
        editor.commit();

        System.out.println("Current Lattitude: " + CurrentLattitude);
        System.out.println("Current Longitude: " + CurrentLongitude);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.cancel();
                Toast.makeText(getApplicationContext(), "Lat: " + CurrentLattitude + " Lng: " + CurrentLongitude, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else {
            updateLocation();

        }

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled. You need to turn on GPS, to get address from your location. Do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        updateLocation();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();

                        Snackbar snackbar = Snackbar.make(MainLayout, "You need to turn on GPS,\n to get your address from current location.", Snackbar.LENGTH_LONG);
                        snackbar.setAction("Turn On", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //CustomerLogIn(phone_no);.
                                statusCheck();
                            }
                        });
                        snackbar.show();

                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void CreateRentRequest(final JSONObject data) {

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
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/rent/create");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setConnectTimeout(10000);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    System.out.println(data.toString());

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                    bufferedWriter.write(data.toString());
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();

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
                        System.out.println(jsonObject);
                        System.out.println("length: " + jsonObject.length());

                        if (jsonObject.length() == 1) {
                            String status = (String) jsonObject.get("status");
                            if (status.equals("success")) {

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove("start_date");
                                editor.remove("end_date");
                                editor.remove("cost");
                                editor.remove("start_owner");
                                editor.remove("start_lat");
                                editor.remove("start_lng");
                                editor.remove("end_owner");
                                editor.remove("end_lat");
                                editor.remove("end_lng");
                                editor.commit();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();
                                    }
                                });

                                Intent intent = new Intent(BookRequestActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();
                                        Toast.makeText(getApplicationContext(), "There's some Error.", Toast.LENGTH_SHORT).show();
                                    }
                                });


                            }
                        } else {
//                            SharedPreferences.Editor editor = sharedPreferences.edit();
//                            editor.remove("start_date");
//                            editor.remove("end_date");
//                            editor.remove("cost");
//                            editor.remove("start_owner");
//                            editor.remove("start_lat");
//                            editor.remove("start_lng");
//                            editor.remove("end_owner");
//                            editor.remove("end_lat");
//                            editor.remove("end_lng");
//                            editor.commit();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    Toast.makeText(getApplicationContext(), "There's some Error.", Toast.LENGTH_SHORT).show();
                                }
                            });


                        }


                    }

                } catch (MalformedURLException e) {
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.remove("start_date");
//                    editor.remove("end_date");
//                    editor.remove("cost");
//                    editor.remove("start_owner");
//                    editor.remove("start_lat");
//                    editor.remove("start_lng");
//                    editor.remove("end_owner");
//                    editor.remove("end_lat");
//                    editor.remove("end_lng");
//                    editor.commit();
                    e.printStackTrace();
                    System.out.println("Error Type: "+e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (IOException e) {
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.remove("start_date");
//                    editor.remove("end_date");
//                    editor.remove("cost");
//                    editor.remove("start_owner");
//                    editor.remove("start_lat");
//                    editor.remove("start_lng");
//                    editor.remove("end_owner");
//                    editor.remove("end_lat");
//                    editor.remove("end_lng");
//                    editor.commit();
                    e.printStackTrace();
                    System.out.println("Error Type: "+e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (JSONException e) {
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.remove("start_date");
//                    editor.remove("end_date");
//                    editor.remove("cost");
//                    editor.remove("start_owner");
//                    editor.remove("start_lat");
//                    editor.remove("start_lng");
//                    editor.remove("end_owner");
//                    editor.remove("end_lat");
//                    editor.remove("end_lng");
//                    editor.commit();
                    e.printStackTrace();
                    System.out.println("Error Type: "+e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void UpdateRequest(final JSONObject data) {

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
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/rent/update");
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setConnectTimeout(10000);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");

                    System.out.println(data.toString());

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                    bufferedWriter.write(data.toString());
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();


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
                        System.out.println(jsonObject);
                        System.out.println("length: " + jsonObject.length());

                        int nModified = (int) jsonObject.get("nModified");
                        System.out.println("nModified: " + nModified);
                        if (nModified == 1) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove("start_date");
                            editor.remove("end_date");
                            editor.remove("cost");
                            editor.remove("start_owner");
                            editor.remove("start_lat");
                            editor.remove("start_lng");
                            editor.remove("end_owner");
                            editor.remove("end_lat");
                            editor.remove("end_lng");
                            editor.commit();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    Toast.makeText(getApplicationContext(), "Updated Request Successfully.", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(BookRequestActivity.this, HomeActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                }
                            });

                            System.out.println("Request Updated");

//                            Intent intent=new Intent(RentRequestDetailsActivity.this, RentRequestDetailsActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//                            intent.putExtra("request_id", request_id);
//                            startActivity(intent);
                        } else {
//                            SharedPreferences.Editor editor = sharedPreferences.edit();
//                            editor.remove("start_date");
//                            editor.remove("end_date");
//                            editor.remove("cost");
//                            editor.remove("start_owner");
//                            editor.remove("start_lat");
//                            editor.remove("start_lng");
//                            editor.remove("end_owner");
//                            editor.remove("end_lat");
//                            editor.remove("end_lng");
//                            editor.commit();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    Toast.makeText(getApplicationContext(), "Not Updated Request Successfully. Try Again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }


                    }

                } catch (MalformedURLException e) {
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.remove("start_date");
//                    editor.remove("end_date");
//                    editor.remove("cost");
//                    editor.remove("start_owner");
//                    editor.remove("start_lat");
//                    editor.remove("start_lng");
//                    editor.remove("end_owner");
//                    editor.remove("end_lat");
//                    editor.remove("end_lng");
//                    editor.commit();
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error. Try Again", Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (IOException e) {
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.remove("start_date");
//                    editor.remove("end_date");
//                    editor.remove("cost");
//                    editor.remove("start_owner");
//                    editor.remove("start_lat");
//                    editor.remove("start_lng");
//                    editor.remove("end_owner");
//                    editor.remove("end_lat");
//                    editor.remove("end_lng");
//                    editor.commit();
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error. Try Again", Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (JSONException e) {
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.remove("start_date");
//                    editor.remove("end_date");
//                    editor.remove("cost");
//                    editor.remove("start_owner");
//                    editor.remove("start_lat");
//                    editor.remove("start_lng");
//                    editor.remove("end_owner");
//                    editor.remove("end_lat");
//                    editor.remove("end_lng");
//                    editor.commit();
//                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error. Try Again", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }


    public void GetPerDayCost() {

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
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/perDayCost");
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
                        System.out.println("length: " + jsonObject.length());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.cancel();
                            }
                        });

                        PerDayCost = (int) jsonObject.get("cost");

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
                            Toast.makeText(getApplicationContext(), "There's some Error.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
