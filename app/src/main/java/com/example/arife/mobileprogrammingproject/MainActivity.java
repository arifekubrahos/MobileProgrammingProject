package com.example.arife.mobileprogrammingproject;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
        fragmentTransaction = fragmentManager.beginTransaction();
        if(deleteLog()){
            SignUpFragment signUpFragment = new SignUpFragment();
            fragmentTransaction.add(R.id.fragment, signUpFragment, "signupfragment");
            fragmentTransaction.commit();
        }
        else{
            Toast.makeText(getApplicationContext(), "Beklenmeyen bir hata oluştu", Toast.LENGTH_LONG).show();
        }

    }
    public boolean deleteLog(){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LogInFragment logInActivity = (LogInFragment) fragmentManager.findFragmentByTag("loginfragment");
        if(logInActivity != null){
            fragmentTransaction.remove(logInActivity);
            fragmentTransaction.commit();
            return true;
        }
        else if(fragmentTransaction.isEmpty()){
            return true;
        }
        else{
            return false;
        }
    }
    public void logIn(View v){
        fragmentTransaction = fragmentManager.beginTransaction();
        if(deleteSign()){
            LogInFragment logInActivity = new LogInFragment();
            fragmentTransaction.add(R.id.fragment,logInActivity, "loginfragment");
            fragmentTransaction.commit();
        }

    }
    public boolean deleteSign(){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SignUpFragment signUpFragment = (SignUpFragment) fragmentManager.findFragmentByTag("signupfragment");
        if(signUpFragment != null){
            fragmentTransaction.remove(signUpFragment);
            fragmentTransaction.commit();
            return true;
        }
        else if(fragmentTransaction.isEmpty()){
            return true;
        }
        else{
            return false;
        }
    }
}
