package droidad.contract.carrental.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;

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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import droidad.contract.carrental.Activities.HomeActivity;
import droidad.contract.carrental.Activities.LogInActivity;
import droidad.contract.carrental.Activities.SignUpActivity;
import droidad.contract.carrental.R;

import static androidx.core.app.NotificationCompat.PRIORITY_LOW;

public class ShareLocatioService extends Service {
    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
    NotificationManager mNotificationManager;

    SharedPreferences sharedPreferences;


    String PendingRequestId;
    int count=0;
    String ch_name = "Location Service";
    String LocationString = "";
    String shared_customer_id;
    Double lat=0.0, lng=0.0;
    NotificationChannel channel = null;
    HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(), Process.THREAD_PRIORITY_BACKGROUND);
    Handler handler;

    NotificationCompat.Builder notificationBuilder;
    Notification notification;


    LocationManager mLocationManager;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("onTaskRemoved");
        Toast.makeText(getApplicationContext(), "Task Removed", Toast.LENGTH_SHORT).show();
        //startService(new Intent(this, NotificationService.class));
        super.onTaskRemoved(rootIntent);

    }


    @Override
    public void onCreate() {
        super.onCreate();
        /*try {
            Intent intent = new Intent();
            String manufacturer = android.os.Build.MANUFACTURER;
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                System.out.println("Mi Executed");
            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
                System.out.println("Oppo Executed");
            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                System.out.println("Vivo Executed");
            } else if("oneplus".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListAct‌​ivity"));
                System.out.println("OnePlus Executed");

            }

            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if  (list.size() > 0) {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            System.out.println("here is your error: " +e);
        }*/

        System.out.println(" notification on create");
        sharedPreferences = getSharedPreferences("user_metadata", Context.MODE_PRIVATE);
        shared_customer_id=sharedPreferences.getString("user_email", "Not Found");


        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Toast.makeText(getApplicationContext(), "Location service started", Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("222", ch_name, NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
        }

        final Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round);
        notificationBuilder = new NotificationCompat.Builder(this, "222");
        notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(largeIcon)
                .setContentTitle("U Go Safe")
                .setPriority(PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        startForeground(207, notification);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setLargeIcon(largeIcon);
        mBuilder.setContentTitle("U Go Safe Location Update");
        mBuilder.setPriority(Notification.PRIORITY_HIGH);
        mBuilder.setAutoCancel(true);

        mBuilder.setChannelId("222");
        mBuilder.setContentText("You have new Notification!");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        } else {
            handler = new Handler();
        }

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);


        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1,
                1, new android.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
//                        LocationString=location.getLatitude()+"/"+location.getLongitude();
////                        setNotificationData();
//                        UpdateCurrentLocation(shared_employee_id, location.getLatitude(), location.getLongitude());
                        System.out.println("Location Changed");
                        lat=location.getLatitude();
                        lng=location.getLongitude();

                        JSONObject user_location_data=new JSONObject();
                        JSONObject location_latlng=new JSONObject();

                        try {
                            location_latlng.put("lon", lng);
                            location_latlng.put("lan", lat);

                            user_location_data.put("email", shared_customer_id);
                            user_location_data.put("location", location_latlng);

                            UpdateLocation(user_location_data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                });


        /*new Thread(new Runnable() {
            @Override
            public void run() {
                current_size = countNotice();
                System.out.println("onCreate Notice " + current_size);
                old_size = current_size;
            }
        }).start();*/


//     String   time = new SimpleDateFormat("dd-MM").format(Calendar.getInstance().getTime());

        //instance = this;


    }

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {

        System.out.println("onStartCommand");




        //updateLocation();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isInternetAvailable()) {
                            // String mycal= String.valueOf(simpleDateFormat("dd-MM").format(Calendar.getInstance().getTime().getDate())+"-"+
                            //       String.valueOf(Calendar.getInstance().getTime().getMonth()));

                            /*new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    getNotificationData();
                                }
                            }).start();*/
                            System.out.println("Lat: "+lat+" Lng: "+lng);
                            if(lat!=0.0 && lng!=0.0){

                            }
                            else {
                                System.out.println("Location Initialize "+count);
                                count++;
                            }

//                            setNotificationData();
                            /*current_size = countNotice();
                            System.out.println(" new notice count: " + current_size);
                            System.out.println(" old notice count: " + old_size);
                            if (current_size == 0) {
                                old_size = 0;
                            } else if (current_size > 1 && old_size == 0) {
                                old_size = current_size;
                                System.out.println("This is Not Notification");
                            } else if (current_size > old_size) {
                                System.out.println("this is our notification");
                                /*int notice_badge=Integer.parseInt(sharedPreferences.getString("notice_badge", "0"));
                                notice_badge=notice_badge+1;
                                editor.putString("notice_badge", String.valueOf(notice_badge));
                                editor.commit();
                                old_size = current_size;
                                mNotificationManager.notify(1, mBuilder.build());
                            }*/
                        } else {
                            System.out.println("else executed");
                        }
                    }

                }).start();

                handler.postDelayed(this, 15000);
            }

        }, 5000);


        //System.out.println("\n substring"+sub);
        //System.out.println("\n date of birth"+time);
        //System.out.println("\n my shared dob"+dob);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //startService(new Intent(this, NotificationServices.class));       super.onDestroy();
        Toast.makeText(getApplicationContext(), "destrooyed service", Toast.LENGTH_SHORT).show();
        System.out.println("location service destroyed");
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

    public void UpdateLocation(final JSONObject data) {

        new Thread(new Runnable() {
            @Override
            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        LoadingProgressBar.setVisibility(View.VISIBLE);
//                    }
//                });
                //shared_phone_no = customer_no;
                try {
                    URL url = new URL("http://fast-cliffs-52494.herokuapp.com/location");
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



                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            setContentView(R.layout.error_layout);
//
//                            findViewById(R.id.try_again_textview).setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    Intent intent = new Intent(SplashScreenActivity.this, SplashScreenActivity.class);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(intent);
//
//                                }
//                            });
//                        }
//                    });
                } catch (IOException e) {
                    e.printStackTrace();

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            setContentView(R.layout.error_layout);
//
//                            findViewById(R.id.try_again_textview).setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    Intent intent = new Intent(SplashScreenActivity.this, SplashScreenActivity.class);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(intent);
//
//                                }
//                            });
//                        }
//                    });

                } catch (JSONException e) {
                    e.printStackTrace();

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            setContentView(R.layout.error_layout);
//
//                            findViewById(R.id.try_again_textview).setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View view) {
//                                    Intent intent = new Intent(SplashScreenActivity.this, SplashScreenActivity.class);
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    startActivity(intent);
//
//                                }
//                            });
//                        }
//                    });
                }
            }
        }).start();
    }

}
