package com.aparnyuk.rsn.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.model.Sms;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;

import java.text.SimpleDateFormat;
import java.util.HashSet;

public class SmsListAdapter extends FirebaseRecyclerAdapter<Sms, SmsListAdapter.SmsViewHolder> {

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
    public SmsListAdapter(Firebase ref) {
        super(Sms.class, R.layout.list_item_for_sms, SmsListAdapter.SmsViewHolder.class, ref);
    }

    static class SmsViewHolder extends RecyclerView.ViewHolder {

        TextView smsText;
        TextView dateText;
        LinearLayout ll;

        public SmsViewHolder(View itemView) {
            super(itemView);
            smsText = (TextView) itemView.findViewById(R.id.sms_text);
            dateText = (TextView) itemView.findViewById(R.id.sms_date);
            ll = (LinearLayout) itemView.findViewById(R.id.sms_main_layout);

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
                        Log.d("Sms", "" + position);
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

/*    static class SmsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener  {

        CardView cv;
        CheckBox deleteSms;
        TextView SmsText;
        TextView dateText;
        LinearLayout ll;

        public SmsViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.Sms_cv);
            SmsText = (TextView) itemView.findViewById(R.id.Sms_text);
            dateText = (TextView) itemView.findViewById(R.id.Sms_date);
            deleteSms = (CheckBox) itemView.findViewById(R.id.checkBox);
            ll = (LinearLayout) itemView.findViewById(R.id.test);

            deleteSms.setOnClickListener(this);
            ll.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                switch (v.getId()) {
                    case R.id.test:
                        itemClick(position);
                        break;
                    case R.id.checkBox:
                        checkClick(position);
                }
            }
        }

        private void itemClick(int position) {
            Log.d("Sms", "Click " + position);
        }

        private void checkClick(int position) {
            Log.d("Sms", "Click " + position);
        }
    }*/

    @Override
    protected void populateViewHolder(SmsListAdapter.SmsViewHolder smsViewHolder, Sms sms, int i) {
        smsViewHolder.smsText.setText(sms.getText());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
        smsViewHolder.dateText.setText(dateFormat.format(sms.getDate()));
        if (deleteItemSet.contains(smsViewHolder.getLayoutPosition())) {
            smsViewHolder.ll.setBackgroundColor(Color.LTGRAY);
        } else {
            smsViewHolder.ll.setBackgroundColor(Color.WHITE);
        }
    }
}

