package com.example.david.journalapp.Activities;

import android.app.DialogFragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.david.journalapp.Dialogs.DeleteEntryDialog;
import com.example.david.journalapp.Models.Entry;
import com.example.david.journalapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.example.david.journalapp.Helper.cancelProgress;
import static com.example.david.journalapp.Helper.getFullDate;
import static com.example.david.journalapp.Helper.openProgress;

public class EntryDetails extends AppCompatActivity {
    String id;
    String date;
    String subject;
    String content;
    TextView dateTextView;
    TextView subjectTextView;
    TextView contentTextView;
    boolean isUpdated;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_details);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        date = intent.getStringExtra("date");
        subject = intent.getStringExtra("subject");
        content = intent.getStringExtra("content");

        dateTextView = findViewById(R.id.entryDetailDateId);
        subjectTextView = findViewById(R.id.entryDetailSubjectId);
        contentTextView = findViewById(R.id.entryDetailContentId);

        dateTextView.setText(getFullDate(date));
        subjectTextView.setText(subject);
        contentTextView.setText(content);
        progress = new ProgressDialog(this);
        isUpdated = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.entry_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isUpdated) {
                    Intent intent = new Intent();
                    intent.putExtra("isUpdated", true);
                    intent.putExtra("content", content);
                    intent.putExtra("subject", subject);
                    intent.putExtra("id", id);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    finish();
                }
                return true;

            case R.id.edit_entry:
                editEntry();
                return true;
            case R.id.delete_entry:
                DialogFragment dialogFragment = new DeleteEntryDialog();
                Bundle data = new Bundle();

                dialogFragment.setArguments(data);
                dialogFragment.show(getFragmentManager(), "missiles");
                data.putString("id", id);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (isUpdated) {
            Log.d("STP_DET", "I am stoping from details");
            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putBoolean("isUpdated", true);
            edit.putString("subject", subject);
            edit.putString("content", content);
            edit.putString("id", id);

            edit.apply();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 1) {
            subject = data.getStringExtra("subject");
            content = data.getStringExtra("content");
            subjectTextView.setText(subject);
            contentTextView.setText(content);
            isUpdated = true;
        }
    }

    public void editEntry() {
        Intent intent = new Intent(this, CreateEntry.class);
        intent.putExtra("id", id);
        intent.putExtra("date", date);
        intent.putExtra("subject", subject);
        intent.putExtra("content", content);

        startActivityForResult(intent, 1);
    }

    /**
     * Makes a call to firebase to delete this entry.
     */
    public void deleteEntry() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        openProgress(progress, "deleting");
        db.collection("entries").document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        handleSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       handleFailure();
                    }
                });
    }

    /**
     * Displays a  toast and navigate back to all entries after successful deletion of an entry.
     */
    public void handleSuccess() {
        Toast.makeText(this, "Entry deleted successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();

        intent.putExtra("deletedEntryId", id);
        cancelProgress(progress);

        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Displays an error message toast indicating that entry was not deleted
     */
    public void handleFailure() {
        cancelProgress(progress);
        Toast.makeText(this, "An error occurred while deleting entry", Toast.LENGTH_SHORT).show();
    }

    /**
     * It sorts the contacts base on their names.
     *
     * @param entries phone contacts
     *
     * @return weekdays in num in a sorted manner.
     */
    public static List<Entry> sortEntries(List<Entry> entries) {
        boolean isSorted = false;
        int hasBeenSorted;
        while (!isSorted) {
            hasBeenSorted = 0;
            for (int i = 1; i < entries.size(); i++) {
                String[] currentEntryDate = entries.get(i).getDate().split(",");
                String[] previousEntry = entries.get(i-1).getDate().split(",");
                Calendar calendar = Calendar.getInstance();
                Calendar calendar1 = Calendar.getInstance();
                calendar.set(Integer.parseInt(currentEntryDate[0]), Integer.parseInt(currentEntryDate[1]), Integer.parseInt(currentEntryDate[2]),
                        Integer.parseInt(currentEntryDate[3]), Integer.parseInt(currentEntryDate[4]));
                calendar1.set(Integer.parseInt(previousEntry[0]), Integer.parseInt(previousEntry[1]), Integer.parseInt(previousEntry[2]),
                        Integer.parseInt(previousEntry[3]), Integer.parseInt(previousEntry[4]));

                if (calendar.getTimeInMillis() > calendar.getTimeInMillis()) {
                    Collections.swap(entries, i, i-1);
                    hasBeenSorted +=1;
                }
            }
            isSorted = hasBeenSorted == 0 ? true : false;
        }
        return entries;
    }
}
