package akd.technologies.scanner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.scanner.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    TextView username,email;
    Button signOut;

    CardView translate_and_summarize,translate;


    FirebaseAuth auth;
    FirebaseDatabase database;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        username = findViewById(R.id.guserName);
        //email = findViewById(R.id.gemail);
        signOut = findViewById(R.id.signOutButton);

        translate_and_summarize = findViewById(R.id.translate_summarize);
        translate = findViewById(R.id.translate);

        //------------------------Redirect to Translate and summarize page--------------------//

        translate_and_summarize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //do something
            }
        });

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this,gso);

        FirebaseUser user = auth.getCurrentUser();

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null){
            String personName = acct.getDisplayName();
            //String personEmail = acct.getEmail();
            username.setText(personName);
            //email.setText(personEmail);
        }

        if (user != null){
            String personName = user.getDisplayName();
            String personEmail = user.getEmail();
            username.setText(personName);
            //email.setText(personEmail);
        }


        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signout();
            }
        });

    }

    void signout(){
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseUser user = auth.getCurrentUser();
        if(acct!=null){
            gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    finish();
                    startActivity(new Intent(HomeActivity.this,MainActivity.class));
                }
            });
            
        }

        if (user != null) {
            auth.signOut();
            startActivity(new Intent(HomeActivity.this,MainActivity.class));
        }

    }
}