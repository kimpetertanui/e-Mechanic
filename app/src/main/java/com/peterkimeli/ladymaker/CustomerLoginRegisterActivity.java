package com.peterkimeli.ladymaker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginRegisterActivity extends AppCompatActivity {
    private Button CustomerLoginButton;
    private Button CustomerRegisterButton;
    private TextView CustomerRegisterLink;
    private TextView CustomerStatus;
    private EditText EmailCustomer;
    private EditText PasswordCustomer;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference CustomerDatabaseRef;
    private String onlineCustomerID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login_register);
        mAuth=FirebaseAuth.getInstance();



        CustomerLoginButton =(Button) findViewById(R.id.customer_login_button);
        CustomerRegisterButton=(Button)findViewById(R.id.customer_register_btn);
        CustomerRegisterLink =(TextView)findViewById(R.id.register_customer_link);
        CustomerStatus=(TextView) findViewById(R.id.customer_status);
        EmailCustomer=(EditText)findViewById(R.id.email_customer);
        PasswordCustomer=(EditText) findViewById(R.id.editText2);
        loadingBar=new ProgressDialog(this);


        CustomerRegisterButton.setVisibility(View.INVISIBLE);
        CustomerRegisterButton.setEnabled(false);

        CustomerRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomerLoginButton.setVisibility(View.INVISIBLE);
                CustomerRegisterLink.setVisibility(View.INVISIBLE);
                CustomerStatus.setText("Register Customer");
                CustomerRegisterButton.setVisibility(View.VISIBLE);
                CustomerRegisterButton.setEnabled(true);
            }
        });

        CustomerRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email= EmailCustomer.getText().toString();
                String password= PasswordCustomer.getText().toString();

                RegisterCustomer(email,password);
            }
        });

        CustomerLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=EmailCustomer.getText().toString();
                String password=PasswordCustomer.getText().toString();
                SignInCustomer(email,password);
            }
        });


    }

    private void SignInCustomer(String email, String password)
    {
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this,"Please enter email",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(CustomerLoginRegisterActivity.this,"Please enter password",Toast.LENGTH_SHORT).show();
        }

        else
            {
              loadingBar.setTitle("Driver SignIn");
              loadingBar.setMessage("Please wait for  while checking your credentials.....");
              loadingBar.show();
              mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Intent driverIntent=new Intent(CustomerLoginRegisterActivity.this, CustomersMapActivity.class);
                                startActivity(driverIntent);
                                Toast.makeText(CustomerLoginRegisterActivity.this,"You have  successfullly logged in",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                            }
                            else  {

                                Toast.makeText(CustomerLoginRegisterActivity.this,"Login Failed. Try again!!!",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });

        }

    }

    private void RegisterCustomer(String email, String password) {
        if (TextUtils.isEmpty(email)){
            Toast.makeText(CustomerLoginRegisterActivity.this,"Please enter email",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(CustomerLoginRegisterActivity.this,"Please enter password",Toast.LENGTH_SHORT).show();
        }

        else {
            loadingBar.setTitle("Driver Registration");
            loadingBar.setMessage("Please wait for complete registration");
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                onlineCustomerID=mAuth.getCurrentUser().getUid();
                                CustomerDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(onlineCustomerID);

                                CustomerDatabaseRef.setValue(true);
                                Intent customerIntent =new Intent(CustomerLoginRegisterActivity.this,CustomersMapActivity.class);
                                startActivity(customerIntent);

                                Toast.makeText(CustomerLoginRegisterActivity.this,"You have registered successfullly",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();


                            }
                            else
                                {

                                Toast.makeText(CustomerLoginRegisterActivity.this,"Registration Failed",Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }

                        }
                    });

        }
    }
}
