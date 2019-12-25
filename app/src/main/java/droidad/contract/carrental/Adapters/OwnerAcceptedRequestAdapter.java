package droidad.contract.carrental.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import droidad.contract.carrental.Holders.RentRequestData;
import droidad.contract.carrental.R;

public class OwnerAcceptedRequestAdapter extends ArrayAdapter<RentRequestData> {

    List<RentRequestData> objects;

    public OwnerAcceptedRequestAdapter(@NonNull Context context, int resource, @NonNull List<RentRequestData> objects) {
        super(context, resource, objects);
        this.objects = objects;
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
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.owner_accepted_requests_list_layout, parent, false);
        }

        TextView customerId=(TextView)convertView.findViewById(R.id.customer_id_textview);
        TextView journeyStatus=(TextView)convertView.findViewById(R.id.journey_status_textview);

        RentRequestData rentRequestData=getItem(position);
        if(rentRequestData.getStatus().equals("notStarted")){
            journeyStatus.setText("Not Started Yet");
        }else  if(rentRequestData.getStatus().equals("started")){
            journeyStatus.setText("Started");
        }else if(rentRequestData.getStatus().equals("finished")){
            journeyStatus.setText("Finished");
        }
        customerId.setText(rentRequestData.getCustomerId());


        return convertView;
    }
}
