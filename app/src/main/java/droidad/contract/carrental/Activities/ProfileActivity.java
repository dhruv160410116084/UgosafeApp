package droidad.contract.carrental.Activities;

import androidx.appcompat.app.AppCompatActivity;
import droidad.contract.carrental.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
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

public class ProfileActivity extends AppCompatActivity {

    TextView NameTextView, EmailTextView, UserTypeTextView, WalletMoneyTextView;
    Button LogOutButton;
    ProgressDialog progressDialog;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");

        NameTextView=(TextView)findViewById(R.id.name_textview);
        EmailTextView=(TextView)findViewById(R.id.email_textview);
        UserTypeTextView=(TextView)findViewById(R.id.user_type_textview);
        WalletMoneyTextView=(TextView)findViewById(R.id.wallet_money_textview);
        LogOutButton=(Button)findViewById(R.id.log_out_button);

        sharedPreferences = getSharedPreferences("user_metadata", Context.MODE_PRIVATE);
        progressDialog = new ProgressDialog(ProfileActivity.this);
        String user_email=sharedPreferences.getString("user_email", "empty");

        if(!user_email.equals("empty")){
            FetechProfile(user_email);
        }else {
            Intent intent=new Intent(ProfileActivity.this, LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        LogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.clear().commit();
                Intent intent=new Intent(ProfileActivity.this, LogInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });

    }

    public void FetechProfile(final String email) {

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
                    URL url = new URL("https://fast-cliffs-52494.herokuapp.com/user/profile/"+ URLEncoder.encode(email));
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setDoInput(true);
                    //httpURLConnection.setDoOutput(true);
                    httpURLConnection.setConnectTimeout(10000);
                    //httpURLConnection.setRequestProperty("Content-Type", "application/json");

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
                        System.out.println("Profile Res: "+jsonObject.toString());

                        inputStream.close();
                        httpURLConnection.disconnect();

                        if(jsonObject.length()!=0){
                            final int money=(int)jsonObject.get("money");
                            final String name=(String)jsonObject.get("name");
                            final String email=(String)jsonObject.get("email");
                            final boolean isOwner=(boolean)jsonObject.getBoolean("isOwner");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    NameTextView.setText(name);
                                    EmailTextView.setText(email);
                                    WalletMoneyTextView.setText(String.valueOf(money));
                                    if(isOwner){
                                        UserTypeTextView.setText("Owner");
                                    }else {
                                        UserTypeTextView.setText("User");
                                    }
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
                            Toast.makeText(getApplicationContext(), "No Records Found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
