package com.example.arife.mobileprogrammingproject;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * User Login process; checking user was saved in app
 */

public class LogInFragment extends Fragment implements View.OnClickListener {
    private TextView errorText;
    private EditText mailText;
    private EditText passwordText;
    private Button logInButton;

    private String userMail;
    private String userPassword;

    private FirebaseAuth mAuth;
    private static String TAG ="Sign in activity";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.login_fragment,container,false);

        mailText=v.findViewById(R.id.input_email);
        passwordText = v.findViewById(R.id.input_password);
        logInButton = v.findViewById(R.id.btn_login);
        logInButton.setOnClickListener(this);
        errorText = v.findViewById(R.id.errorText);

        //database kullancı bağlantısı
        mAuth = FirebaseAuth.getInstance();

        return v;
    }

    //checking user
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_login && validate()){
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Giriş yapılıyor...");
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(userMail, userPassword)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                final FirebaseUser user = mAuth.getCurrentUser();
                                new android.os.Handler().postDelayed(
                                        new Runnable() {
                                            public void run() {
                                                updateUI(user);
                                                progressDialog.dismiss();
                                            }
                                        }, 3000);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());

                                new android.os.Handler().postDelayed(
                                        new Runnable() {
                                            public void run() {
                                                updateUI(null);
                                                progressDialog.dismiss();
                                            }
                                        }, 3000);
                            }

                        }
                    });
        }
    }

    //go to home page
    private void updateUI(FirebaseUser user) {
        //burası ne yapacağına karar vereceğin yer
        if(user != null){

            Intent i = new Intent(getActivity(),HomePageActivity.class);
            startActivity(i);
            //finish();
        }
        else
            errorText.setText("Hata oluştur lütfen tekrar deneyin.");
    }

    //error check on layout
     public boolean validate(){
         boolean valid = true;

         userMail = mailText.getText().toString();
         userPassword = passwordText.getText().toString();

         if (userMail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(userMail).matches()) {
             mailText.setError("Geçerli bir mail adresi girin");
             valid = false;
         } else {
             mailText.setError(null);
         }

         if (userPassword.isEmpty() || userPassword.length() < 4 || userPassword.length() > 10) {
             passwordText.setError("Lütfen geçerli bir şifre girin");
             valid = false;
         } else {
             passwordText.setError(null);
         }

         return valid;
     }
}
