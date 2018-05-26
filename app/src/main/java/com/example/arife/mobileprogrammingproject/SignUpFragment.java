package com.example.arife.mobileprogrammingproject;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Patterns;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * User signup process; adding firebase new user authentication and also database that is named "Users"
 */

public class SignUpFragment extends Fragment implements View.OnClickListener {

    private TextView errorText;
    private EditText nameText;
    private EditText mailText;
    private EditText passwordText;
    private EditText passwordConfirmText;
    private Button signUpButton;

    private String userMail;
    private String userPassword;
    private String userConfirmPassword;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReferance;
    private static String TAG ="Sign in activity";
    private String userName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.signup_fragment,container,false);

        nameText = v.findViewById(R.id.input_name);
        mailText=v.findViewById(R.id.input_email);
        passwordText = v.findViewById(R.id.input_password);
        passwordConfirmText = v.findViewById(R.id.input_confirm_password);
        signUpButton = v.findViewById(R.id.btn_signup);
        signUpButton.setOnClickListener(this);
        errorText = v.findViewById(R.id.errorText);
        //database kullancı bağlantısı
        mAuth = FirebaseAuth.getInstance();

        return v;
    }

    //add database, go to home page
    private void updateUI(FirebaseUser user) {

        signUpButton.setEnabled(true);

        if(user != null){
            mDatabaseReferance = FirebaseDatabase.getInstance().getReference("Users");
            User newUser = new User();
            newUser.setName(userName);
            newUser.setMail(userMail);
            newUser.setHelpCount(-1);
            mDatabaseReferance.child(user.getUid()).setValue(newUser);

            Intent i = new Intent(getActivity(),HomePageActivity.class);
            startActivity(i);
        }
        else{
            errorText.setText("Hata oluştu lütfen tekrar deneyin.");
        }
    }

    //adding authentication on firebase
    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.btn_signup && validate()){
            signUpButton.setEnabled(false);
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Hesap Oluşturuluyor...");
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(userMail,userPassword)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithuserMail:success");
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
                                Log.w(TAG, "createUserWithuserMail:failure", task.getException());
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

    //error check on layout
    public boolean validate(){
        boolean valid = true;

        userName = nameText.getText().toString();
        userMail = mailText.getText().toString();
        userPassword = passwordText.getText().toString();
        userConfirmPassword = passwordConfirmText.getText().toString();

        if (userName.isEmpty() || userName.length() < 3) {
            nameText.setError("Lütfen en az üç karakter giriniz!");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (userMail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(userMail).matches()) {
            mailText.setError("Lütfen geçerli bir mail adresi giriniz");
            valid = false;
        } else {
            mailText.setError(null);
        }

        if (userPassword.isEmpty() || userPassword.length() < 4 || userPassword.length() > 10) {
            passwordText.setError("en az 6 karakterli ve içinde sayı ve yıldız bulundurun!");
            valid = false;
        } else {
            passwordText.setError(null);
        }
        if(!userConfirmPassword.equals(userPassword)){
            passwordText.setError("Şifreler aynı değil");
            passwordConfirmText.setError("");
            valid = false;
        }else{
            passwordText.setError(null);
            passwordConfirmText.setError(null);
        }
        return valid;
    }
}
