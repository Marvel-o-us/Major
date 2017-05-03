package in.vestapp.major;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by rahul on 4/5/17.
 */

public class MainActivity extends Activity {

    private static final int REQUEST_GALLERY = 1000;
    private static final int STORAGE_PERMISSION_CODE = 1001;
    public static final String BASE_URL = "http://localhost:8000";

    ImageView postImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        postImage = (ImageView) findViewById(R.id.uploadImage);
    }

    public void upload(View v){
        if(isStorageAllowed()){
            galleryPicker();
        }else {
            requestStoragePermission();
        }

    }

    private void galleryPicker(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY);
    }


    private boolean isStorageAllowed() {
        //Getting the permission status

        int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //If permission is granted returning true
        if (result2 == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Storage Permission is required for you to upload photos/videos through VestaSocial", Toast.LENGTH_SHORT).show();
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }



    boolean askedForPermissionContacts;

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                galleryPicker();
            } else {
                if (askedForPermissionContacts) {
                    Toast.makeText(this, "Allow vesta storage permission to access your videos/photos in the settings of vesta App", Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(this, "Oops you just denied the permission.Please Try Again", Toast.LENGTH_LONG).show();
                    askedForPermissionContacts = true;
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

       if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String imagePath = getPathFromGalleryUri(this, selectedImage);
            postImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));


           ApiService apiService = ApiClient.getService();
           File imageFile = new File(imagePath);
           RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
           MultipartBody.Part body = MultipartBody.Part.createFormData("media", imageFile.getName(), requestFile);

           final ProgressDialog dialog = ProgressDialog.show(this, "", "Uploading. Please wait...", true);
           dialog.show();

           Call<Void> call = apiService.uploadImageMySpace(body);
           call.enqueue(new Callback<Void>() {
               @Override
               public void onResponse(Call<Void> call, Response<Void> response) {
                   Toast.makeText(MainActivity.this,"Upload was successfull :)",Toast.LENGTH_SHORT).show();
                   dialog.dismiss();
               }

               @Override
               public void onFailure(Call<Void> call, Throwable t) {
                   Toast.makeText(MainActivity.this,"Failed : " + t.getMessage(),Toast.LENGTH_SHORT).show();
                   dialog.dismiss();
               }
           });
        }
    }

    public static String getPathFromGalleryUri(Context context, Uri uri) {
        //Log.e(TAG,"Uri = " + uri + "    filepath = " + uri.getPath() + "used = " + Home.usedCamera);
        // Split at colon, use second item in the array

        String[] projection = { MediaStore.Images.Media.DATA };

        Cursor cursor = context.getContentResolver().
                query(uri,projection,null,null,null);

        String filePath = "";
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        Log.e(TAG,"ImagePath = " + filePath);

        return filePath;
    }
}
