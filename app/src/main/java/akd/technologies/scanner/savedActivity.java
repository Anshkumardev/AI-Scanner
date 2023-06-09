package akd.technologies.scanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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

public class savedActivity extends AppCompatActivity {

    String title_data,content_data,docId;
    Button saveBtn;
    CardView deleteNote;

    ProgressBar progressBar,progressBarDelete;

    boolean isEditMode = false;

    EditText content,title;

    int color = Color.parseColor("#FFFFC107");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        saveBtn = findViewById(R.id.save_and_edit_btn);

        title = findViewById(R.id.savedTitle);

        progressBarDelete = findViewById(R.id.progressBar5);
        progressBarDelete.setIndeterminateTintList(ColorStateList.valueOf(color)); // Set the desired color


        content = findViewById(R.id.saved_textView);

        title_data = getIntent().getStringExtra("title");
        content_data = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");

        progressBar = findViewById(R.id.progressBar3);

        //-------------------------------Delete note--------------------------//
        deleteNote = findViewById(R.id.delete);

        title.setText(title_data);
        content.setText(content_data);

        if (title_data!=null && !title_data.isEmpty()){
            isEditMode = true;
        }

        title.setHintTextColor(Color.GRAY);
        content.setHintTextColor(Color.GRAY);

        if(isEditMode){
            title.setEnabled(false);
            title.setFocusable(false);
            title.setFocusableInTouchMode(false);
            title.setTextColor(Color.GRAY);
            deleteNote.setVisibility(View.VISIBLE);
        }

        deleteNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteNote.setVisibility(View.INVISIBLE);
                progressBarDelete.setVisibility(View.VISIBLE);
                deleteNoteFromFirebase(title.getText().toString());
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                saveBtn.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                String newTitle = title.getText().toString();
                String newContent = content.getText().toString();

                if(TextUtils.isEmpty(newTitle)){
                    title.setError("Title cannot be empty");
                    title.requestFocus();
                    saveBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }else if (TextUtils.isEmpty(newContent)) {
                    content.setError("Text cannot be empty");
                    content.requestFocus();
                    saveBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }else {

                    saved saved = new saved();
                    saved.setName(title.getText().toString());
                    saved.setContent(content.getText().toString());
                    saved.setTimestamp(Timestamp.now());

                    saveContentToFirebase(saved,newTitle);
                }

            }
        });

    }

    private void deleteNoteFromFirebase(String name) {

        DocumentReference documentReference;


        documentReference = Utility.getCollectionReferenceForNotes().document(name);

        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    saveBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(savedActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                    Intent intent = new Intent(savedActivity.this,HomeActivity.class);
                    startActivity(intent);
                }else {
                    progressBar.setVisibility(View.GONE);
                    saveBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(savedActivity.this, "Failed while deleting", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void saveContentToFirebase(saved saved,String newFileName){
        DocumentReference documentReference;


        documentReference = Utility.getCollectionReferenceForNotes().document(newFileName);

        documentReference.set(saved).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressBarDelete.setVisibility(View.GONE);
                    deleteNote.setVisibility(View.VISIBLE);
                    Toast.makeText(savedActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                    Intent intent = new Intent(savedActivity.this,HomeActivity.class);
                    startActivity(intent);
                }else {
                    progressBarDelete.setVisibility(View.GONE);
                    deleteNote.setVisibility(View.VISIBLE);
                    Toast.makeText(savedActivity.this, "Failed while saving", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}