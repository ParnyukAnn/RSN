package com.aparnyuk.rsn.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.model.Note;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;

import java.text.SimpleDateFormat;
import java.util.HashSet;

public class NoteListAdapter extends FirebaseRecyclerAdapter<Note, NoteListAdapter.NoteViewHolder> {

    private static HashSet<Integer> deleteItemSet = new HashSet<>();

    public static boolean isDeleteMode() {
        return deleteMode;
    }

    private static boolean deleteMode = false;
    private static boolean changeMode = false;

    private static OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position, boolean deleteMode, boolean changeMode);

        void onItemLongClick(View view, int position, boolean mode, boolean changeMode);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public static HashSet<Integer> getDeleteItemSet() {
        return deleteItemSet;
    }

    public NoteListAdapter(Firebase ref) {
        super(Note.class, R.layout.list_item_for_note, NoteListAdapter.NoteViewHolder.class, ref);
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView noteText;
        TextView dateText;
        LinearLayout ll;

        public NoteViewHolder(View itemView) {
            super(itemView);
            noteText = (TextView) itemView.findViewById(R.id.note_text);
            dateText = (TextView) itemView.findViewById(R.id.note_date);
            ll = (LinearLayout) itemView.findViewById(R.id.note_main_layout);

            ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getLayoutPosition();
                    if (listener != null) {
                        if (deleteMode) {
                            if (deleteItemSet.contains(position)) {
                                deleteItemSet.remove(position);
                                v.setBackgroundColor(Color.WHITE);
                                if (deleteItemSet.isEmpty()) {
                                    deleteMode = false;
                                    changeMode = true;
                                }
                            } else {
                                deleteItemSet.add(position);
                                v.setBackgroundColor(Color.LTGRAY);
                            }
                        }
                        listener.onItemClick(v, position, deleteMode, changeMode);
                        changeMode = false;
                    }
                }
            });

            ll.setLongClickable(true);
            ll.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getLayoutPosition();
                    if (listener != null) {
                        if (deleteMode) {
                            if (deleteItemSet.contains(position)) {
                                deleteItemSet.remove(position);
                                v.setBackgroundColor(Color.WHITE);
                                if (deleteItemSet.isEmpty()) {
                                    deleteMode = false;
                                    changeMode = true;
                                }
                            } else {
                                deleteItemSet.add(position);
                                v.setBackgroundColor(Color.LTGRAY);
                            }
                        } else {
                            deleteMode = true;
                            changeMode = true;
                            v.setBackgroundColor(Color.LTGRAY);
                            deleteItemSet.add(position);
                        }
                        Log.d("Note", "" + position);
                        listener.onItemLongClick(v, position, deleteMode, changeMode);
                        changeMode = false;
                        return true;
                    }
                    return false;
                }
            });

        }
    }

    public void clearDeleteMode() {
        deleteMode = false;
        changeMode = false;
        deleteItemSet.clear();
    }

    @Override
    protected void populateViewHolder(NoteListAdapter.NoteViewHolder noteViewHolder, Note note, int i) {
        if (note != null) {
            noteViewHolder.noteText.setText(note.getText());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
            noteViewHolder.dateText.setText(dateFormat.format(note.getDate()));
            if (deleteItemSet.contains(noteViewHolder.getLayoutPosition())) {
                noteViewHolder.ll.setBackgroundColor(Color.LTGRAY);
            } else {
                noteViewHolder.ll.setBackgroundColor(Color.WHITE);
            }
        }
    }
}

