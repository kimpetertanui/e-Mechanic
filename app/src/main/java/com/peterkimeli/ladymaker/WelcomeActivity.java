package com.peterkimeli.ladymaker;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {
    private Button WelcomeDriverButton;
    private Button WelcomeCustomerButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        WelcomeDriverButton = (Button)findViewById(R.id.welcome_driver_btn);
        WelcomeCustomerButton=(Button)findViewById(R.id.welcome_custtomer_btn);

        WelcomeDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent about =new Intent(WelcomeActivity.this, DriverLoginRegisterActivity.class);
                startActivity(about);
            }
        });

        WelcomeCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profile= new Intent(WelcomeActivity.this,CustomerLoginRegisterActivity.class);
                startActivity(profile);

            }
        });
    }
}
