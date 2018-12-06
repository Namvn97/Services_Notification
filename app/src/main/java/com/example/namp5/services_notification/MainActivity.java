package com.example.namp5.services_notification;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        MyService.OnSyncActivityListerner, SongAdapter.OnClickItemSongListener, SeekBar.OnSeekBarChangeListener {
    public static final int NEXT_SONG = 1;
    public static final int PREVIOUS_SONG = -1;
    public static final int MESSAGE_DELAY = 1000;
    public static final String[] SONG_NAMES = {"Merry Christmas", "Last Christmas"};
    public static final int[] RESOURCE_SONG = {R.raw.wewishyouamerrychristmas, R.raw.lastchristmas};
    private MyService mService;
    private ImageView mImageView_Play;
    private ImageView mImageView_Next;
    private ImageView mImageView_Pre;
    private TextView mTextTimeStart;
    private TextView mTextTimeFinsh;
    private SeekBar mSeekBar;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        Intent intent = new Intent(this, MyService.class);
        if (mService == null) {
            startService(intent);
        }
        bindService(intent, mConnection, BIND_AUTO_CREATE);
        updateTimeSong();
    }

    private void initView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_songs);
        recyclerView.setHasFixedSize(true);
        final SongAdapter myAdapter = new SongAdapter(this);
        recyclerView.setAdapter(myAdapter);
        mImageView_Play = findViewById(R.id.image_play);
        mImageView_Next = findViewById(R.id.image_next);
        mImageView_Pre = findViewById(R.id.image_previous);
        mSeekBar = findViewById(R.id.seek_bar_music);
        mTextTimeStart = findViewById(R.id.text_start_time);
        mTextTimeFinsh = findViewById(R.id.text_finsh_time);
        mImageView_Play.setOnClickListener(this);
        mImageView_Pre.setOnClickListener(this);
        mImageView_Next.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    private void updateTimeSong() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int currentPosition = mService.getCurrrentPosition();
                mSeekBar.setProgress(currentPosition);
                mTextTimeStart.setText(TimeUtil.convertMilisecondToFormatTime(currentPosition));
                mHandler.sendMessageDelayed(new Message(), MESSAGE_DELAY);
            }
        };
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.LocalBinder binder = (MyService.LocalBinder) iBinder;
            mService = binder.getService();
            mService.setSynSeekbarListerner(MainActivity.this);
            syncSeekbar(mService.getDuration());
            syncNotification(mService.isPlaying());
            mHandler.sendMessageDelayed(new Message(), MESSAGE_DELAY);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            unbindService(mConnection);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_play:
                playSong();
                mService.updateNotification();
                break;
            case R.id.image_next:
                mService.changeSong(NEXT_SONG);
                mService.updateNotification();
                mImageView_Play.setImageResource(R.drawable.ic_pause_black_24dp);
                break;
            case R.id.image_previous:
                mService.changeSong(PREVIOUS_SONG);
                mService.updateNotification();
                mImageView_Play.setImageResource(R.drawable.ic_pause_black_24dp);
                break;
        }
    }

    private void playSong() {
        if (mService.isPlaying()) {
            mService.pause();
            mImageView_Play.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        } else {
            mService.start();
            mImageView_Play.setImageResource(R.drawable.ic_pause_black_24dp);
        }
    }

    @Override
    public void syncSeekbar(int max) {
        mSeekBar.setMax(max);
        mTextTimeFinsh.setText(TimeUtil.convertMilisecondToFormatTime(max));
    }

    @Override
    public void syncNotification(boolean isPlaying) {
        if (isPlaying) {
            mImageView_Play.setImageResource(R.drawable.ic_pause_black_24dp);
        } else {
            mImageView_Play.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    @Override
    public void clickItemSongListener(int position) {
        mService.create(position);
        mService.start();
        mImageView_Play.setImageResource(R.drawable.ic_pause_black_24dp);
        mService.updateNotification();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mService.seek(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
