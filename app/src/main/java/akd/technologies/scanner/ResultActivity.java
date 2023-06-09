package akd.technologies.scanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scanner.R;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;


public class ResultActivity extends AppCompatActivity {


    TextView summaryTextView;

    String newFileName;
    TextView dynamicTitle;

    String summary;

    Button save_button;

    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        dynamicTitle = findViewById(R.id.dynamicTitle);

        save_button = findViewById(R.id.save_btn);

        progressBar = findViewById(R.id.progressBar4);

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRenameDialog();
            }
        });

        Intent intent = getIntent();
        summary = intent.getStringExtra("summary");
        String title = intent.getStringExtra("title");

        dynamicTitle.setText(title);

        summaryTextView = findViewById(R.id.summary_textView);

        if (summary != null){
            summaryTextView.setText(summary);
        }


    }

    private void showRenameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename File");

        // Create an EditText field for user input
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newFileName = input.getText().toString();

                save_button.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                saved saved = new saved();
                saved.setName(newFileName);
                saved.setContent(summary);
                saved.setTimestamp(Timestamp.now());

                saveContentToFirebase(saved,newFileName);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    void saveContentToFirebase(saved saved,String newFileName){
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForNotes().document(newFileName);

        documentReference.set(saved).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    save_button.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ResultActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                    Intent intent = new Intent(ResultActivity.this,HomeActivity.class);
                    startActivity(intent);
                }else {
                    save_button.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ResultActivity.this, "Failed while saving", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
