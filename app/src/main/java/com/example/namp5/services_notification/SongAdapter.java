package com.example.namp5.services_notification;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by namp5 on 12/3/2018.
 */


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder>  {
    private OnClickItemSongListener mListener;

    public SongAdapter(OnClickItemSongListener listener){
    this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(mListener, position);
    }

    @Override
    public int getItemCount() {
        return  MainActivity.SONG_NAMES.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTextView;
        private OnClickItemSongListener mListener;
        private int mPosition;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.text_song_name);
        }

        public void bindData(final OnClickItemSongListener listener, final int position) {
            mPosition = position ;
            mListener = listener ;
            mTextView.setText(MainActivity.SONG_NAMES[position]);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.clickItemSongListener(mPosition);
        }
    }
    interface OnClickItemSongListener {
        void clickItemSongListener(int position);
    }
}
