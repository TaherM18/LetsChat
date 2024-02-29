package com.example.wechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.wechat.adapter.RecyclerContactsAdapter;
import com.example.wechat.model.ContactModel;

import java.util.ArrayList;

public class ContactsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<ContactModel> contactModels;
    private RecyclerContactsAdapter adapter;
    private int PERMISSION_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        contactModels = new ArrayList<>();

        checkRuntimePermission();
    }

    private void checkRuntimePermission() {
        if (ContextCompat.checkSelfPermission(ContactsActivity.this,
                android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ContactsActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_CODE);
        }
        else {
            getContactList();
        }
    }

    private void getContactList() {

        Uri uri = ContactsContract.Contacts.CONTENT_URI;

        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC";

        Cursor cursor = getContentResolver().query(uri, null, null, null, sort);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                Uri phone_url = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" =?";
                Cursor phone_cursor = getContentResolver().query(
                        phone_url, null, selection, new String[]{id}, null);

                if (phone_cursor.moveToNext()) {
                    String number = phone_cursor.getString(phone_cursor.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));

                    ContactModel model = new ContactModel();
                    model.setName(name);
                    model.setNumber(number);

                    contactModels.add(model);

                    phone_cursor.close();
                }
            }
            cursor.close();
        }

        recyclerView = findViewById(R.id.contactsRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecyclerContactsAdapter(this, contactModels);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getContactList();
        }
        else {
            Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show();
            checkRuntimePermission();
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case 121 :
//                Intent iDial = new Intent(Intent.ACTION_DIAL);
//                iDial.setData(Uri.parse("tel: " + Global.telephone));
//                startActivity(iDial);
                break;
            case 122 :
//                Intent iMsg = new Intent(Intent.ACTION_SENDTO);
//                iMsg.setData(Uri.parse("smsto: "+Uri.encode(Global.telephone)));
//                iMsg.putExtra("sms_body", "Hello! How are you?");
//                startActivity(iMsg);
                break;
        }
        return super.onContextItemSelected(item);
    }
}