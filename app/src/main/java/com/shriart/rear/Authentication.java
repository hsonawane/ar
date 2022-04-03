package com.shriart.rear;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Authentication extends AppCompatActivity {

    EditText phoneNumber, otp;
    Button sendOtp, verifyOtp, resendOtp;

    String userMobileNumber, verificationId;
    FirebaseAuth firebaseAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    PhoneAuthProvider.ForceResendingToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        phoneNumber = findViewById(R.id.phoneNumber);
        sendOtp = findViewById(R.id.sendOtp);

        otp = findViewById(R.id.otp);
        verifyOtp = findViewById(R.id.verifyOtp);
        resendOtp = findViewById(R.id.resendOtp);
        resendOtp.setEnabled(false);


        firebaseAuth = FirebaseAuth.getInstance();

        sendOtp.setOnClickListener(view -> {
            if(phoneNumber.getText().toString().isEmpty()){
                phoneNumber.setError("Mobile Number is required");
            }
            userMobileNumber = "+91"+phoneNumber.getText().toString();
            verifyMobileNumber(userMobileNumber);

            Toast.makeText(this, phoneNumber.getText().toString(), Toast.LENGTH_SHORT).show();

        });

        verifyOtp.setOnClickListener(view -> {
            if(otp.getText().toString().isEmpty()){
                otp.setError("Enter OTP first");
                return;
            }
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp.getText().toString());
            authenticateUser(credential);
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                authenticateUser(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(Authentication.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                verificationId = s;
                token = forceResendingToken;

                phoneNumber.setVisibility(View.GONE);
                sendOtp.setVisibility(View.GONE);

                otp.setVisibility(View.VISIBLE);
                resendOtp.setVisibility(View.VISIBLE);
                resendOtp.setEnabled(false);
                verifyOtp.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                resendOtp.setEnabled(true);
            }
        };
    }



    public void verifyMobileNumber(String mobileNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setActivity(this)
                .setPhoneNumber(mobileNumber)
                .setTimeout(60l, TimeUnit.SECONDS)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void authenticateUser(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(Authentication.this, "Success", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Authentication.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}