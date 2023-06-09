package akd.technologies.scanner;

import static android.os.Build.VERSION_CODES.Q;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scanner.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.Query;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;
    private static final int IMAGE_PICK_CAMERA_CODE = 1001;

    ImageView view_image;

    Toolbar toolbar;

    RecyclerView recyclerView;

    SavedAdapter savedAdapter;

    String cameraPermission[];
    String storagePermission[];

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Uri image_uri;

    TextView username,email;
    Button signOut;

    CardView translate_and_summarize,translate;

    ImageView camera_image,gallery_image;
    private boolean fromTranslate;

    FirebaseAuth auth;
    FirebaseDatabase database;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    FloatingActionButton add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        add = findViewById(R.id.add_note);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, savedActivity.class);
                startActivity(intent);
            }
        });


        //------------------------------------Recycler View----------------------------------//

        recyclerView = findViewById(R.id.recyclerView);
        
        setUpRecyclerView();


        //---------------------------------------App Drawer------------------------------------//

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("AI Scanner");

        navigationView.bringToFront();


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);




        username = findViewById(R.id.guserName);

        Dialog dialog = new Dialog(HomeActivity.this);

        translate_and_summarize = findViewById(R.id.translate_summarize);
        translate = findViewById(R.id.translate);

        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};

        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //------------------------Redirect to Translate and summarize page--------------------//

        translate_and_summarize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fromTranslate = false;

                dialog.setContentView(R.layout.image_selection_dialog);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(true);

                camera_image = dialog.findViewById(R.id.camera_selection);
                gallery_image = dialog.findViewById(R.id.gallery_selection);

                camera_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(!checkCameraPermission()){
                            requestCameraPermission();
                        }
                        else{
                            pickCamera();
                            dialog.dismiss();
                        }
                    }
                });

                gallery_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!checkStoragePermission()){

                            requestStoragePermission();
                        }
                        else {
                            pickGallery();
                            dialog.dismiss();
                        }
                    }
                });

                dialog.show();

            }
        });

        //----------------------------------Translate -------------------------------//

        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromTranslate = true;

                dialog.setContentView(R.layout.image_selection_dialog);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(true);

                camera_image = dialog.findViewById(R.id.camera_selection);
                gallery_image = dialog.findViewById(R.id.gallery_selection);

                camera_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(!checkCameraPermission()){
                            requestCameraPermission();
                        }
                        else{
                            pickCamera();
                            dialog.dismiss();
                        }
                    }
                });

                gallery_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!checkStoragePermission()){

                            requestStoragePermission();
                        }
                        else {
                            pickGallery();
                            dialog.dismiss();
                        }
                    }
                });

                dialog.show();
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




    }

    //-----------------------------------Setting recyclerview items-----------------------------//44
    private void setUpRecyclerView() {

        Query query = Utility.getCollectionReferenceForNotes().orderBy("timestamp",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<saved> options = new FirestoreRecyclerOptions.Builder<saved>().setQuery(query, saved.class).build();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        savedAdapter = new SavedAdapter(options,this);
        recyclerView.setAdapter(savedAdapter);
    }

    private void pickGallery() {


        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("return-data", true);
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New AI Scanner Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image Translation");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent CameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        CameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(CameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);

    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {

        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    //--------------------check camera permission--------------------------//

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)  ==  (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result1 && result;
    }

    //--------------------------Handling Permission Result---------------------//


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:

                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    //-----------------------------Handling Image Result-----------------------//


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                image_uri = data.getData();

            }


            if(fromTranslate){
                Intent sendimage = new Intent(HomeActivity.this, TranslateActivity.class);
                sendimage.putExtra("imageUri", image_uri.toString());
                startActivity(sendimage);
            }else {
                Intent sendimage = new Intent(HomeActivity.this, ImageViewActivity.class);
                sendimage.putExtra("imageUri", image_uri.toString());
                startActivity(sendimage);
            }


        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.info_nav) {
            // Handle item 1 click
            Toast.makeText(this, "Info Selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.logout_nav) {
            // Handle item 2 click
            signOut();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {

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

    @Override
    public void onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        savedAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        savedAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        savedAdapter.notifyDataSetChanged();
    }
}

