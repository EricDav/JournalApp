package com.example.david.journalapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.david.journalapp.Adapters.EntryAdapter;
import com.example.david.journalapp.R;
import com.example.david.journalapp.RecyclerTouchListner;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.example.david.journalapp.Models.Entry;

import static com.example.david.journalapp.Helper.cancelProgress;
import static com.example.david.journalapp.Helper.openProgress;

public class Main2Activity extends AppCompatActivity {
    TextView textView;
    List<Entry> entries = new ArrayList<>();
    SharedPreferences sharedPref;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_account_circle_black_24dp);
        toolbar.setOverflowIcon(drawable);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Main2Activity.this, CreateEntry.class), 1);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        textView = findViewById(R.id.emptyMainTextId);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListner(getApplicationContext(), recyclerView, new RecyclerTouchListner.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Entry entry = entries.get(position);
                Intent intent = new Intent(Main2Activity.this, EntryDetails.class);
                intent.putExtra("id", entry.getEntryId());
                intent.putExtra("date", entry.getDate());
                intent.putExtra("subject", entry.getSubject());
                intent.putExtra("content", entry.getContent());

                startActivityForResult(intent, 2);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        if (savedInstanceState != null && savedInstanceState.containsKey("subjects")) {
            textView.setVisibility(View.GONE);
            String[] subjects = savedInstanceState.getStringArray("subjects");
            String[] contents = savedInstanceState.getStringArray("contents");
            String[] ids = savedInstanceState.getStringArray("ids");
            String[] dates = savedInstanceState.getStringArray("dates");

            createEntries(subjects, contents, ids, dates);
            setRecyclerViewLayout(entries);
            Log.d("SAVED", "I GOT IN SAVED INSTANCE STATE");
        } else {
            fetchEntries(true);
        }
    }

    private void createEntries(String[] subjects, String[] contents, String[] ids, String[] dates) {
        List<Entry> savedEntries = new ArrayList<>();
        for (int i = 0; i < subjects.length; i++) {
            Entry entry = new Entry(ids[i], subjects[i], contents[i], sharedPref.getString("loginUser", ""), dates[i]);
            savedEntries.add(entry);
        }

        entries = savedEntries;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("SAVING_HERE", "I AM SAVING STATE");
        if (entries != null && entries.size() != 0) {
            String[] subjects = new String[entries.size()];
            String[] contents = new String[entries.size()];
            String[] ids = new String[entries.size()];
            String[] dates = new String[entries.size()];

            for (int i = 0; i < entries.size(); i++) {
                subjects[i] = entries.get(i).getSubject();
                contents[i] = entries.get(i).getContent();
                ids[i] = entries.get(i).getEntryId();
                dates[i] = entries.get(i).getDate();
            }

            outState.putStringArray("subjects", subjects);
            outState.putStringArray("contents", contents);
            outState.putStringArray("ids", ids);
            outState.putStringArray("dates", dates);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 1) {
            String subject = data.getStringExtra("subject");
            String content = data.getStringExtra("content");
            String id = data.getStringExtra("id");
            String date = data.getStringExtra("date");
            String currentUser = sharedPref.getString("loginUser", "");
            Entry entry = new Entry(id, subject, content, currentUser, date);
            entries.add(entry);
            setRecyclerViewLayout(entries);
        } else if (resultCode == RESULT_OK && requestCode == 2) {
            if (data.hasExtra("deletedEntryId")) {
                deleteEntry(data.getStringExtra("deletedEntryId"));
                setRecyclerViewLayout(entries);
            } else if (data.hasExtra("isUpdated")) {
                String content = data.getStringExtra("content");
                String subject = data.getStringExtra("subject");
                String id = data.getStringExtra("id");

                updateEntry(id, subject, content);
            }
        }

        if (entries.size() > 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText("You did not have any entry in your diary. Click the icon below to create an entry");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (sharedPref.getBoolean("isUpdated", false)) {
            String content = sharedPref.getString("content", "");
            String subject = sharedPref.getString("subject", "");
            String id = sharedPref.getString("id", "");

            updateEntry(id, subject, content);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("isUpdated", false);
            editor.apply();
        }
    }

    /**
     * It updates an entry.
     *
     * @param id The id of the entry to be updated.
     * @param subject The updated subject.
     * @param content The updated content.
     */
    private void updateEntry(String id, String subject, String content) {
        ArrayList<Entry> updatedEntry = new ArrayList<>();
        for (Entry entry: entries) {
            if (entry.getEntryId().equals(id)) {
                entry.setContent(content);
                entry.setSubject(subject);
            }
            updatedEntry.add(entry);
        }

        entries = updatedEntry;
        setRecyclerViewLayout(entries);
    }

    /**
     * Fetch all the entries the current user has created from firebase.
     */
    public void fetchEntries(final boolean shouldDisplayError) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        entries = new ArrayList<>();


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        db.collection("entries")
                .whereEqualTo("userEmail", sharedPref.getString("loginUser", ""))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().isEmpty()) {
                                textView.setText("You did not have any entry in your diary. Click the icon below to create an entry");
                            } else {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Entry entry = new Entry(document.getId(), document.get("subject").toString(), document.get("content").toString(), document.get("userEmail").toString(), document.get("date").toString());
                                    entries.add(entry);
                                }

                                setRecyclerViewLayout(entries);
                                textView.setVisibility(View.GONE);
                            }
                        } else {
                            if (shouldDisplayError) {
                                Button button = findViewById(R.id.reloadId);
                                button.setVisibility(View.VISIBLE);
                                textView.setText("An error occurred while fetching entries");
                            }

                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.getItem(0).setTitle(sharedPref.getString("loginUser", ""));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                signOut();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }

    }

    /**
     * It sets the Recycler view layouts.
     */
    public void setRecyclerViewLayout(List<Entry> entries) {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(new EntryAdapter(sortEntries(entries)));
    }

    /**
     * Deletes an entry from the list of entries.
     *
     * @param entryId The id of the entry to be deleted.
     */
    public void deleteEntry(String entryId) {
        ArrayList<Entry> updatedEntry = new ArrayList<>();
        for (Entry entry: entries) {
            if (!entry.getEntryId().equals(entryId)) {
                updatedEntry.add(entry);
            }
        }

        entries = updatedEntry;
    }

    /**
     * Sign out the current user and returns back to login page.
     */
    private void signOut() {
        final ProgressDialog progress = new ProgressDialog(this);
        openProgress(progress, "logging out");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        cancelProgress(progress);
                        sharedPref.edit().clear().apply();
                        startActivity(new Intent(Main2Activity.this, SignInActivity.class));
                        finish();
                    }
                });
    }


    /**
     * It sorts entries from the most recent to the least.
     *
     * @param entries The entries.
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

                if (calendar.getTimeInMillis() > calendar1.getTimeInMillis()) {
                    Collections.swap(entries, i, i-1);
                    hasBeenSorted +=1;
                }
            }
            isSorted = hasBeenSorted == 0 ? true : false;
        }
        return entries;
    }
}
