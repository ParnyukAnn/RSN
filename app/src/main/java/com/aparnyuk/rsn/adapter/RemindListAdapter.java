package com.aparnyuk.rsn.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.model.Remind;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;

import java.text.SimpleDateFormat;
import java.util.HashSet;

public class RemindListAdapter extends FirebaseRecyclerAdapter<Remind, RemindListAdapter.RemindViewHolder> {

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

    public RemindListAdapter(Firebase ref) {
        super(Remind.class, R.layout.list_item_for_remind, RemindListAdapter.RemindViewHolder.class, ref);
    }

    static class RemindViewHolder extends RecyclerView.ViewHolder {

        TextView remindText;
        TextView dateText;
        ImageView remindCheck;
        LinearLayout ll;

        public RemindViewHolder(View itemView) {
            super(itemView);
            remindText = (TextView) itemView.findViewById(R.id.remind_text);
            dateText = (TextView) itemView.findViewById(R.id.remind_date);
            remindCheck = (ImageView) itemView.findViewById(R.id.remind_check);
            ll = (LinearLayout) itemView.findViewById(R.id.remind_main_layout);

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
                        Log.d("Remind", "" + position);
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
    protected void populateViewHolder(RemindListAdapter.RemindViewHolder RemindViewHolder, Remind remind, int i) {
        if (remind != null) {
            RemindViewHolder.remindText.setText(remind.getText());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
            RemindViewHolder.dateText.setText(dateFormat.format(remind.getDate()));
            if (deleteItemSet.contains(RemindViewHolder.getLayoutPosition())) {
                RemindViewHolder.ll.setBackgroundColor(Color.LTGRAY);
            } else {
                RemindViewHolder.ll.setBackgroundColor(Color.WHITE);
                if (!remind.isOpen()) {
                    RemindViewHolder.remindCheck.setVisibility(View.VISIBLE);
                } else {
                    RemindViewHolder.remindCheck.setVisibility(View.GONE);
                }
            }
        }
    }
}

