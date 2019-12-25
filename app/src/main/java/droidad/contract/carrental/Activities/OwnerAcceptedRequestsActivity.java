package droidad.contract.carrental.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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

import droidad.contract.carrental.Adapters.OwnerAcceptedRequestAdapter;
import droidad.contract.carrental.Adapters.OwnerRequestAdapter;
import droidad.contract.carrental.Holders.RentRequestData;
import droidad.contract.carrental.R;

public class OwnerAcceptedRequestsActivity extends AppCompatActivity {

    ListView OwnerRequestListView;

    SwipeRefreshLayout swipeRefreshLayout;

    OwnerAcceptedRequestAdapter ownerAcceptedRequestAdapter;

    SharedPreferences sharedPreferences;

    String ownerId;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_accepted_requests);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Requests");

        OwnerRequestListView=(ListView)findViewById(R.id.owner_request_listview);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.refresh_layout);

        sharedPreferences = getSharedPreferences("user_metadata", Context.MODE_PRIVATE);
        ownerId=sharedPreferences.getString("user_email", "Not found");

        progressDialog = new ProgressDialog(OwnerAcceptedRequestsActivity.this);

        final List<RentRequestData> requestsList=new ArrayList<>();
        ownerAcceptedRequestAdapter=new OwnerAcceptedRequestAdapter(this, R.layout.owner_accepted_requests_list_layout, requestsList);
        OwnerRequestListView.setAdapter(ownerAcceptedRequestAdapter);

        RetrieveOwnerRequests(ownerId);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RetrieveOwnerRequests(ownerId);
            }
        });

        OwnerRequestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RentRequestData rentRequestData=requestsList.get(i);
                Intent intent=new Intent(OwnerAcceptedRequestsActivity.this, TrackUsersActivity.class);
                intent.putExtra("customer_id", rentRequestData.getCustomerId());
                startActivity(intent);
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
                        ownerAcceptedRequestAdapter.clear();
                    }
                });
                try {
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/rent/owner/accepted/"+ URLEncoder.encode(data));
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
                                        swipeRefreshLayout.setRefreshing(false);

                                        progressDialog.cancel();

                                        if(!status.equals("finished")){
                                            ownerAcceptedRequestAdapter.add(rentRequestData);
                                        }

                                    }
                                });


                            }

                        }
                        if(ownerAcceptedRequestAdapter.getCount()==0){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    swipeRefreshLayout.setRefreshing(false);
                                    progressDialog.cancel();
                                    Toast.makeText(getApplicationContext(), "No Records Found", Toast.LENGTH_SHORT).show();
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
                            swipeRefreshLayout.setRefreshing(false);
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "No Records Found", Toast.LENGTH_SHORT).show();
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
