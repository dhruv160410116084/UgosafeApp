package droidad.contract.carrental.Activities;

import androidx.appcompat.app.AppCompatActivity;
import droidad.contract.carrental.R;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class feedbackActivity extends AppCompatActivity {

    Button sendFeedbackButton;
    EditText feedback;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedback = (EditText)findViewById(R.id.feedback_edit_text);
        sendFeedbackButton = (Button)findViewById(R.id.feedback);
        progressDialog = new ProgressDialog(feedbackActivity.this);

        sendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject object = new JSONObject();
                JSONObject selection = new JSONObject();
                JSONObject update = new JSONObject();
                try {
                    if(feedback.getText().toString().length() >= 4){
                        selection.put("_id",getIntent().getExtras().getString("request_id"));
                        update.put("feedback",feedback.getText().toString());
                        object.put("selection",selection);
                        object.put("update",update);
                        UpdateJourneryStatus(object);
                    }else{
                        Toast.makeText(getApplicationContext(),"we would appriciate you feedback",Toast.LENGTH_SHORT).show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
                                    Toast.makeText(getApplicationContext(), "Feedback send successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent intent;
                            intent=new Intent(feedbackActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.cancel();
                                    //Toast.makeText(getApplicationContext(), "Not Updated Journey Status Successfully. Try Again", Toast.LENGTH_SHORT).show();
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
}
