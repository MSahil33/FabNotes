package com.example.notesapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.notesapp.dao.NoteDao;
import com.example.notesapp.entities.Note;

@Database(entities = Note.class,version = 1,exportSchema = false)
public abstract class NotesDB extends RoomDatabase {
    private static NotesDB notes_db;

    public static synchronized NotesDB getDatabase(Context context) {

        if(notes_db==null){
            notes_db = Room.databaseBuilder(context,NotesDB.class,"notes_db").build();
        }
        return notes_db;
    }

    public abstract NoteDao notes_dao();
}
