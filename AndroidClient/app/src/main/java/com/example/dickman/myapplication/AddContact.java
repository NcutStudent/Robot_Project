package com.example.dickman.myapplication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.dickman.myapplication.Util.IMAGE_FOLDER_NAME;
import static com.example.dickman.myapplication.Util.IMAGE_PATH;
import static com.example.dickman.myapplication.Util.NO_OVERRIDE;
import static com.example.dickman.myapplication.Util.OVERRIDE;
import static com.example.dickman.myapplication.Util.SHARED_PREFERENCES;
import static com.example.dickman.myapplication.Util.USER_ID;
import static com.example.dickman.myapplication.Util.YES_OVERRIDE;

public class AddContact extends AppCompatActivity {

    final static int REQUEST_PICK_IMAGE = 2229;
    final static int REQUEST_IMAGE_CAPTURE = 1924;
    TextView tvInputId;
    ImageView imagePreview;
    Button selectImage;
    Button submitButton;
    Bitmap bmp;
    AlertDialog chooseMethod;

    private File mypath;
    final static String TEMP_IMAGE_NAME = "temp.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        imagePreview = findViewById(R.id.image_preview);
        tvInputId = findViewById(R.id.input_id);
        selectImage = findViewById(R.id.btn_select_image);
        submitButton = findViewById(R.id.ok);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ListView listView = new ListView(this);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{ getString(R.string.select_from_gallery), getString(R.string.take_photo)}));
        builder.setView(listView);
        chooseMethod = builder.create();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE);
                } else if(position == 1) {
                    if (ActivityCompat.checkSelfPermission(AddContact.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ) {
                        requestPermissions(new String[]{new String(Manifest.permission.CAMERA)}, 0);
                    } else {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File newFile = new File(AddContact.this.getFilesDir(), TEMP_IMAGE_NAME);

                        Uri photoUri = FileProvider.getUriForFile(
                                AddContact.this,
                                getPackageName() + ".fileprovider",
                                newFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                        } else {
                            AlertDialog.Builder b = new AlertDialog.Builder(AddContact.this);
                            b.setTitle("Error");
                            b.setMessage(R.string.can_not_find_camera);
                            b.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    Intent intent = new Intent();
                                    intent.setType("image/*");
                                    intent.setAction(Intent.ACTION_GET_CONTENT);
                                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE);
                                }
                            });
                            b.setPositiveButton("OK", null);
                            b.show();
                        }
                    }
                }
                chooseMethod.cancel();
            }
        });
    }

    public void selectImage(View view) {
        chooseMethod.show();
    }

    public void submit(View view) {
        if(tvInputId.getText().toString().equals("")){
            Toast.makeText(this, getString(R.string.please_input_id), Toast.LENGTH_SHORT).show();
            return;
        }
        if(bmp == null) {
            Toast.makeText(this, getString(R.string.please_select_image), Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String isIdExist = sharedPreferences.getString(tvInputId.getText().toString(), null);
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                add(NO_OVERRIDE);
            }
        };
        if(isIdExist != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warning);
            builder.setMessage(R.string.id_is_exist_warning);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    add(YES_OVERRIDE);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.show();
        } else {
            onClickListener.onClick(null, 0);
        }
    }

    private void add(String override) {
        selectImage.setEnabled(false);
        submitButton.setEnabled(false);
        tvInputId.setEnabled(false);
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir(IMAGE_FOLDER_NAME, Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        String imgName = tvInputId.getText().toString() + ".png";
        mypath = new File(directory, imgName);
        try {
            FileOutputStream fos = new FileOutputStream(mypath);
            boolean i = bmp.compress(Bitmap.CompressFormat.PNG, 70, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(USER_ID, tvInputId.getText().toString());
        bundle.putString(IMAGE_PATH, mypath.getAbsolutePath());
        bundle.putString(OVERRIDE,override);
        intent.putExtras(bundle);
        setResult(RESULT_OK,intent);
        AddContact.this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                imagePreview.setImageBitmap(bitmap);
                bmp = bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            File mypath = new File(AddContact.this.getFilesDir(), TEMP_IMAGE_NAME);
            Bitmap bitmap = BitmapFactory.decodeFile(mypath.getAbsolutePath());
            imagePreview.setImageBitmap(bitmap);
            bmp = bitmap;
            mypath.delete();
        }
    }
}
