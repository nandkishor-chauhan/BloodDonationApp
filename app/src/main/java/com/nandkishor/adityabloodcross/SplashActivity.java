package com.nandkishor.adityabloodcross;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        refresh();
    }

    void refresh(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                    if (new CommonAdapter(SplashActivity.this).isNetworkAvailable()){
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        if (mAuth.getCurrentUser()!=null){
                            startActivity(new Intent(SplashActivity.this,HomeActivity.class));
                            finish();
                        }else {
                            startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                            finish();
                        }
                    }else {
                        RelativeLayout splashLayout = findViewById(R.id.splashLayout);
                        Snackbar.make(splashLayout,"No Internet",Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                refresh();
                            }
                        }).show();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
