package droidad.contract.carrental.Activities;

import androidx.appcompat.app.AppCompatActivity;
import droidad.contract.carrental.R;

import android.os.Bundle;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        System.out.println("about us");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("About us");
    }
}
