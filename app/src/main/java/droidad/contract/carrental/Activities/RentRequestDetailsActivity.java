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

public class RentRequestDetailsActivity extends AppCompatActivity {

    TextView RequestIdTextView, StartDateTextView, EndDateTextView, OwnerIdTextView, CostTextView,
    RequestStatusTextView,JourneyStatusTextView, PaymentStatusTextView, EquipmentsTextView,DeliveryTypeTextView,DriverTextView;

    Button PayRentButton, StartJourneyButton, EndJourneyButton,FeedBackButton, EditButton;

    String request_id, customer_id;
    int rentCost=0;
    String equipments_string="";

    boolean endJourneyClicked = false;
    SharedPreferences sharedPreferences;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_request_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Request Details");

        RequestIdTextView=(TextView)findViewById(R.id.request_id_textview);
        StartDateTextView=(TextView)findViewById(R.id.start_date_textview);
        EndDateTextView=(TextView)findViewById(R.id.end_date_textview);
        OwnerIdTextView=(TextView)findViewById(R.id.owner_id_textview);
        CostTextView=(TextView)findViewById(R.id.cost_textview);
        RequestStatusTextView=(TextView)findViewById(R.id.request_status_textview);
        JourneyStatusTextView=(TextView)findViewById(R.id.journey_status_textview);
        PaymentStatusTextView=(TextView)findViewById(R.id.payment_status_textview);
        EquipmentsTextView=(TextView)findViewById(R.id.equipments_textview);
        PayRentButton=(Button)findViewById(R.id.pay_rent_button);
        StartJourneyButton=(Button)findViewById(R.id.start_journey_button);
        EndJourneyButton=(Button)findViewById(R.id.end_journey_button);
        EditButton=(Button)findViewById(R.id.edit_button);
        DeliveryTypeTextView = (TextView)findViewById(R.id.delivery_type);
        DriverTextView = (TextView)findViewById(R.id.driver);


//        FeedBackButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(RentRequestDetailsActivity.this, feedbackActivity.class);
//                startActivity(i);
//            }
//        });

        sharedPreferences = getSharedPreferences("user_metadata", Context.MODE_PRIVATE);

        progressDialog = new ProgressDialog(RentRequestDetailsActivity.this);

        customer_id=sharedPreferences.getString("user_email", "Not Found");

        request_id=getIntent().getExtras().getString("request_id");

        if(request_id.length()>0){
            RequestIdTextView.setText(request_id);
            RetrieveRentRequestDetails(request_id);
        }

        EditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(RentRequestDetailsActivity.this, BookRequestActivity.class);
                intent.putExtra("activity_type", "Edit");
                startActivity(intent);

            }
        });



        PayRentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(RentRequestDetailsActivity.this)
                        .setTitle("Payment Conformation")
                        .setMessage("Do you want to Pay Rent from your Wallet?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                JSONObject pay_rent_data=new JSONObject();
                                JSONObject sender=new JSONObject();
                                JSONObject receiver=new JSONObject();

                                try {
                                    sender.put("email", customer_id);
                                    receiver.put("email", OwnerIdTextView.getText().toString());
                                    pay_rent_data.put("sender", sender);
                                    pay_rent_data.put("receiver", receiver);
                                    pay_rent_data.put("cost", rentCost);
                                    pay_rent_data.put("rentId", request_id);

                                    PayRent(pay_rent_data);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(getApplicationContext(), "Please, pay rent to start journey", Toast.LENGTH_LONG).show();
                            }
                        })
                        .show();
            }
        });

        StartJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(RentRequestDetailsActivity.this)
                        .setTitle("Journey Start Conformation")
                        .setMessage("Do you want to Start Journey?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                JSONObject journey_data=new JSONObject();
                                JSONObject selection=new JSONObject();
                                JSONObject update=new JSONObject();

                                try {
                                    selection.put("_id", request_id);

                                    update.put("status", "started");

                                    journey_data.put("selection", selection);
                                    journey_data.put("update", update);

                                    UpdateJourneryStatus(journey_data);

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

        EndJourneyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(RentRequestDetailsActivity.this)
                        .setTitle("Journey End Conformation")
                        .setMessage("Do you want to End Journey?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                JSONObject journey_data=new JSONObject();
                                JSONObject selection=new JSONObject();
                                JSONObject update=new JSONObject();

                                try {
                                    selection.put("_id", request_id);

                                    update.put("status", "finished");

                                    journey_data.put("selection", selection);
                                    journey_data.put("update", update);
                                    endJourneyClicked = true;
                                    UpdateJourneryStatus(journey_data);

                                   // Intent intent = new Intent(RentRequestDetailsActivity.this,feedbackActivity.class);
                                    //intent.putExtra("_id",request_id);
                                    //startActivity(intent);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                endJourneyClicked = false;
                            }
                        })
                        .show();
            }
        });

    }

    public void RetrieveRentRequestDetails(final String data) {

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
                        System.out.println("customer rent deatils"+jsonObject); //{"money":1000,"_id":"5dbd602e90c09f00178cec1e","name":"User2","email":"user2@gmail.com","password":"User2","isOwner":false,"__v":0}
                        System.out.println("length: "+jsonObject.length());

                        JSONArray rents=(JSONArray)jsonObject.getJSONArray("rents");
                        System.out.println("rents: "+rents);
                        if(rents.length()!=0){
                            for(int i=0; i<rents.length(); i++){
                                JSONObject object= (JSONObject) rents.get(i);
                                System.out.println("Object "+i+": "+object);
                                final String _id=(String)object.get("_id");
                                final String rentStartDate=(String)object.get("rentStartDate");
                                final String rentEndDate=(String)object.get("rentEndDate");
                                final String ownerId=(String)object.get("ownerId");
                                final String ownerIdEnd=(String)object.get("ownerIdEnd");
                                final String customerId=(String)object.get("customerId");
                                final String startCity=(String)object.get("startCity");
                                final String endCity=(String)object.get("endCity");
                                JSONObject location=(JSONObject)object.get("location");
                                final String lat=(String)location.get("lat");
                                final String lon=(String)location.get("lon");
                                final int cost=(int)object.get("cost");
                                final String status=(String)object.get("status");
                                final String isRequestAccepted=(String)object.get("isRequestAccepted");
                                final boolean isPaid=(boolean)object.get("isPaid");
                                final boolean deliveryType = (boolean)object.get("isHomeDelivery");
                                final boolean driver = (boolean)object.get("driver");
                                JSONArray equipments=(JSONArray)object.get("equipments");

                                for(int j=0; j<equipments.length(); j++){
                                    if(j==0){
                                        equipments_string+=equipments.get(j);
                                    }else{
                                        equipments_string+="\n"+equipments.get(j);
                                    }
                                }

                                rentCost=cost;

                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                editor.putString("rent_id", _id);
                                editor.putString("start_date", rentStartDate);
                                editor.putString("end_date", rentEndDate);
                                editor.putString("cost", String.valueOf(cost));
                                editor.putString("start_city", startCity);
                                editor.putString("end_city", endCity);
                                editor.putString("customer_lat", lat);
                                editor.putString("customer_lng", lon);
                                editor.putBoolean("driver", driver);
                                editor.putBoolean("home_delivery", deliveryType);
                                editor.commit();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();
                                        StartDateTextView.setText(rentStartDate);
                                        EndDateTextView.setText(rentEndDate);
                                        OwnerIdTextView.setText(ownerId);
                                        CostTextView.setText(CostTextView.getText().charAt(0)+String.valueOf(cost));
                                         JourneyStatusTextView.setText(status);
                                         RequestStatusTextView.setText(isRequestAccepted);
                                         EquipmentsTextView.setText(equipments_string);
                                         if(isPaid){
                                             PaymentStatusTextView.setText("Paid");
                                         }else {
                                             PaymentStatusTextView.setText("Not Paid");
                                         }

                                         if(deliveryType)
                                                DeliveryTypeTextView.setText("Home Delivery");
                                         else
                                                DeliveryTypeTextView.setText("PickUp");

                                         if(driver)
                                                DriverTextView.setText("Yes");
                                         else
                                                DriverTextView.setText("No");

                                        if(RequestStatusTextView.getText().toString().equals("accepted")&&JourneyStatusTextView.getText().toString().equals("notStarted")
                                                &&PaymentStatusTextView.getText().toString().equals("Not Paid")){
                                            PayRentButton.setVisibility(View.VISIBLE);
                                            StartJourneyButton.setVisibility(View.INVISIBLE);
                                            EndJourneyButton.setVisibility(View.INVISIBLE);
                                        }

                                        if(RequestStatusTextView.getText().toString().equals("accepted")&&JourneyStatusTextView.getText().toString().equals("notStarted")
                                                &&PaymentStatusTextView.getText().toString().equals("Paid")){
                                            PayRentButton.setVisibility(View.INVISIBLE);
                                            StartJourneyButton.setVisibility(View.VISIBLE);
                                            EndJourneyButton.setVisibility(View.INVISIBLE);
                                        }

                                        if(RequestStatusTextView.getText().toString().equals("accepted")&&JourneyStatusTextView.getText().toString().equals("started")
                                                &&PaymentStatusTextView.getText().toString().equals("Paid")){
                                            PayRentButton.setVisibility(View.INVISIBLE);
                                            StartJourneyButton.setVisibility(View.INVISIBLE);
                                            EndJourneyButton.setVisibility(View.VISIBLE);
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
                            Toast.makeText(getApplicationContext(), "No Records Found.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void PayRent(final JSONObject data) {

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
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/user/pay");
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
                        final JSONObject jsonObject = new JSONObject(stringBuffer.toString());

                        inputStream.close();
                        httpURLConnection.disconnect();
                        System.out.println(jsonObject);
                        System.out.println("length: "+jsonObject.length());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.cancel();
                            }
                        });

                        try{
                            final String status=(String)jsonObject.get("status");
                            if(status.equals("success")){
                                Intent intent=new Intent(RentRequestDetailsActivity.this, RentRequestDetailsActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("request_id", request_id);
                                startActivity(intent);
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.cancel();

                                        Toast.makeText(getApplicationContext(), "There's some Error. Try Again", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    try {
                                        JSONObject err=(JSONObject)jsonObject.get("err");
                                        String status=(String)err.get("status");
                                        if(status.equals("balance is low")){
                                            Toast.makeText(getApplicationContext(), "You don't have sufficient balance.", Toast.LENGTH_LONG).show();
                                        }else {
                                            Toast.makeText(getApplicationContext(), "There's some Error. Try Again", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException ex) {
                                        ex.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "There's some Error. Try Again", Toast.LENGTH_LONG).show();
                                    }
                                    //Toast.makeText(getApplicationContext(), "There's some Error. Try Again", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(getApplicationContext(), "There's some Error. Try Again", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error. Try Again", Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error. Try Again", Toast.LENGTH_LONG).show();
                        }
                    });                }
            }
        }).start();
    }

    public void UpdateJourneryStatus(final JSONObject data) {

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
                                    Toast.makeText(getApplicationContext(), "Updated Journey Status Successfully.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent intent;
                            if(endJourneyClicked)
                             intent=new Intent(RentRequestDetailsActivity.this, feedbackActivity.class);
                            else
                                intent=new Intent(RentRequestDetailsActivity.this, RentRequestDetailsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("request_id", request_id);
                            startActivity(intent);
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    Toast.makeText(getApplicationContext(), "Not Updated Journey Status Successfully. Try Again", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getApplicationContext(), "There's some Error. Try Again", Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some Error. Try Again", Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
