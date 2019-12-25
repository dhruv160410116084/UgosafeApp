package droidad.contract.carrental.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

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

public class LogInActivity extends AppCompatActivity {

    RelativeLayout MainLayout;
    TextInputLayout EmailIdTextInput, PasswordTextInput;
    Button LoginButton;
    TextView SignUpTextView;

    SharedPreferences sharedPreferences;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        getSupportActionBar().setTitle("Login");

        MainLayout=(RelativeLayout)findViewById(R.id.main_layout);
        EmailIdTextInput=(TextInputLayout)findViewById(R.id.email_id_textinput);
        PasswordTextInput=(TextInputLayout)findViewById(R.id.password_textinput);
        LoginButton=(Button)findViewById(R.id.login_button);
        SignUpTextView=(TextView)findViewById(R.id.signup_textview);

        sharedPreferences = getSharedPreferences("user_metadata", Context.MODE_PRIVATE);

        progressDialog = new ProgressDialog(LogInActivity.this);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(EmailIdTextInput.getEditText().getText().length()==0){
                    EmailIdTextInput.getEditText().setError("Please, insert Email Id");
                }
                else if(PasswordTextInput.getEditText().getText().length()==0){
                    PasswordTextInput.getEditText().setError("Please, insert Password.");
                }
                else if(EmailIdTextInput.getEditText().getText().length()==0 && PasswordTextInput.getEditText().getText().length()==0){
                    EmailIdTextInput.getEditText().setError("Please, insert Email Id");
                    PasswordTextInput.getEditText().setError("Please, insert Password.");
                }
                else {

                    try {
                        JSONObject loginDetails=new JSONObject();
                        loginDetails.put("email", EmailIdTextInput.getEditText().getText());
                        loginDetails.put("password", PasswordTextInput.getEditText().getText());

                        LoginUser(loginDetails);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        SignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(LogInActivity.this, SignUpActivity.class));

            }
        });



    }

    public void LoginUser(final JSONObject data) {

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
                //shared_phone_no = customer_no;
                try {
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/user/login");
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
                        System.out.println("length: "+jsonObject.length());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.cancel();
                            }
                        });

                        if(jsonObject.length()==0){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Email or Password is wrong.", Toast.LENGTH_LONG).show();
                                }
                            });

                        }else {
                            String _id=(String)jsonObject.get("_id");
                            String email=(String)jsonObject.get("email");
                            boolean isOwner=(boolean)jsonObject.get("isOwner");
                            String userType;
                            if(isOwner){
                                userType="Car Owner";
                            }else {
                                userType="Car Renter";
                            }
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                            editor.putString("user_id", _id);
                            editor.putString("user_email", email);
                            editor.putString("user_type", userType);
                            editor.putInt("user_panic_count", 0);
                            editor.commit();

                            if(userType.equals("Car Owner")){
                                Intent intent=new Intent(LogInActivity.this, OwnerHomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }else {
                                Intent intent=new Intent(LogInActivity.this, BookRequestActivity.class);
                                intent.putExtra("activity_type", "Try");
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }

                        }

                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some error.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some error.", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();

                  runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            Toast.makeText(getApplicationContext(), "There's some error.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}
