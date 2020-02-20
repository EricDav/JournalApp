package com.example.david.journalapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.david.journalapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.example.david.journalapp.Helper.cancelProgress;
import static com.example.david.journalapp.Helper.isConnected;
import static com.example.david.journalapp.Helper.openProgress;

public class CreateEntry extends AppCompatActivity  {

    EditText subjectEditText;
    EditText contentEditText;
    String subject;
    String content;
    String date;
    String id;
    ProgressDialog progress;
    Intent intent;
    SharedPreferences sharedPref;
    MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_entry);
        subjectEditText = findViewById(R.id.subjectId);
        contentEditText = findViewById(R.id.contentId);
        progress = new ProgressDialog(this);

        intent = getIntent();

        if (intent.hasExtra("id")) {
            subjectEditText.setText(intent.getStringExtra("subject"));
            contentEditText.setText(intent.getStringExtra("content"));
            setTitle("Edit Entry");
        }

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean("isDatabaseMightChange", false);
        editor.apply();
    }

    public void saveEntry() {

        if (!isEmptyInput() && isConnected(this)) {
            subject = subjectEditText.getText().toString();
            content = contentEditText.getText().toString();


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setTimestampsInSnapshotsEnabled(true)
                    .build();
            db.setFirestoreSettings(settings);
            Calendar calender = Calendar.getInstance();

            Integer year = calender.get(Calendar.YEAR);
            Integer month = calender.get(Calendar.MONTH);      // 0 to 11
            Integer day = calender.get(Calendar.DAY_OF_MONTH);
            Integer hour = calender.get(Calendar.HOUR_OF_DAY);
            Integer minute = calender.get(Calendar.MINUTE);

            date = year.toString() + "," + month.toString() + "," + day.toString() + "," + hour.toString() + "," + minute.toString();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

            Map<String, Object> entry = new HashMap<>();
            entry.put("subject", subject);
            entry.put("content", content);
            entry.put("userEmail", sharedPref.getString("loginUser", ""));
            entry.put("date", date);

            item.setVisible(false);
            openProgress(progress, "saving");
            if (intent.hasExtra("id")) {
                if (intent.getStringExtra("subject").equals(subjectEditText.getText().toString()) &&
                        intent.getStringExtra("content").equals(contentEditText.getText().toString())) {
                    finish();
                    return;
                }
                db.collection("entries").document(intent.getStringExtra("id"))
                        .set(entry)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        handleOnSuccess();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                handleFailure();
                            }
                        });
            } else {
                db.collection("entries")
                        .add(entry)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                id = documentReference.getId();
                                handleOnSuccess();
                                Log.d("SUCCESS", "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                handleFailure();

                                Log.w("FAILURE", "Error adding document", e);
                            }
                        });
            }
        }
    }

    private void handleFailure() {
        cancelProgress(progress);
        item.setVisible(true);
        Toast.makeText(this, "An error occurred, try again", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.entry, menu);
        item = menu.getItem(0);
        return true;
    }

    /**
     * It starts the main activities with the details of the created entry after saving an entry successfully.
     */
    private void handleOnSuccess() {
        Toast.makeText(this, "Entry saved successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.putExtra("subject", subject);
        intent.putExtra("content", content);
        intent.putExtra("id", id);
        intent.putExtra("date", date);

        setResult(RESULT_OK, intent);
        cancelProgress(progress);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.entry:
                saveEntry();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * It checks if any of the inputs supplied by a user is empty.
     *
     * @return true if any of the input is empty else returns false
     */
    public boolean isEmptyInput() {
        if(subjectEditText.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Subject is required", Toast.LENGTH_SHORT).show();
            return true;
        } else if (contentEditText.getText().toString().trim().equals("")) {
            Toast.makeText(this, "Content is required", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }

}
