package droidad.contract.carrental.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import droidad.contract.carrental.Adapters.RentRequestAdapter;
import droidad.contract.carrental.Holders.RentRequestData;
import droidad.contract.carrental.R;
import droidad.contract.carrental.Services.ShareLocatioService;

public class HomeActivity extends AppCompatActivity {

    BottomAppBar bottomAppBar;
    FloatingActionButton PanicButton;
    //FloatingActionButton AddRequestButton;
    SwipeRefreshLayout swipeRefreshLayout;
    ListView RentRequestListView;
    TextView CurrentTextView, HistoryTextView, NoRecordTextView;

    RentRequestAdapter rentRequestAdapter;

    SharedPreferences sharedPreferences;

    String requestStatus = "Current";

    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getSupportActionBar().setTitle("Home");

        progressDialog = new ProgressDialog(HomeActivity.this);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getApplicationContext(), "You must accept this Location Permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

        Intent in = new Intent(HomeActivity.this, ShareLocatioService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            System.out.println("Foreground if");
            getApplicationContext().startForegroundService(in);


        } else {
            System.out.println("Foreground else");
            getApplicationContext().startService(in);
        }

        bottomAppBar = (BottomAppBar) findViewById(R.id.bottom_appbar);
        PanicButton = (FloatingActionButton) findViewById(R.id.panic_button);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        RentRequestListView = (ListView) findViewById(R.id.rent_request_listview);
        CurrentTextView = (TextView) findViewById(R.id.current_textview);
        HistoryTextView = (TextView) findViewById(R.id.history_textview);
        NoRecordTextView = (TextView) findViewById(R.id.no_records_textview);

        sharedPreferences = getSharedPreferences("user_metadata", Context.MODE_PRIVATE);

        final String CustomerId = sharedPreferences.getString("user_email", "Not Found");

        final List<RentRequestData> requestsList = new ArrayList<>();
        rentRequestAdapter = new RentRequestAdapter(this, R.layout.rent_request_list_layout, requestsList);
        RentRequestListView.setAdapter(rentRequestAdapter);

        RetrieveRentRequests(CustomerId);

//        try {
//            Intent intent = new Intent();
//            String manufacturer = android.os.Build.MANUFACTURER;
//            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
//                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
//                System.out.println("Mi Executed");
//            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
//                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
//                System.out.println("Oppo Executed");
//            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
//                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
//                System.out.println("Vivo Executed");
//            } else if("oneplus".equalsIgnoreCase(manufacturer)) {
//                intent.setComponent(new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListAct‌​ivity"));
//                System.out.println("OnePlus Executed");
//
//            }
//
//            List<ResolveInfo> list = getApplicationContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//            if  (list.size() > 0) {
//                getApplicationContext().startActivity(intent);
//            }
//        } catch (Exception e) {
//            System.out.println("here is your error: " +e);
//        }

        PanicButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                System.out.println("Panic Button Clicked");

                int panic_count=sharedPreferences.getInt("user_panic_count", 0);
                panic_count++;
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putInt("user_panic_count", panic_count);
                editor.commit();
                String user_email=sharedPreferences.getString("user_email", null);
                if(panic_count==3){
                    PanicButton.setBackgroundColor(R.color.panicStartColor);

                    if(user_email.length()!=0){
                        SendPanic(user_email);
                    }

                    Toast.makeText(getApplicationContext(), "Panic Started", Toast.LENGTH_SHORT).show();
                }else if(panic_count==6){
                    editor.putInt("user_panic_count", 0);
                    editor.commit();
                    PanicButton.setBackgroundColor(R.color.colorAccent);
                    if(user_email.length()!=0){
                        RemovePanic(user_email);
                    }
                    Toast.makeText(getApplicationContext(), "Panic Closed", Toast.LENGTH_SHORT).show();
                }
                System.out.println("Panic Count: "+panic_count);

            }
        });



