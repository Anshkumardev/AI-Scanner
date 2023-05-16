package akd.technologies.scanner;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.scanner.R;

public class ImageViewActivity extends AppCompatActivity {

    Spinner input_spinner, output_spinner;

    Uri imgUri;
    ImageView picked_image_viewer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        //----------------------------Setting Image to imageView----------------//

        picked_image_viewer = findViewById(R.id.picked_image_viewer);

        Bundle bundle = getIntent().getExtras();

        // if bundle is not null then get the image value
        if (bundle != null) {
            Bundle extras = getIntent().getExtras();
            imgUri = Uri.parse(extras.getString("imageUri"));
        }

        picked_image_viewer.setImageURI(imgUri);

        input_spinner = findViewById(R.id.input_language_spinner);
        output_spinner = findViewById(R.id.output_language_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.input_language, android.R.layout.simple_spinner_dropdown_item);
            // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
        input_spinner.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.output_language, android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        output_spinner.setAdapter(adapter1);
    }
}