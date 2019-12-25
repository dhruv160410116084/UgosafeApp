package droidad.contract.carrental.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import droidad.contract.carrental.Holders.RentRequestData;
import droidad.contract.carrental.R;

public class OwnerRequestAdapter extends ArrayAdapter<RentRequestData> {

    List<RentRequestData> objects;

    public OwnerRequestAdapter(@NonNull Context context, int resource, @NonNull List<RentRequestData> objects) {
        super(context, resource, objects);
        objects=this.objects;
    }

    @Nullable
    @Override
    public RentRequestData getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.owner_request_list_layout, parent, false);
        }

        TextView customerId=(TextView)convertView.findViewById(R.id.customer_id_textview);
        TextView startDate=(TextView)convertView.findViewById(R.id.start_date_textview);
        TextView endDate=(TextView)convertView.findViewById(R.id.end_date_textview);
        TextView journeyStatus=(TextView)convertView.findViewById(R.id.journey_status_textview);
        TextView rent_cost=(TextView)convertView.findViewById(R.id.rent_cost_textview);

        RentRequestData rentRequestData=getItem(position);
        customerId.setText(rentRequestData.getCustomerId());
        startDate.setText(rentRequestData.getRentStartDate());
        endDate.setText(rentRequestData.getRentEndDate());
        if(rentRequestData.getStatus().equals("notStarted")){
            journeyStatus.setText("Not Started Yet");
        }else  if(rentRequestData.getStatus().equals("started")){
            journeyStatus.setText("Started");
        }else if(rentRequestData.getStatus().equals("finished")){
            journeyStatus.setText("Finished");
        }
        rent_cost.setText(rent_cost.getText().charAt(0)+String.valueOf(rentRequestData.getCost()));




        return convertView;
    }
}
