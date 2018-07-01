package com.example.david.journalapp.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.david.journalapp.R;

import java.util.List;

import com.example.david.journalapp.Models.Entry;

import static com.example.david.journalapp.Helper.getFormatedDate;

/**
 * Created by David on 29/06/2018.
 */

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.MyViewHolder> {

    List<Entry> entries;

    public EntryAdapter(List<Entry> entries) {
        this.entries = entries;
    }


    public  class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView subject, contentSummary, date;

        public MyViewHolder(View itemView) {
            super(itemView);
            subject = (TextView) itemView.findViewById(R.id.subjectId);
            contentSummary = (TextView) itemView.findViewById(R.id.contentSummaryId);
            date = (TextView) itemView.findViewById(R.id.dateId);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Entry entry = entries.get(position);

        holder.subject.setText(entry.getSubject());
        holder.contentSummary.setText(entry.getContent());
        holder.date.setText(getFormatedDate(entry.getDate()));

    }


    @Override
    public int getItemCount() {
        return entries.size();
    }
}
