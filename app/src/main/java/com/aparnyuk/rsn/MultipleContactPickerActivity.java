package com.aparnyuk.rsn;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;

public class MultipleContactPickerActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public ArrayList<Contact> contacts = new ArrayList<Contact>();

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 101;

    private RecyclerView contactsView;
    //    private RecyclerView.Adapter contactsAdapter;
    private ContactsAdapter contactsAdapter;
    private RecyclerView.LayoutManager contactsLayoutManager;

    private static final String[] PROJECTION =
            {
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts.PHOTO_ID,
//                ContactsContract.CommonDataKinds.Email.DATA,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
            };

    //    private static final String FILTER = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
    private static final String FILTER = ContactsContract.CommonDataKinds.Phone.NUMBER + " NOT LIKE ''";

    private static final String SORT_ORDER = "CASE WHEN "
            + ContactsContract.Contacts.DISPLAY_NAME
            + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
            + ContactsContract.Contacts.DISPLAY_NAME
            + ", "
//            + ContactsContract.CommonDataKinds.Email.DATA
            + ContactsContract.CommonDataKinds.Phone.NUMBER
            + " COLLATE NOCASE";

    private Bitmap getPhotoById(int id) {

        Bitmap bitmapPhoto = null;
        byte[] photo = null;

        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, id);
        Cursor cursorPhoto = getContentResolver().query(photoUri, new String[]{ContactsContract.CommonDataKinds.Photo.PHOTO}, null, null, null);

        try {
            if (cursorPhoto.moveToFirst())
                photo = cursorPhoto.getBlob(0);

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        } finally {

            cursorPhoto.close();
        }

        if (photo != null) {
            bitmapPhoto = BitmapFactory.decodeByteArray(photo, 0, photo.length);
//                    Log.d("!!!!!!!!!!!!!!!PICTURE!!!!!!!!!!", "!!!!!!!!!!!!!!!!");
        }

        return bitmapPhoto;
    }

    private ArrayList<Contact> contactsFromCursor(Cursor cursor) {
        ArrayList<Contact> c = new ArrayList<Contact>();


        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            do {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
//                String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));

                if (email != null) {
                    c.add(new Contact(name, email, getPhotoById(id)));
                    Log.d("Name: ", name);
                    Log.d("email: ", email);

                }

            } while (cursor.moveToNext());
        }

        return c;
    }

    private void checkPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {

            getLoaderManager().initLoader(0, null, this);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_multiple_contact_picker);

        checkPermissions();

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.button_ab_close);
        getSupportActionBar().setTitle(getString(R.string.add_friends));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        contactsView = (RecyclerView) findViewById(R.id.contacts_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        contactsView.setHasFixedSize(true);

        // use a linear layout manager
        contactsLayoutManager = new LinearLayoutManager(this);
        contactsView.setLayoutManager(contactsLayoutManager);

        // specify an adapter (see also next example)
        contactsAdapter = new ContactsAdapter(this, contacts);
        contactsView.setAdapter(contactsAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    // Initializes the loader

                    getLoaderManager().initLoader(0, null, this);

                } else {

                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
               /*
         * Makes search string into pattern and
         * stores it in the selection array
         */
        //  mSelectionArgs[0] = "%" + mSearchString + "%";
        // Starts the query
        return new CursorLoader(
                this,
//                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION,
                FILTER,
                null,
                SORT_ORDER
        );
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        contacts.addAll(contactsFromCursor(cursor));
        contactsAdapter.notifyDataSetChanged();

    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
    }
}
