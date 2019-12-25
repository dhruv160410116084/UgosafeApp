package droidad.contract.carrental.Adapters;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import droidad.contract.carrental.Activities.BookRequestActivity;
import droidad.contract.carrental.Activities.SelectCarActivity;
import droidad.contract.carrental.Activities.SelectLocationMapActivity;
import droidad.contract.carrental.Holders.CarHolder;
import droidad.contract.carrental.R;

public class CarListAdapter extends ArrayAdapter<CarHolder> {
    List<CarHolder> objects;
    SharedPreferences sharedPreferences;


    public CarListAdapter(@NonNull Context context, int resource, @NonNull List<CarHolder> objects) {
        super(context, resource, objects);
        objects=this.objects;
        sharedPreferences = getContext().getSharedPreferences("user_metadata", Context.MODE_PRIVATE);

    }

    @Nullable
    @Override
    public CarHolder getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.select_car_holder, parent, false);
        }



        TextView carName=(TextView)convertView.findViewById(R.id.car_name_textview);
        TextView seater=(TextView)convertView.findViewById(R.id.seater_textview);
        final TextView price=(TextView)convertView.findViewById(R.id.price_textview);
        Button book = (Button)convertView.findViewById(R.id.book_button);


        CarHolder carData=getItem(position);
        System.out.println("adapter-car");
        carName.setText(carData.getCarName());
        seater.setText(String.valueOf(carData.getSeater()));
        price.setText(String.valueOf(carData.getPrice()));

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle("Car Booking Conformation");
                alertDialog.setCancelable(false);
                alertDialog.setMessage("Click Yes to book car?");
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int SharedCost=Integer.parseInt(sharedPreferences.getString("cost", "0"));
                        int FinalCost=Integer.parseInt(price.getText().toString())+SharedCost;
                        SharedPreferences.Editor editor=sharedPreferences.edit();
                        editor.putString("cost", String.valueOf(FinalCost));
                        editor.commit();
                        Intent intent = ((Activity) getContext()).getIntent();
                        String ActivityType=intent.getExtras().getString("activity_type");
                        Intent i = new Intent(getContext(),BookRequestActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.putExtra("activity_type", ActivityType);
                        getContext().startActivity(i);
                        ((Activity) getContext()).finish();

                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("NO");


                    }
                });
                alertDialog.show();
            }
        });


        return convertView;
    }
}


