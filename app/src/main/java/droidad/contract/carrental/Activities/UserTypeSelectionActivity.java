package droidad.contract.carrental.Activities;

import androidx.appcompat.app.AppCompatActivity;
import droidad.contract.carrental.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserTypeSelectionActivity extends AppCompatActivity {

    Button CarRenterButton, CarOwnerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type_selection);

        CarRenterButton=(Button)findViewById(R.id.car_renter_button);
        CarOwnerButton=(Button)findViewById(R.id.car_owner_button);



        CarRenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserTypeSelectionActivity.this,BookRequestActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("activity_type", "Try");
                startActivity(intent);
            }
        });

        CarOwnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserTypeSelectionActivity.this,LogInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
        });
    }
}
