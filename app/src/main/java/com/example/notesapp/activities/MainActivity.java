package com.example.notesapp.activities;

import static androidx.recyclerview.widget.StaggeredGridLayoutManager.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.notesapp.R;
import com.example.notesapp.adapters.NotesAdapter;
import com.example.notesapp.database.NotesDB;
import com.example.notesapp.entities.Note;
import com.example.notesapp.listeners.NotesListeners;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity implements NotesListeners{


    private RecyclerView notesRecyclerView;
    private List<Note> noteLists;
    private NotesAdapter notesAdapter;

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;

    private int noteClickedPosition = -1;
    private ActivityResultLauncher<Intent> addNoteLauncher;
    private ActivityResultLauncher<Intent> viewUpdateNoteLauncher;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView img_AddNoteMain = findViewById(R.id.add_btn_main);


        //Activity launcher register for adding the notes
        addNoteLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
            if(result.getResultCode() == RESULT_OK && result.getData() != null){
                getNotes(REQUEST_CODE_ADD_NOTE,false);
            }else{
                Log.d("Add note","Add note");
            }
        });


        //Activity launcher register for viewing or updating the notes
        viewUpdateNoteLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
            Intent data = result.getData();
            if(result.getResultCode() == RESULT_OK && result.getData() != null){
                getNotes(REQUEST_CODE_UPDATE_NOTE,data.getBooleanExtra("isNoteDeleted",false));
            }else{
                Log.d("View or update","Updated note");
            }

        });

        img_AddNoteMain.setOnClickListener(v -> {
            Intent it = new Intent(getApplicationContext(), AddNote.class);
            it.putExtra("requestCode",REQUEST_CODE_ADD_NOTE);
            addNoteLauncher.launch(it);
        });

        notesRecyclerView = findViewById(R.id.layout_recycler);

        notesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,VERTICAL));

        noteLists = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteLists,this);
        notesRecyclerView.setAdapter(notesAdapter);

        getNotes(REQUEST_CODE_SHOW_NOTES,false);


        //Functionality for search note
        EditText inpSearch = findViewById(R.id.inp_search);
        inpSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteLists.size() != 0){
                    notesAdapter.searchNotes(s.toString());
                }
            }
        });

        //After clicking the about us button in quick actions then redirecting to about us page
        findViewById(R.id.img_aboutUs).setOnClickListener(v->{
            Intent it = new Intent(getApplicationContext(),About_Us.class);
            startActivity(it);
        });

    }




    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent it = new Intent(getApplicationContext(), AddNote.class);
        it.putExtra("isViewOrUpdate",true);
        it.putExtra("note",note);
        it.putExtra("requestCode",REQUEST_CODE_UPDATE_NOTE);
        viewUpdateNoteLauncher.launch(it);
    }

//    Function to display the notes in recycler view

    private void getNotes(final int requestCode,final boolean isNoteDeleted){

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<List<Note>> future =  executor.submit(()-> NotesDB.getDatabase(getApplicationContext()).notes_dao().getAllNotes());

        executor.execute(()->{
            try {
                List<Note> notes = future.get();

                //Displaying the notes after the following operations
                if(requestCode == REQUEST_CODE_SHOW_NOTES){
                    noteLists.addAll(notes);
                    notesAdapter.notifyDataSetChanged();
                }else if(requestCode == REQUEST_CODE_ADD_NOTE){
                    noteLists.add(0,notes.get(0));
                    notesAdapter.notifyItemInserted(0);
                }else if(requestCode == REQUEST_CODE_UPDATE_NOTE){
                    noteLists.remove(noteClickedPosition);

                    //After deleting the note
                    if(isNoteDeleted){
                        notesAdapter.notifyItemRemoved(noteClickedPosition);
                    }else{
                        noteLists.add(noteClickedPosition,notes.get(noteClickedPosition));
                        notesAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }
                notesRecyclerView.smoothScrollToPosition(0);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                executor.shutdown();
            }
        });
    }



}