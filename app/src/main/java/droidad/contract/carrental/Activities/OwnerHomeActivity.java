package droidad.contract.carrental.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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
import java.util.ArrayList;
import java.util.List;

import droidad.contract.carrental.Adapters.OwnerRequestAdapter;
import droidad.contract.carrental.Adapters.RentRequestAdapter;
import droidad.contract.carrental.Holders.RentRequestData;
import droidad.contract.carrental.R;

public class OwnerHomeActivity extends AppCompatActivity {

    BottomAppBar bottomAppBar;
    ListView OwnerRequestListView;

    FloatingActionButton LocateUsersButton;

    SwipeRefreshLayout swipeRefreshLayout;

    TextView CurrentTextView, HistoryTextView, NoRecordTextView;

    OwnerRequestAdapter ownerRequestAdapter;

    SharedPreferences sharedPreferences;

    String ownerId;

    String requestStatus = "Current";

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);

        getSupportActionBar().setTitle("Home");

        bottomAppBar=(BottomAppBar)findViewById(R.id.bottom_appbar);
        OwnerRequestListView=(ListView)findViewById(R.id.owner_request_listview);
        LocateUsersButton=(FloatingActionButton)findViewById(R.id.locate_users_button);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.refresh_layout);
        CurrentTextView = (TextView) findViewById(R.id.current_textview);
        HistoryTextView = (TextView) findViewById(R.id.history_textview);
        NoRecordTextView = (TextView) findViewById(R.id.no_records_textview);

        sharedPreferences = getSharedPreferences("user_metadata", Context.MODE_PRIVATE);
        ownerId=sharedPreferences.getString("user_email", "Not found");

        progressDialog = new ProgressDialog(OwnerHomeActivity.this);

        final List<RentRequestData> requestsList=new ArrayList<>();
        ownerRequestAdapter=new OwnerRequestAdapter(this, R.layout.owner_request_list_layout, requestsList);
        OwnerRequestListView.setAdapter(ownerRequestAdapter);

        RetrieveOwnerRequests(ownerId);

        bottomAppBar.replaceMenu(R.menu.owner_home_menu);
        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.panic_list_button) {
                    startActivity(new Intent(OwnerHomeActivity.this, PanicListActivity.class));
                }
                else if (id == R.id.profile_button) {
                    startActivity(new Intent(OwnerHomeActivity.this, ProfileActivity.class));
                }
                else if(id ==R.id.aboutus_button){
                    startActivity(new Intent(OwnerHomeActivity.this, AboutUsActivity.class));
                }
                return false;
            }
        });


        CurrentTextView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                requestStatus = "Current";
                RetrieveOwnerRequests(ownerId);
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
                HistoryOwnerRetrieveRentRequests(ownerId);
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
                    RetrieveOwnerRequests(ownerId);
                } else {
                    HistoryOwnerRetrieveRentRequests(ownerId);
                }

            }
        });

        OwnerRequestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RentRequestData rentRequestData=requestsList.get(i);
                Intent intent=new Intent(OwnerHomeActivity.this, OwnerRequestDetailsActivity.class);
                intent.putExtra("request_id", rentRequestData.get_id());
                startActivity(intent);
            }
        });

        LocateUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OwnerHomeActivity.this, OwnerAcceptedRequestsActivity.class));
            }
        });

    }


    public void RetrieveOwnerRequests(final String data) {

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ownerRequestAdapter.clear();
                    }
                });
                try {
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/rent/owner/"+ URLEncoder.encode(data));
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

                        JSONArray rents=(JSONArray)jsonObject.getJSONArray("rents");
                        if(rents.length()!=0){
                            for(int i=0; i<rents.length(); i++){
                                JSONObject object= (JSONObject) rents.get(i);
                                final String _id=(String)object.get("_id");
                                final String rentStartDate=(String)object.get("rentStartDate");
                                final String rentEndDate=(String)object.get("rentEndDate");
                                final String ownerId=(String)object.get("ownerId");
                                final String customerId=(String)object.get("customerId");
                                final int cost=(int)object.get("cost");
                                final String status=(String)object.get("status");
                                final String isRequestAccepted=(String)object.get("isRequestAccepted");
                                final boolean isPaid=(boolean)object.get("isPaid");

                                final RentRequestData rentRequestData=new RentRequestData(_id, rentStartDate, rentEndDate, ownerId,
                                        customerId, cost, status, isRequestAccepted, isPaid);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();
                                        swipeRefreshLayout.setRefreshing(false);

                                        ownerRequestAdapter.add(rentRequestData);
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
                            Toast.makeText(getApplicationContext(), "No Records Found.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void HistoryOwnerRetrieveRentRequests(final String data) {

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ownerRequestAdapter.clear();
                    }
                });
                try {
                    URL url = new URL("https://fast-cliffs-52494.herokuapp.com/rent/owner/history/"+ URLEncoder.encode(data));
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

                        JSONArray rents=(JSONArray)jsonObject.getJSONArray("rents");
                        if(rents.length()!=0){
                            for(int i=0; i<rents.length(); i++){
                                JSONObject object= (JSONObject) rents.get(i);
                                final String _id=(String)object.get("_id");
                                final String rentStartDate=(String)object.get("rentStartDate");
                                final String rentEndDate=(String)object.get("rentEndDate");
                                final String ownerId=(String)object.get("ownerId");
                                final String customerId=(String)object.get("customerId");
                                final int cost=(int)object.get("cost");
                                final String status=(String)object.get("status");
                                final String isRequestAccepted=(String)object.get("isRequestAccepted");
                                final boolean isPaid=(boolean)object.get("isPaid");

                                final RentRequestData rentRequestData=new RentRequestData(_id, rentStartDate, rentEndDate, ownerId,
                                        customerId, cost, status, isRequestAccepted, isPaid);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();
                                        swipeRefreshLayout.setRefreshing(false);

                                        ownerRequestAdapter.add(rentRequestData);
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
                            Toast.makeText(getApplicationContext(), "No Records Found.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
