package droidad.contract.carrental.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

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

import droidad.contract.carrental.R;
import droidad.contract.carrental.ServerOperations.ServerOperationFunctions;
import droidad.contract.carrental.Services.FindCurrentLocationService;

public class SignUpActivity extends AppCompatActivity {

    RelativeLayout MainLayout;
    TextInputLayout NameTextInput, EmailIdTextInput, PasswordTextInput;
    RadioGroup UserTyperRadioGroup;
    Button SignUpButton, ShareLocationButton;

    RadioButton radioButton;

    ProgressDialog progressDialog;
    Spinner citiesSpinner;



    public static String ActivityForLocation;

    static SignUpActivity instance;
    LocationRequest locationRequest;

    String CurrentLattitude = "", CurrentLongitude = "";

    FusedLocationProviderClient fusedLocationProviderClient;

    public static SignUpActivity getInstance() {
        return instance;
    }

    ServerOperationFunctions serverOperationFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Sign Up");

        MainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        NameTextInput = (TextInputLayout) findViewById(R.id.name_textinput);
        EmailIdTextInput = (TextInputLayout) findViewById(R.id.email_id_textinput);
        PasswordTextInput = (TextInputLayout) findViewById(R.id.password_textinput);
        UserTyperRadioGroup = (RadioGroup) findViewById(R.id.user_typer_radiogroup);
        SignUpButton = (Button) findViewById(R.id.signup_button);
        ShareLocationButton = (Button) findViewById(R.id.share_location_button);
        citiesSpinner = (Spinner)findViewById(R.id.cities);

        String [] cities = {"Mumbai","Delhi" ,"Ahmedabad","Baroda","Nadiad","Banglore","Gandhinagar"};

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,cities);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citiesSpinner.setAdapter(aa);
        citiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                System.out.println("city"+ citiesSpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        progressDialog = new ProgressDialog(SignUpActivity.this);

        instance = this;

        ActivityForLocation = "SignUpActivity";

        serverOperationFunctions = new ServerOperationFunctions();

        UserTyperRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                int selectedId = UserTyperRadioGroup.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                radioButton = (RadioButton) findViewById(selectedId);
                String selectedUserType = radioButton.getText().toString();
                System.out.println("Selected: " + selectedUserType);
                if (selectedUserType.equals("Car Owner")) {
                    ShareLocationButton.setVisibility(View.VISIBLE);
                } else {
                    ShareLocationButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedId = UserTyperRadioGroup.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                radioButton = (RadioButton) findViewById(selectedId);
                String selectedUserType = radioButton.getText().toString();

                if (selectedUserType.length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please, select user type.", Toast.LENGTH_SHORT).show();
                } else if (NameTextInput.getEditText().getText().length() == 0) {
                    NameTextInput.getEditText().setError("Please, insert Name");
                } else if (EmailIdTextInput.getEditText().getText().length() == 0) {
                    EmailIdTextInput.getEditText().setError("Please, insert Email Id");
                } else if (PasswordTextInput.getEditText().getText().length() == 0) {
                    PasswordTextInput.getEditText().setError("Please, insert Password");
                } else {
                    //SignUp user

                    JSONObject signUpDetails = new JSONObject();
                    JSONObject locationDetails = new JSONObject();


                    try {
                        signUpDetails.put("name", NameTextInput.getEditText().getText());
                        signUpDetails.put("email", EmailIdTextInput.getEditText().getText());
                        signUpDetails.put("password", PasswordTextInput.getEditText().getText());
                        signUpDetails.put("city",citiesSpinner.getSelectedItem());

                        if (selectedUserType.equals("Car Owner")) {
                            if (CurrentLattitude.length() == 0 || CurrentLongitude.length() == 0) {
                                Toast.makeText(getApplicationContext(), "Please click Share your Location Button to provide your Address.", Toast.LENGTH_SHORT).show();
                            } else {

                                locationDetails.put("lat", CurrentLattitude);
                                locationDetails.put("lon", CurrentLongitude);

                            }
                            signUpDetails.put("isOwner", true);
                            signUpDetails.put("location", locationDetails);
                        } else {
                            signUpDetails.put("isOwner", false);
                        }
                        System.out.println("Input: "+signUpDetails);
                        RegisterUser(signUpDetails);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


//                    Intent intent=new Intent(SignUpActivity.this, LogInActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
                }


            }
        });

        ShareLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dexter.withActivity(SignUpActivity.this)
                        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                statusCheck();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                Toast.makeText(getApplicationContext(), "You must allow us to track your Address", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();
            }
        });

    }

    private void updateLocation() {

        SignUpButton.setEnabled(false);
        SignUpButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.accent_disable_curve));

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
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
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
        CurrentLattitude = lat;
        CurrentLongitude = longi;

        System.out.println("Current Lattitude: " + CurrentLattitude);
        System.out.println("Current Longitude: " + CurrentLongitude);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.cancel();
            }
        });


        SignUpButton.setEnabled(true);
        SignUpButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.accent_curve));
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

    public void RegisterUser(final JSONObject data) {

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
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/user/register");
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
                        System.out.println(jsonObject); //{"money":1000,"_id":"5dbd602e90c09f00178cec1e","name":"User2","email":"user2@gmail.com","password":"User2","isOwner":false,"__v":0}

                        String name=(String)jsonObject.get("name");
                        System.out.println("name: "+name);
                        if(name.equals("MongoError")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    Toast.makeText(getApplicationContext(), "You already have Account.", Toast.LENGTH_LONG).show();
                                    Intent intent=new Intent(SignUpActivity.this, LogInActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });

                        }else {
                            try{
                                String _id=(String)jsonObject.get("_id");
                                String email=(String)jsonObject.get("email");
                                boolean isOwner=(boolean)jsonObject.get("isOwner");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();
                                    }
                                });

                                Intent intent=new Intent(SignUpActivity.this, LogInActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            }catch (Exception e){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();
                                        Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }





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
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
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
