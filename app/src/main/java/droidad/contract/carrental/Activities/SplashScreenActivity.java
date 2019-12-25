package droidad.contract.carrental.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.Timer;
import java.util.TimerTask;

import droidad.contract.carrental.R;

public class SplashScreenActivity extends AppCompatActivity {

    RelativeLayout SplashLayout;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        SplashLayout=(RelativeLayout)findViewById(R.id.splash_layout);
        sharedPreferences = getSharedPreferences("user_metadata", Context.MODE_PRIVATE);

        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        final Timer t = new Timer();

        t.scheduleAtFixedRate(new TimerTask() {

                                  @Override
                                  public void run() {
                                      if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                              connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                                          t.cancel();

                                          String user_email=sharedPreferences.getString("user_email", "");
                                          String user_type=sharedPreferences.getString("user_type", "");
                                          System.out.println("user_email: "+user_email+"\n user_type: "+user_type);
                                          if( user_email.length()!=0 || user_type.length()!=0){
                                              if(user_type.equals("Car Renter")){
                                                  Intent intent=new Intent(SplashScreenActivity.this, HomeActivity.class);
                                                  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                  startActivity(intent);
                                              }else{
                                                  Intent intent=new Intent(SplashScreenActivity.this, OwnerHomeActivity.class);
                                                  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                  startActivity(intent);
                                              }
                                          }else{
                                             // Intent intent=new Intent(SplashScreenActivity.this, LogInActivity.class);
                                              Intent intent = new Intent(SplashScreenActivity.this,UserTypeSelectionActivity.class);
                                              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                              startActivity(intent);

                                          }


//
                                      } else {

                                          Snackbar snackbar = Snackbar.make(SplashLayout, "No internet connection", Snackbar.LENGTH_SHORT);

                                          snackbar.show();
                                      }
                                  }

                              },

                2000,

                2000);
    }
}
