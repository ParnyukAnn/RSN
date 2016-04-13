package com.aparnyuk.rsn.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aparnyuk.rsn.R;
import com.aparnyuk.rsn.model.Calls;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;

import java.text.SimpleDateFormat;
import java.util.HashSet;

public class CallListAdapter extends FirebaseRecyclerAdapter<Calls, CallListAdapter.CallViewHolder> {

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

    public CallListAdapter(Firebase ref) {
        super(Calls.class, R.layout.list_item_for_calls, CallListAdapter.CallViewHolder.class, ref);
    }

    static class CallViewHolder extends RecyclerView.ViewHolder {
        TextView phoneNum;
        TextView callText;
        TextView dateText;
        RelativeLayout ll;

        public CallViewHolder(View itemView) {
            super(itemView);
            phoneNum = (TextView) itemView.findViewById(R.id.call_phone_num);
            callText = (TextView) itemView.findViewById(R.id.call_text);
            dateText = (TextView) itemView.findViewById(R.id.call_date);
            ll = (RelativeLayout) itemView.findViewById(R.id.call_main_layout);

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
                        Log.d("Call", "" + position);
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
    protected void populateViewHolder(CallListAdapter.CallViewHolder CallViewHolder, Calls call, int i) {
        if (call != null) {
            String str = "";
            if (call.getNumbers().size() > 1) {
                if (call.getNumbers().size() == 2) {
                    str = " and other 1";
                } else {
                    str = " and others " + (call.getNumbers().size() - 1);
                }
            }
/*        String str = new String();
        int size = call.getNumbers().size();
        if (size > 4) {
            str = " and others " + (size - 1);
        } else {
            for (int s = 0; s < size; s++) {
//                str = str +System.getProperty("line.separator")+ call.getNumbers().get(i);
                str = str +"<br>" + call.getNumbers().get(i);

            }
        }*/

            CallViewHolder.phoneNum.setText(call.getNumbers().get(0) + str);
            CallViewHolder.callText.setText(call.getText());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
            CallViewHolder.dateText.setText(dateFormat.format(call.getDate()));
            if (deleteItemSet.contains(CallViewHolder.getLayoutPosition())) {
                CallViewHolder.ll.setBackgroundColor(Color.LTGRAY);
            } else {
                CallViewHolder.ll.setBackgroundColor(Color.WHITE);
            }
        }
    }
}

