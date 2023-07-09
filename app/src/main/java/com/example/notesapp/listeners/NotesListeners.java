package com.example.notesapp.listeners;

import com.example.notesapp.entities.Note;

public interface NotesListeners {
    void onNoteClicked(Note note, int position);
}
