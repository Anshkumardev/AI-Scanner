package akd.technologies.scanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.scanner.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TranslateActivity extends AppCompatActivity {

    private String url = "http://" + "10.0.2.2" + ":" + 5000 + "/translate";
    private String postBodyString;
    private MediaType mediaType;
    private RequestBody requestBody;
    private Button proceed;

    Uri imgUri;
    ImageView picked_image_viewer;

    String selectedImagePath;

    LottieAnimationView progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        //---------------------------------Python Connect-------------------------//

        proceed = findViewById(R.id.proceed_btn_translate);
        progressBar = findViewById(R.id.progressBarTranslate);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBar.setVisibility(View.VISIBLE);
                progressBar.playAnimation(); // Start the animation

                View darkOverlay = findViewById(R.id.dark_overlay);
                darkOverlay.setVisibility(View.VISIBLE);


                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;

                selectedImagePath = getPath(getApplicationContext(), imgUri);

                // Read BitMap by file path
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                RequestBody postBodyImage = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", "androidTranslate.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                        .build();


                postRequest(url, postBodyImage);

            }
        });


        //----------------------------Setting Image to imageView----------------//

        picked_image_viewer = findViewById(R.id.picked_image_viewer_translate);

        Bundle bundle = getIntent().getExtras();

        // if bundle is not null then get the image value
        if (bundle != null) {
            Bundle extras = getIntent().getExtras();
            imgUri = Uri.parse(extras.getString("imageUri"));
        }

        picked_image_viewer.setImageURI(imgUri);


    }

    //----------------------------Python Connection-----------------------------------//


    void postRequest(String postUrl, RequestBody postBody) {

        long contentLength = 1000;

        try {
            contentLength = postBody.contentLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Set the connection timeout
                .readTimeout(60, TimeUnit.SECONDS)     // Set the read timeout
                .build();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .header("Content-Length", String.valueOf(contentLength))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.cancelAnimation();
                        progressBar.setVisibility(View.GONE);
                        View darkOverlay = findViewById(R.id.dark_overlay);
                        darkOverlay.setVisibility(View.GONE);
                        Toast.makeText(TranslateActivity.this, "Something went wrong:" + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.i("error",e.toString());
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            progressBar.cancelAnimation();
                            progressBar.setVisibility(View.GONE);
                            View darkOverlay = findViewById(R.id.dark_overlay);
                            darkOverlay.setVisibility(View.GONE);
                            String translation = response.body().string();
                            Intent intent = new Intent(TranslateActivity.this, ResultActivity.class);
                            intent.putExtra("summary", translation);
                            intent.putExtra("title","Translation");
                            startActivity(intent);
                        } catch (IOException e) {
                            progressBar.cancelAnimation();
                            progressBar.setVisibility(View.GONE);
                            View darkOverlay = findViewById(R.id.dark_overlay);
                            darkOverlay.setVisibility(View.GONE);
                            Toast.makeText(TranslateActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }


    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