//
        CurrentTextView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                requestStatus = "Current";
                RetrieveRentRequests(CustomerId);
                CurrentTextView.setTypeface(null, Typeface.BOLD);
                CurrentTextView.setTextColor(R.color.colorPrimaryDark);
                HistoryTextView.setTypeface(null, Typeface.NORMAL);
                HistoryTextView.setTextColor(R.color.accentTextColor);
                Toast.makeText(getApplicationContext(), "Current", Toast.LENGTH_SHORT).show();
            }
        });

        HistoryTextView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                requestStatus = "History";
                HistoryRetrieveRentRequests(CustomerId);
                HistoryTextView.setTypeface(null, Typeface.BOLD);
                HistoryTextView.setTextColor(R.color.colorPrimaryDark);
                CurrentTextView.setTypeface(null, Typeface.NORMAL);
                CurrentTextView.setTextColor(R.color.accentTextColor);

                Toast.makeText(getApplicationContext(), "History", Toast.LENGTH_SHORT).show();
            }
        });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (requestStatus.equals("Current")) {
                    RetrieveRentRequests(CustomerId);
                } else {
                    HistoryRetrieveRentRequests(CustomerId);
                }
            }
        });

        bottomAppBar.replaceMenu(R.menu.bottom_app_bar_menu);
        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.profile_button) {
                    startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                }
                else if(id ==R.id.aboutus_button){
                    startActivity(new Intent(HomeActivity.this, AboutUsActivity.class));
                }
                return false;
            }
        });

        RentRequestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RentRequestData rentRequestData = requestsList.get(i);
                Intent intent = new Intent(HomeActivity.this, RentRequestDetailsActivity.class);
                intent.putExtra("request_id", rentRequestData.get_id());
                startActivity(intent);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_request_button:
                Intent intent=new Intent(HomeActivity.this, BookRequestActivity.class);
                intent.putExtra("activity_type", "Book");
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void RetrieveRentRequests(final String data) {

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
                        rentRequestAdapter.clear();
                    }
                });
                try {
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/rent/customer/" + URLEncoder.encode(data));
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

                        JSONArray rents = (JSONArray) jsonObject.getJSONArray("rents");
                        if (rents.length() != 0) {
                            for (int i = 0; i < rents.length(); i++) {
                                JSONObject object = (JSONObject) rents.get(i);
                                final String _id = (String) object.get("_id");
                                final String rentStartDate = (String) object.get("rentStartDate");
                                final String rentEndDate = (String) object.get("rentEndDate");
                                final String ownerId = (String) object.get("ownerId");
                                final String customerId = (String) object.get("customerId");
                                final int cost = (int) object.get("cost");
                                final String status = (String) object.get("status");
                                final String isRequestAccepted = (String) object.get("isRequestAccepted");
                                final boolean isPaid = (boolean) object.get("isPaid");

                                final RentRequestData rentRequestData = new RentRequestData(_id, rentStartDate, rentEndDate, ownerId,
                                        customerId, cost, status, isRequestAccepted, isPaid);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();
                                        swipeRefreshLayout.setRefreshing(false);
                                        NoRecordTextView.setVisibility(View.INVISIBLE);

                                        rentRequestAdapter.add(rentRequestData);
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
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), "No Records Found.", Toast.LENGTH_SHORT).show();
                            NoRecordTextView.setVisibility(View.VISIBLE);
                        }
                    });

                }
            }
        }).start();
    }

    public void SendPanic(final String data) {

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
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/location/panic/add/" + URLEncoder.encode(data));
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

                        String status=jsonObject.getString("status");
                        System.out.println("Panic Send Status: "+status);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.cancel();
                            }
                        });

                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), "Panic add failed.", Toast.LENGTH_SHORT).show();
                            NoRecordTextView.setVisibility(View.VISIBLE);
                        }
                    });

                }
            }
        }).start();
    }

    public void RemovePanic(final String data) {

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
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/location/panic/remove/" + URLEncoder.encode(data));
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

                        String status=jsonObject.getString("status");
                        System.out.println("Panic Send Status: "+status);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.cancel();
                            }
                        });

                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), "Panic remove failed.", Toast.LENGTH_SHORT).show();
                            NoRecordTextView.setVisibility(View.VISIBLE);
                        }
                    });

                }
            }
        }).start();
    }

    public void HistoryRetrieveRentRequests(final String data) {

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
                        rentRequestAdapter.clear();
                    }
                });
                try {
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/rent/customer/history/" + URLEncoder.encode(data));
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

                        JSONArray rents = (JSONArray) jsonObject.getJSONArray("rents");
                        if (rents.length() != 0) {
                            for (int i = 0; i < rents.length(); i++) {
                                JSONObject object = (JSONObject) rents.get(i);
                                final String _id = (String) object.get("_id");
                                final String rentStartDate = (String) object.get("rentStartDate");
                                final String rentEndDate = (String) object.get("rentEndDate");
                                final String ownerId = (String) object.get("ownerId");
                                final String customerId = (String) object.get("customerId");
                                final int cost = (int) object.get("cost");
                                final String status = (String) object.get("status");
                                final String isRequestAccepted = (String) object.get("isRequestAccepted");
                                final boolean isPaid = (boolean) object.get("isPaid");

                                final RentRequestData rentRequestData = new RentRequestData(_id, rentStartDate, rentEndDate, ownerId,
                                        customerId, cost, status, isRequestAccepted, isPaid);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();
                                        NoRecordTextView.setVisibility(View.INVISIBLE);
                                        NoRecordTextView.setVisibility(View.INVISIBLE);
                                        swipeRefreshLayout.setRefreshing(false);

                                        rentRequestAdapter.add(rentRequestData);
                                    }
                                });


                            }

                        }
                    }

                } catch (
                        MalformedURLException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (
                        IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), "There's some Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (
                        JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(getApplicationContext(), "No Records Found.", Toast.LENGTH_SHORT).show();
                            NoRecordTextView.setVisibility(View.VISIBLE);
                        }
                    });

                }
            }
        }).

                start();
    }
}
