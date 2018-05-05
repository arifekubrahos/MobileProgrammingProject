package com.example.arife.mobileprogrammingproject;

import android.support.v4.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    //remove eklenecek
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorize);
        fragmentManager = getFragmentManager();

    }
    public void signUp(View v){
        SignUpActivity signUpActivity = new SignUpActivity();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment,signUpActivity);
        fragmentTransaction.commit();
    }
    public void logIn(View v){
        LogInActivity logInActivity = new LogInActivity();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment,logInActivity);
        fragmentTransaction.commit();
    }

}
