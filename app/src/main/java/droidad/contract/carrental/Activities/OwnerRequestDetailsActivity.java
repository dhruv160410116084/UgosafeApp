package droidad.contract.carrental.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import droidad.contract.carrental.R;

public class OwnerRequestDetailsActivity extends AppCompatActivity {

    TextView RequestIdTextView, StartDateTextView, EndDateTextView, CustomerIdTextView, CostTextView,
            RequestStatusTextView,JourneyStatusTextView, PaymentStatusTextView,DeliveryStatusTextView,DriverStatusTextView;

    Button AcceptButton, RejectButton;

    String request_id, owner_id;
    int rentCost=0;

    SharedPreferences sharedPreferences;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_request_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Request Details");

        RequestIdTextView=(TextView)findViewById(R.id.request_id_textview);
        StartDateTextView=(TextView)findViewById(R.id.start_date_textview);
        EndDateTextView=(TextView)findViewById(R.id.end_date_textview);
        CustomerIdTextView=(TextView)findViewById(R.id.customer_id_textview);
        CostTextView=(TextView)findViewById(R.id.cost_textview);
        RequestStatusTextView=(TextView)findViewById(R.id.request_status_textview);
        JourneyStatusTextView=(TextView)findViewById(R.id.journey_status_textview);
        PaymentStatusTextView=(TextView)findViewById(R.id.payment_status_textview);
        AcceptButton=(Button)findViewById(R.id.accept_button);
        RejectButton=(Button)findViewById(R.id.reject_button);
        DeliveryStatusTextView = (TextView)findViewById(R.id.delivery_status);
        DriverStatusTextView = (TextView)findViewById(R.id.driver_status);

        sharedPreferences = getSharedPreferences("user_metadata", Context.MODE_PRIVATE);

        progressDialog = new ProgressDialog(OwnerRequestDetailsActivity.this);

        owner_id=sharedPreferences.getString("user_email", "Not Found");

        request_id=getIntent().getExtras().getString("request_id");

        if(request_id.length()>0) {
            RequestIdTextView.setText(request_id);
            RetrieveOwnerRequestDetails(request_id);
        }

        AcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(OwnerRequestDetailsActivity.this)
                        .setTitle("Request Conformation")
                        .setMessage("Do you want to Accept Request?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                JSONObject request_data=new JSONObject();
                                JSONObject selection=new JSONObject();
                                JSONObject update=new JSONObject();

                                try {
                                    selection.put("_id", request_id);

                                    update.put("isRequestAccepted", "accepted");

                                    request_data.put("selection", selection);
                                    request_data.put("update", update);

                                    UpdateRequestStatus(request_data);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
            }
        });

        RejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(OwnerRequestDetailsActivity.this)
                        .setTitle("Request Conformation")
                        .setMessage("Do you want to Reject Request?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                JSONObject request_data=new JSONObject();
                                JSONObject selection=new JSONObject();
                                JSONObject update=new JSONObject();

                                try {
                                    selection.put("_id", request_id);

                                    update.put("isRequestAccepted", "rejected");

                                    request_data.put("selection", selection);
                                    request_data.put("update", update);

                                    UpdateRequestStatus(request_data);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
            }
        });
    }

    public void RetrieveOwnerRequestDetails(final String data) {

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
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/rent/"+ URLEncoder.encode(data));
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
                        System.out.println("owner rent details"+jsonObject); //{"money":1000,"_id":"5dbd602e90c09f00178cec1e","name":"User2","email":"user2@gmail.com","password":"User2","isOwner":false,"__v":0}
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
                                final boolean isHomeDelivery = (boolean)object.get("isHomeDelivery");
                                final boolean driver = (boolean)object.get("driver");

                                rentCost=cost;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();
                                        StartDateTextView.setText(rentStartDate);
                                        EndDateTextView.setText(rentEndDate);
                                        CustomerIdTextView.setText(customerId);
                                        CostTextView.setText(CostTextView.getText().charAt(0)+String.valueOf(cost));
                                        JourneyStatusTextView.setText(status);
                                        RequestStatusTextView.setText(isRequestAccepted);
                                        if(isPaid){
                                            PaymentStatusTextView.setText("Paid");
                                        }else {
                                            PaymentStatusTextView.setText("Not Paid");
                                        }

                                        if(isHomeDelivery){
                                            DeliveryStatusTextView.setText("Home Delivery");
                                        }else{
                                            DeliveryStatusTextView.setText("Pick up");
                                        }

                                        if(driver){
                                            DriverStatusTextView.setText("Yes");
                                        }else{
                                            DriverStatusTextView.setText("No");
                                        }

                                        if(isRequestAccepted.equals("pending")){
                                            AcceptButton.setVisibility(View.VISIBLE);
                                            RejectButton.setVisibility(View.VISIBLE);
                                        }else {
                                            AcceptButton.setVisibility(View.INVISIBLE);
                                            RejectButton.setVisibility(View.INVISIBLE);
                                        }

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
                            Toast.makeText(getApplicationContext(), "No Records Found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }


    public void UpdateRequestStatus(final JSONObject data) {

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
                        System.out.println("length: "+jsonObject.length());

                        int nModified=(int)jsonObject.get("nModified");
                        if(nModified==1){

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    Toast.makeText(getApplicationContext(), "Updated Request Status Successfully.", Toast.LENGTH_SHORT).show();
                                }
                            });

                            Intent intent=new Intent(OwnerRequestDetailsActivity.this, OwnerHomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("request_id", request_id);
                            startActivity(intent);
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    Toast.makeText(getApplicationContext(), "Not Updated Request Status Successfully. Try Again", Toast.LENGTH_SHORT).show();
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
