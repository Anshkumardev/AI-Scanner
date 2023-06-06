package akd.technologies.scanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.scanner.R;

public class ResultActivity extends AppCompatActivity {


    TextView summaryTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String summary = intent.getStringExtra("summary");

        summaryTextView = findViewById(R.id.summary_textView);

        if (summary != null){
            summaryTextView.setText(summary);
        }


    }
}