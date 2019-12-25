package droidad.contract.carrental.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;

import droidad.contract.carrental.Activities.BookRequestActivity;
import droidad.contract.carrental.Activities.SignUpActivity;

import static droidad.contract.carrental.Activities.SignUpActivity.ActivityForLocation;


public class FindCurrentLocationService extends BroadcastReceiver {
    public static final String ACTION_PROCESS_UPDATE="droidad.contract.carrental.UPDATE_LOCATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent!=null){
            final String action=intent.getAction();
            if(ACTION_PROCESS_UPDATE.equals(action)){
                LocationResult result=LocationResult.extractResult(intent);
                if(result!=null){
                    Location location=result.getLastLocation();
//                    String location_string=new StringBuilder(" "+ location.getLatitude())
//                            .append("/")
//                            .append(location.getLongitude())
//                            .toString();

                    String lat=new StringBuilder(""+location.getLatitude()).toString();
                    String lng=new StringBuilder(""+location.getLongitude()).toString();

                    try{
//                      System.out.println("In the try "+ActivityForLocationBookRequest.equals("BookRequestActivity"));
//                      System.out.println("type"+ActivityForLocationBookRequest.getClass().getName());
                        if(ActivityForLocation.equals("SignUpActivity")){
                            System.out.println("signupactivity called");
                            SignUpActivity.getInstance().retriveLatLang(lat, lng);
                        }
                        if(ActivityForLocation.equals("BookRequestActivity")  == true){
                            System.out.println("BookRequiestActiviey called");
                            BookRequestActivity.getInstance().retriveLatLang(lat, lng);
                        }

                    }catch (Exception e){
                        //Toast.makeText(context, location_string, Toast.LENGTH_SHORT).show();
                        System.out.println("error:=>"+e);
                    }
                }
            }
        }
    }

}
