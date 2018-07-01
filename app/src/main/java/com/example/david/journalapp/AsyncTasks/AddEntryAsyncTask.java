package com.example.david.journalapp.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by David on 30/06/2018.
 */

public class AddEntryAsyncTask extends AsyncTask<String, Void, Boolean> {
    Context context;

    public AddEntryAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... values) {
        String subject = values[0];
        String content = values[1];
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
//                .setTimestampsInSnapshotsEnabled(true)
//                .build();
//        db.setFirestoreSettings(settings);

        Map<String, Object> entry = new HashMap<>();
        entry.put("subject", subject);
        entry.put("content", content);
        db.collection("entries")
                .add(entry)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(context, "entry saved successfully.", Toast.LENGTH_SHORT).show();
                        Log.d("SUCCESS", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "entry failed oooo.", Toast.LENGTH_SHORT).show();
                        Log.w("FAILURE", "Error adding document", e);
                    }
                });
        return true;
    }

    protected void onPostExecute(Boolean result) {
        if (result) {
            Toast.makeText(context, "Message has already been sent.", Toast.LENGTH_SHORT).show();
        }
    }
}
