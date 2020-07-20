package com.example.cabs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
Button up,ch;
ImageView img;
EditText name,number,email;
DatabaseReference ref;
User user;
    private static final int REQUEST_CAMERA = 1337;

    private Uri resultUri;
    private FirebaseAuth auth;

    public static final String Name = "nameKey";
    private String userid;
    private String mName,mPhone;
    private String mImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ch = findViewById(R.id.btnChoose);
        up = findViewById(R.id.btn_Update);
        img = findViewById(R.id.imageView2);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Filechooser();
            }
        });
        name = findViewById(R.id.edit_name);
        number = findViewById(R.id.edit_number);
        auth = FirebaseAuth.getInstance();
        userid = auth.getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference().child("Users").child("Passenger").child(userid);
        getUserInfo();
        email = findViewById(R.id.edit_email);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personEmail = acct.getEmail();
            email.setText(personEmail);
        }
        ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             Filechooser();
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
                Intent in = new Intent(ProfileActivity.this,MapsActivity.class);
                startActivity(in);
            }
        });

    }


    private void saveUserInformation() {
        mName = name.getText().toString();
        mPhone = number.getText().toString();
       Map userInfo = new HashMap();
       userInfo.put("name", mName);
       userInfo.put("phone",mPhone);
       ref.updateChildren(userInfo);
       if(resultUri != null){
           final StorageReference filepath = FirebaseStorage.getInstance().getReference().child("Passenger_images").child(userid);
           Bitmap bitmap = null;
           try{
               bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);
           }catch (IOException e){
               e.printStackTrace();
           }
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
           bitmap.compress(Bitmap.CompressFormat.JPEG,20,baos);
           byte[] data = baos.toByteArray();
           UploadTask uploadTask = filepath.putBytes(data);
           uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
               @Override
               public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                     @Override
                     public void onSuccess(Uri uri) {
                         Map newImage = new HashMap();
                         newImage.put("profileImageUrl",uri.toString());
                         ref.updateChildren(newImage);
                         return;
                     }
                 });
               }
           });
       }
    }


    private  void getUserInfo(){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){
                        mName = map.get("name").toString();
                        name.setText(mName);
                    }
                    if(map.get("phone") != null){
                        mPhone = map.get("phone").toString();
                        number.setText(mPhone);
                    }
                    if(map.get("profileImageUrl") != null){
                        mImg = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mImg).into(img);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void Filechooser()
    {
        final CharSequence[] items={"Camera","Gallery","Remove Photo","Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,REQUEST_CAMERA);
                }
                else if (items[i].equals("Gallery")) {

                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent,1);

                } else if (items[i].equals("Remove Photo")) {
                     img.setImageDrawable(getResources().getDrawable(R.drawable.images));
                     dialogInterface.dismiss();

                }else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == Activity.RESULT_OK)
        {
           final Uri imageUri = data.getData();
            resultUri = imageUri;
            img.setImageURI(resultUri);
        }

        if (requestCode== REQUEST_CAMERA)
        {
            Bitmap imageData = (Bitmap) data.getExtras().get("data");
            img.setImageBitmap(imageData);
        }
    }
    @Override
    public void onBackPressed() {
        this.finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
