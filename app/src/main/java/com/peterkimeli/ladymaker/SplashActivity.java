package com.peterkimeli.ladymaker;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Thread thread=new Thread(){
            @Override
            public void run(){
                try
                {
                    sleep(4000);

                }
                catch ( Exception e){
                    e.printStackTrace();

                }

                finally {
                    Intent welcomeintent=new Intent(SplashActivity.this,WelcomeActivity.class);
                    startActivity(welcomeintent);

                }
            }
        };
        thread.start();
    }
    @Override
    public void onPause(){
        super.onPause();
    }

}
