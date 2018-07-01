package com.example.david.journalapp.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.example.david.journalapp.Activities.EntryDetails;

/**
 * Created by David on 01/07/2018.
 */

public class DeleteEntryDialog extends DialogFragment {

    ProgressDialog progress;
    // Use the Builder class for convenient dialog construction

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());

        builder.setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((EntryDetails) getActivity()).deleteEntry();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void deleteEntry() {

    }

    public void dismissLoader() {
        progress.dismiss();
    }
}
