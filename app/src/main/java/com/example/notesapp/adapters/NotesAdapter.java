package com.example.notesapp.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;
import com.example.notesapp.entities.Note;
import com.example.notesapp.listeners.NotesListeners;
import com.makeramen.roundedimageview.RoundedImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<Note> notes;
    private final NotesListeners notesListeners;
    private final List<Note> notesSource;

    private Timer timer;

    public NotesAdapter(List<Note> notes,NotesListeners notesListeners) {

        this.notes = notes;
        this.notesListeners = notesListeners;
        notesSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(v -> notesListeners.onNoteClicked(notes.get(position),position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

     static class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView txtTitle,txtSubtitle,txtDateTime;
        LinearLayout layoutNote;

        RoundedImageView imgNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtSubtitle = itemView.findViewById(R.id.txtSubtitle);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
            layoutNote = itemView.findViewById(R.id.layout_note);
            imgNote = itemView.findViewById(R.id.image_Note);
        }
        void setNote(Note note){
            txtTitle.setText(note.getTitle());
            if(note.getSubtitle().trim().isEmpty()){
                txtSubtitle.setVisibility(View.GONE);
            }else {
                txtSubtitle.setText(note.getSubtitle());
            }
            txtDateTime.setText(note.getDateTime());


            //Setting the background color of the note
            Drawable bg = layoutNote.getBackground();
            if(bg instanceof GradientDrawable){

                GradientDrawable gradientDrawable = (GradientDrawable) bg;
                if (note.getColor()!=null){
                    gradientDrawable.setColor(Color.parseColor(note.getColor()));
                }else {
                    gradientDrawable.setColor(Color.parseColor("#333333"));
                }

            }else if(bg instanceof ColorDrawable){
                if (note.getColor()!=null){
                    layoutNote.setBackgroundColor(Color.parseColor(note.getColor()));
                }else {
                    layoutNote.setBackgroundColor(Color.parseColor("#333333"));
                }
            }

            //Setting the image on the note in the note container
            if(note.getImgPath() != null){
                imgNote.setImageBitmap(BitmapFactory.decodeFile(note.getImgPath()));
                imgNote.setVisibility(View.VISIBLE);
            }else{
                imgNote.setVisibility(View.GONE);
            }
        }
    }



    //Adapter function for search app
    public void searchNotes(final String searchKeyword){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyword.trim().isEmpty()){
                    notes = notesSource;
                }else{
                    ArrayList<Note> temp = new ArrayList<>();
                    for(Note note : notesSource){
                        if(note.getTitle().toLowerCase().trim().contains(searchKeyword.toLowerCase()) || note.getSubtitle().toLowerCase().trim().contains(searchKeyword.toLowerCase()) || note.getNoteText().toLowerCase().trim().contains(searchKeyword.toLowerCase())){
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
            }
        },500);

    }

    //Cancel Timer
    public void cancelTimer(){
        if(timer != null){
            timer.cancel();
        }
    }
}
