package droidad.contract.carrental.Activities;

import androidx.appcompat.app.AppCompatActivity;
import droidad.contract.carrental.Adapters.CarListAdapter;
import droidad.contract.carrental.Holders.CarHolder;
import droidad.contract.carrental.R;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;


public class SelectCarActivity extends AppCompatActivity {
    ArrayList<CarHolder> carArrayList;
    ListView list;
    CarListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_car);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Select Car");

        list = (ListView)findViewById(R.id.carList);
        carArrayList = new ArrayList<>();

        carArrayList.add(new CarHolder("Suziki swift",4,500));
        carArrayList.add(new CarHolder("Bolero",6,350));
        carArrayList.add(new CarHolder("Desire",8,400));
        carArrayList.add(new CarHolder("Ferari",4,3000));
        carArrayList.add(new CarHolder("Omni",4,200));
        carArrayList.add(new CarHolder("Renault Duster",6,600));
        carArrayList.add(new CarHolder("Honda city",6,500));
        carArrayList.add(new CarHolder("Vitara breza",8,450));
        carArrayList.add(new CarHolder("Skoda Octevia",10,250));
        adapter = new CarListAdapter(this,R.layout.select_car_holder,carArrayList);
        list.setAdapter(adapter);

    }
}
