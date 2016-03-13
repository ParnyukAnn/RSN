package com.aparnyuk.rsn.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aparnyuk.rsn.Contact;
import com.aparnyuk.rsn.R;

import java.util.ArrayList;


public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    private ArrayList<Contact> contacts;
    private int checkBoxCount = 0;
    private ActionMode mActionMode;
    private Activity activity;

    private void selectItem(int position, ViewHolder holder){
        if (contacts.get(position).isSelected()){
            holder.rowView.setSelected(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                holder.rowView.setActivated(true);
        } else {
            holder.rowView.setSelected(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                holder.rowView.setActivated(false);

        }
        updateActionBar();
    }

    private void updateActionBar(){
        checkBoxCount = 0;
        for(int i=0; i<contacts.size(); i++) {
            if( contacts.get(i).isSelected()) checkBoxCount++;
        }

        if (checkBoxCount == 0){
            if (mActionMode != null ){
                mActionMode.finish();
                mActionMode = null;
            }

        } else {
            if (mActionMode == null) mActionMode = ((AppCompatActivity) activity).startSupportActionMode(mActionModeCallback);
        }
        if (mActionMode != null) {
            if (checkBoxCount == 1) {
                mActionMode.setTitle(activity.getString(R.string.add) + " " + String.valueOf(checkBoxCount) + " " + activity.getString(R.string.friend));
            } else {
                mActionMode.setTitle(activity.getString(R.string.add) + " " + String.valueOf(checkBoxCount) + " " + activity.getString(R.string.friends));
            }
        }

    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);

           // searchButton.setVisibility(View.GONE);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

            int id = item.getItemId();
            if (id == R.id.menu_ok) {
                finishWithResult();
//                   //activity.finish();
//
//                   // mode.finish();
                return true;
            } else {
                return false;
            }

        }

        private void finishWithResult()
        {
            ArrayList<Contact> selectedContacts = new ArrayList<>();
            for (Contact c: contacts){
                if (c.isSelected()){
                    selectedContacts.add(c);
                }
            }

            Bundle resultData = new Bundle();
            resultData.putParcelableArrayList("contacts", selectedContacts);
            Intent intent = new Intent();
            intent.putExtras(resultData);
            activity.setResult(activity.RESULT_OK, intent);
            activity.finish();
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {

            boolean isAnySelected = false;
            for(int i=0; i<contacts.size(); i++) {
                if (contacts.get(i).isSelected()){
                    contacts.get(i).setSelected(false);
                    isAnySelected = true;
                }
            }

            if (isAnySelected) {
                //contactIconLazyLoad.Reset();
                notifyDataSetChanged();
            }


            //searchButton.setVisibility(View.VISIBLE);
            mActionMode = null;

        }
    };

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case


        public View rowView;
        public ImageView pictureView;
        public TextView nameView;
        public TextView emailView;


        public ViewHolder(View v) {
            super(v);

            rowView = v;
            nameView = (TextView) v.findViewById(R.id.contact_name);
            emailView = (TextView) v.findViewById(R.id.contact_email);
            pictureView = (ImageView) v.findViewById(R.id.contact_picture);


        }


    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ContactsAdapter(Activity activity, ArrayList<Contact> contacts) {
        this.activity = activity;
        this.contacts = contacts;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item_view, parent, false);
        // set the view's size, margins, paddings and layout parameters


        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.nameView.setText(contacts.get(position).getName());
        holder.emailView.setText(contacts.get(position).getNumber());

        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(Resources.getSystem(), contacts.get(position).getPicture());
        drawable.setCircular(true);

        holder.pictureView.setImageDrawable(drawable);

        holder.rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contacts.get(position).setSelected(!contacts.get(position).isSelected());
                selectItem(position, holder);
            }
        });
        selectItem(position, holder);


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return contacts.size();
    }
}
