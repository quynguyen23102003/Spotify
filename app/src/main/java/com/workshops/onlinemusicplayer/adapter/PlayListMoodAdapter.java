package com.workshops.onlinemusicplayer.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.workshops.onlinemusicplayer.R;
import com.workshops.onlinemusicplayer.model.Mood;
import com.workshops.onlinemusicplayer.model.PlayListPopular;

import java.util.ArrayList;

public class PlayListMoodAdapter extends RecyclerView.Adapter<PlayListMoodAdapter.ViewHolder> {

    private ArrayList<Mood> ds;
    private Context context;

    public PlayListMoodAdapter(ArrayList<Mood> ds, Context context) {
        this.ds = ds;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_playlist_mood,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Mood mood = ds.get(position);
        Glide.with(context).load(mood.getImage()).into(holder.imageMood);


    }

    @Override
    public int getItemCount() {
        return ds.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageMood;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMood = itemView.findViewById(R.id.imageMood);

        }
    }
}
