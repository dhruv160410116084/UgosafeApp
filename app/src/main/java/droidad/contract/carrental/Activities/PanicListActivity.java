package droidad.contract.carrental.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import droidad.contract.carrental.Adapters.OwnerRequestAdapter;
import droidad.contract.carrental.Holders.RentRequestData;
import droidad.contract.carrental.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

public class PanicListActivity extends AppCompatActivity {

    ListView PanicListView;
    SwipeRefreshLayout swipeRefreshLayout;

    ProgressDialog progressDialog;

    ArrayAdapter<String> panicUsersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panic_list);

        PanicListView=(ListView)findViewById(R.id.panic_listview);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.refresh_layout);
        progressDialog = new ProgressDialog(PanicListActivity.this);

        final List<String> panicList=new ArrayList<>();
        panicUsersAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, panicList);
        PanicListView.setAdapter(panicUsersAdapter);

        RetrievePanicRequests();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RetrievePanicRequests();
            }
        });

        PanicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String panicUser=panicUsersAdapter.getItem(i);
                Intent intent=new Intent(PanicListActivity.this, TrackUsersActivity.class);
                intent.putExtra("customer_id", panicUser);
                startActivity(intent);
            }
        });


    }

    public void RetrievePanicRequests() {

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
                        panicUsersAdapter.clear();
                    }
                });
                try {
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/location/panic/list");
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
                        System.out.println("panic length: "+jsonObject.length());

                        JSONArray list=(JSONArray)jsonObject.getJSONArray("list");
                        System.out.println("List Length: "+list.length());
                        if(list.length()>0){

                            for(int i=0; i<list.length(); i++){
                                JSONObject object= (JSONObject) list.get(i);
                                try{
                                    final String email=(String)object.get("email");
                                    System.out.println("user_email: "+email);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.cancel();
                                            swipeRefreshLayout.setRefreshing(false);

                                            panicUsersAdapter.add(email);
                                        }
                                    });
                                }catch (Exception e){

                                }






                            }
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    Toast.makeText(getApplicationContext(), "No Records Found.", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getApplicationContext(), "No Records Found.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
