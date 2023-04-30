package akd.technologies.scanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scanner.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    EditText username,password;
    Button signUp;
    FirebaseAuth auth;
    TextView signin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.signup_username_editText);
        password = findViewById(R.id.signup_password_editText);
        signUp = findViewById(R.id.signup_button);
        auth = FirebaseAuth.getInstance();

        //----------------------------Redirect to sign in--------------------------//

        signin = findViewById(R.id.signin_on_register_page);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                finish();
            }
        });


        //---------------------------------------Register New User---------------------------------//

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }
        });
    }



    //-------------------------- creating user to register---------------------//
    private void createUser(){
        String email = username.getText().toString();
        String signupPassword = password.getText().toString();

        if(TextUtils.isEmpty(email)){
            username.setError("Email cannot be empty");
            username.requestFocus();
        }else if (TextUtils.isEmpty(signupPassword)){
            password.setError("Password cannot be empty");
            password.requestFocus();
        }else{
            auth.createUserWithEmailAndPassword(email,signupPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this,HomeActivity.class));
                    }else{
                        Toast.makeText(RegisterActivity.this, "Registration Error: "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if(user != null){
            startActivity(new Intent(RegisterActivity.this,HomeActivity.class));
        }
    }
}