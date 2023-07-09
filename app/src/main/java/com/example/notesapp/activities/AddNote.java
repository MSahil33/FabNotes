package com.example.notesapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notesapp.R;
import com.example.notesapp.database.NotesDB;
import com.example.notesapp.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AddNote extends AppCompatActivity {


    private EditText inpTitle,inpSubtitle,inpNoteText;
    private TextView dateTime,txtUrl;
    private ImageView imgNote;
    private LinearLayout layoutUrl;
    private String selectedNoteColor;
    private String selectedImagePath;
    private AlertDialog addUrlDialog;
    private AlertDialog deleteNoteDialog;
    private Note alreadyAvailableNote;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;

    ActivityResultLauncher<Intent> addImageLauncher;

    View viewSubtitleIndicator ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        ImageView img_back_btn = findViewById(R.id.img_back_btn);

        img_back_btn.setOnClickListener(v -> onBackPressed());


        //Connecting with id's for all the inputs and texts

        inpTitle = findViewById(R.id.edt_title);
        inpSubtitle = findViewById(R.id.edt_note_subtitle);
        inpNoteText = findViewById(R.id.edt_noteText);
        dateTime = findViewById(R.id.txt_dateTime);
        viewSubtitleIndicator = findViewById(R.id.note_subtitle_indicator);
        imgNote = findViewById(R.id.img_Note);
        txtUrl = findViewById(R.id.txt_url);
        layoutUrl = findViewById(R.id.layout_Url);

        dateTime.setText(new SimpleDateFormat("EEEE , dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));


        //Handling the activity results for select image
        addImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
                        Uri selectedImageUri = result.getData().getData();
                        if(selectedImageUri != null){
                            try {
                                InputStream inpStream = getContentResolver().openInputStream(selectedImageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(inpStream);
                                imgNote.setImageBitmap(bitmap);
                                imgNote.setVisibility(View.VISIBLE);
                                //Displaying the remove image button
                                findViewById(R.id.img_deleteNoteImage).setVisibility(View.VISIBLE);
                                selectedImagePath = getPathFromUri(selectedImageUri);
                            }catch (Exception ex){
                                Toast.makeText(this,ex.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
        });


//        After Clicking the save btn
        ImageView saveBtn = findViewById(R.id.img_done_btn);

        saveBtn.setOnClickListener(v -> saveNote());


        //Initially setting the selected note color to default
        selectedNoteColor = "#333333";

        //Initially setting the image path empty
        selectedImagePath = "";

        if(getIntent().getBooleanExtra("isViewOrUpdate",false)){
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }


        //After clicking the remove url button
        findViewById(R.id.img_deleteUrl).setOnClickListener(v -> {
            txtUrl.setText(null);
            layoutUrl.setVisibility(View.GONE);
        });

        //After clicking the remove image button
        findViewById(R.id.img_deleteNoteImage).setOnClickListener(v->{
            imgNote.setImageBitmap(null);
            imgNote.setVisibility(View.GONE);
            findViewById(R.id.img_deleteNoteImage).setVisibility(View.GONE);
            selectedImagePath = "";
        });

        //Initialization the miscellaneous field
        initMiscellaneous();


        //Setting the Subtitle indicator color
        setViewSubtitleIndicatorColor();

    }


    //Function to handle the view or update note
    private void setViewOrUpdateNote(){
        inpTitle.setText(alreadyAvailableNote.getTitle());
        inpSubtitle.setText(alreadyAvailableNote.getSubtitle());
        inpNoteText.setText(alreadyAvailableNote.getNoteText());
        dateTime.setText(alreadyAvailableNote.getDateTime());

        if(alreadyAvailableNote.getImgPath() != null && !alreadyAvailableNote.getImgPath().trim().isEmpty()){
            imgNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImgPath()));
            imgNote.setVisibility(View.VISIBLE);

            //Displaying the remove image button
            findViewById(R.id.img_deleteNoteImage).setVisibility(View.VISIBLE);

            selectedImagePath = alreadyAvailableNote.getImgPath();
        }

        if(alreadyAvailableNote.getUrl() != null && !alreadyAvailableNote.getUrl().trim().isEmpty()){
            txtUrl.setText(alreadyAvailableNote.getUrl());
            layoutUrl.setVisibility(View.VISIBLE);
        }

        initMiscellaneous();
    }

    private void saveNote() {

        String note_title = inpTitle.getText().toString();
        String note_subtitle = inpSubtitle.getText().toString();
        String note_Text = inpNoteText.getText().toString();

//        Validating the inputs
        if(note_title.trim().isEmpty()){
            Toast.makeText(this,"Title can't be Empty",Toast.LENGTH_SHORT).show();
            return;
        } else if (note_Text.trim().isEmpty()) {
            Toast.makeText(this,"Note can't be empty",Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();

        note.setTitle(note_title);
        note.setSubtitle(note_subtitle);
        note.setNoteText(note_Text);
        note.setDateTime(dateTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImgPath(selectedImagePath);

        //Checking whether the url is added or not
        if(layoutUrl.getVisibility() == View.VISIBLE){
            note.setUrl(txtUrl.getText().toString());
        }


        //Setting the id of the new note with the old id
        if(alreadyAvailableNote != null){
            note.setId(alreadyAvailableNote.getId());
        }


// Latest code to handle saving of the note in the database in background in different thread without effecting the current ui thread

        // Execute the saving operation in a background thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> future = executor.submit(() -> {
            // Save the note using the NotesDB's DAO (Data Access Object)
            NotesDB.getDatabase(getApplicationContext()).notes_dao().insertNote(note);
            return null;
        });


        try {
            // Get the result (null in this case) and handle any exceptions
            future.get();
            // Note saved successfully, perform any required actions (e.g., update UI)
            runOnUiThread(() -> {
                Intent it = new Intent();
                setResult(RESULT_OK, it);
                finish();
            });
        } catch (Exception e) {
            // Handle the exception (e.g., show an error message)
            e.printStackTrace();
        }finally {

            // Shutdown the executor
            executor.shutdown();
        }
    }

//    Function to initialize the miscellaneous layout
    private void initMiscellaneous(){
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layout_miscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehaviorLayout = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.txtMiscellaneous).setOnClickListener(v -> {
            if(bottomSheetBehaviorLayout.getState() != BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehaviorLayout.setState(BottomSheetBehavior.STATE_EXPANDED);
            }else{
                bottomSheetBehaviorLayout.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });


        //Function after selecting the color for the note

        final ImageView imgColor1 = findViewById(R.id.imgColor1);
        final ImageView imgColor2 = findViewById(R.id.imgColor2);
        final ImageView imgColor3 = findViewById(R.id.imgColor3);
        final ImageView imgColor4 = findViewById(R.id.imgColor4);
        final ImageView imgColor5 = findViewById(R.id.imgColor5);

        layoutMiscellaneous.findViewById(R.id.view_color1).setOnClickListener(v -> {
            selectedNoteColor = "#333333";
            imgColor1.setImageResource(R.drawable.ic_done);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(0);
            setViewSubtitleIndicatorColor();
        });
        layoutMiscellaneous.findViewById(R.id.view_color2).setOnClickListener(v -> {
            selectedNoteColor = "#fdbe3b";
            imgColor1.setImageResource(0);
            imgColor2.setImageResource(R.drawable.ic_done);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(0);
            setViewSubtitleIndicatorColor();
        });
        layoutMiscellaneous.findViewById(R.id.view_color3).setOnClickListener(v -> {
            selectedNoteColor = "#ff4842";
            imgColor1.setImageResource(0);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(R.drawable.ic_done);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(0);
            setViewSubtitleIndicatorColor();
        });
        layoutMiscellaneous.findViewById(R.id.view_color4).setOnClickListener(v -> {
            selectedNoteColor = "#3a52fc";
            imgColor1.setImageResource(0);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(R.drawable.ic_done);
            imgColor5.setImageResource(0);
            setViewSubtitleIndicatorColor();
        });
        layoutMiscellaneous.findViewById(R.id.view_color5).setOnClickListener(v -> {
            selectedNoteColor = "#01BABA";
            imgColor1.setImageResource(0);
            imgColor2.setImageResource(0);
            imgColor3.setImageResource(0);
            imgColor4.setImageResource(0);
            imgColor5.setImageResource(R.drawable.ic_done);
            setViewSubtitleIndicatorColor();
        });


        //Setting the color of the above if already available during view or update
        if(alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()){
            switch (alreadyAvailableNote.getColor()){
                case "#fdbe3b" :
                    layoutMiscellaneous.findViewById(R.id.view_color2).performClick();
                    break;
                case "#ff4842" :
                    layoutMiscellaneous.findViewById(R.id.view_color3).performClick();
                    break;
                case "#3a52fc" :
                    layoutMiscellaneous.findViewById(R.id.view_color4).performClick();
                    break;
                case "#01BABA" :
                    layoutMiscellaneous.findViewById(R.id.view_color5).performClick();
                    break;
            }
        }

        //After clicking the add image layout
        layoutMiscellaneous.findViewById(R.id.layout_AddImage).setOnClickListener(v -> {
           bottomSheetBehaviorLayout.setState(BottomSheetBehavior.STATE_COLLAPSED);

           //Getting the permission from the user to access the media to select the image
           if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
               ActivityCompat.requestPermissions(AddNote.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE_STORAGE_PERMISSION);
           }
           //Else if the permission is already given directly selecting the Image
            else{
                selectImage();
           }
        });

        //After clicking the add url/link button

        layoutMiscellaneous.findViewById(R.id.layout_AddUrl).setOnClickListener(view ->{
            bottomSheetBehaviorLayout.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddUrlDialog();
        });


        //Functionality for deleting the note

        //If there is already note available then displaying the delete note functionality
        if(alreadyAvailableNote != null){
            layoutMiscellaneous.findViewById(R.id.layout_deleteNote).setVisibility(View.VISIBLE);
            //After clicking the delete button
            layoutMiscellaneous.findViewById(R.id.layout_deleteNote).setOnClickListener(v ->{
                bottomSheetBehaviorLayout.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDeleteNoteDialog();
            });
        }


    }


    //Function to set the color of the subtitle Indicator
    private void setViewSubtitleIndicatorColor(){

        Drawable bg = viewSubtitleIndicator.getBackground();
        if(bg instanceof GradientDrawable){

            GradientDrawable gradientDrawable = (GradientDrawable) bg;
            gradientDrawable.setColor(Color.parseColor(selectedNoteColor));

        }else if(bg instanceof ColorDrawable){
             viewSubtitleIndicator.setBackgroundColor(Color.parseColor(selectedNoteColor));
        }
    }


    //Function to select the image
    private void selectImage() {

        Intent it = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        //Launching the activity for the activityResult Handler
        addImageLauncher.launch(it);
    }


    //Handling the permission if the user has given the permission or not
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length>0){
//        If permission granted or given then selectImage

            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
            //Otherwise displaying the toast message as permission denied
            else {
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }


    //Function to get The path of the image from the uri
    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri,null,null,null,null);

        if(cursor == null){
            filePath = contentUri.getPath();

        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;


    }


    //Function to show the add url dialog box
    private void showAddUrlDialog(){
        if(addUrlDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(AddNote.this);
            View v  = LayoutInflater.from(this).inflate(R.layout.layout_add_url, findViewById(R.id.layout_AddUrl_Container));
            builder.setView(v);

            addUrlDialog = builder.create();
            if(addUrlDialog != null){
                addUrlDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inpAddUrl = v.findViewById(R.id.edt_url);
            inpAddUrl.requestFocus();


            //After clicking the add button in the dialog box


            v.findViewById(R.id.txt_Add).setOnClickListener(view -> {

                if(inpAddUrl.getText().toString().trim().isEmpty()){
                    Toast.makeText(this,"Enter a url",Toast.LENGTH_SHORT).show();
                }else if(!Patterns.WEB_URL.matcher(inpAddUrl.getText().toString()).matches()){
                    Toast.makeText(this, "Enter a valid url", Toast.LENGTH_SHORT).show();
                }else{
                    txtUrl.setText(inpAddUrl.getText().toString());
                    layoutUrl.setVisibility(View.VISIBLE);
                    inpAddUrl.setText(null);
                    addUrlDialog.dismiss();
                }
            });


            //After clicking the cancel button in the dialog box
            v.findViewById(R.id.txt_Cancel).setOnClickListener(view -> addUrlDialog.dismiss());


        }

        addUrlDialog.show();

    }

    //Function to show the delete note dialog box
    private void showDeleteNoteDialog() {
        if (deleteNoteDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AddNote.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note, findViewById(R.id.layout_deleteNote_Container));
            builder.setView(view);
            deleteNoteDialog = builder.create();
            if(deleteNoteDialog.getWindow() != null){
                deleteNoteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            //Running the delete note functionality in thr background and redirecting to the home page
            view.findViewById(R.id.txt_delete).setOnClickListener(v ->{
                ExecutorService service = Executors.newSingleThreadExecutor();
                service.execute(() -> {

                    //Deleting the selected note or opened note
                    NotesDB.getDatabase(getApplicationContext()).notes_dao().deleteNote(alreadyAvailableNote);

                    //deleting the note
                    try {
                        // Note saved successfully, perform any required actions (e.g., update UI)
                        runOnUiThread(() -> {
                            Intent it = new Intent();
                            it.putExtra("isNoteDeleted",true);
                            setResult(RESULT_OK, it);
                            finish();
                        });
                    } catch (Exception e) {
                        // Handle the exception (e.g., show an error message)
                        e.printStackTrace();
                    }finally {
                        // Shutdown the executor service
                        service.shutdown();
                    }
                });
            });


            //After clicking the cancel button in the dialog box
            view.findViewById(R.id.txt_Cancel).setOnClickListener(v -> deleteNoteDialog.dismiss());
        }
        deleteNoteDialog.show();

    }
}