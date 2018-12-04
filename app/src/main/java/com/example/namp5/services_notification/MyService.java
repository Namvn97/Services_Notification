package com.example.namp5.services_notification;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

/**
 * Created by namp5 on 11/30/2018.
 */

public class MyService extends Service implements MusicInterface, MediaPlayer.OnCompletionListener {
    public static final String EXTRA_REQUEST_CODE = "REQUEST CODE";
    public static final int VALUE_NEXT_SONG = 1112;
    public static final int VALUE_PREVIOUS_SONG = 1113;
    public static final int VALUE_PLAY_SONG = 1114;
    public static final int CHANGE_POSITION = 1;

    private final IBinder mBinder = new LocalBinder();
    private MediaPlayer mMediaPlayer;
    private PendingIntent mNextPendingIntent;
    private PendingIntent mPreviousPendingIntent;
    private PendingIntent mPlayPendingIntent;
    private OnSyncActivityListerner mListerner;
    private int mCurrentIndex;
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mNotificationLayout;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int request = intent.getIntExtra(EXTRA_REQUEST_CODE, 0);
            switch (request) {
                case VALUE_PLAY_SONG:
                    playSong();
                    updateNotification();
                    break;
                case VALUE_NEXT_SONG:
                    changeSong(MainActivity.NEXT_SONG);
                    mListerner.syncNotification(true);
                    updateNotification();
                    break;
                case VALUE_PREVIOUS_SONG:
                    changeSong(MainActivity.PREVIOUS_SONG);
                    mListerner.syncNotification(true);
                    updateNotification();
                    break;
            }
        }
        initLayoutForNotification(R.layout.notification_custom,
                MainActivity.SONG_NAMES[getCurrentIndex()], isPlaying());
        createNextPendingIntent();
        createPreviousPendingIntent();
        createMusicNotification();
        createPlayPendingIntent();
        return START_STICKY;
    }
    private void playSong(){
            if (isPlaying()) {
                this.pause();
                mListerner.syncNotification(false);
            } else {
                this.start();
                mListerner.syncNotification(true);
            }
    }

    private void createPlayPendingIntent() {
        Intent nextIntent = new Intent(getApplicationContext(), MyService.class);
        nextIntent.putExtra(EXTRA_REQUEST_CODE, VALUE_PLAY_SONG);
        mPlayPendingIntent = PendingIntent.getService(getApplicationContext(),
                VALUE_PLAY_SONG, nextIntent, 0);
        mNotificationLayout.setOnClickPendingIntent(R.id.image_play, mPlayPendingIntent);
    }

    private void createNextPendingIntent() {
        Intent nextIntent = new Intent(getApplicationContext(), MyService.class);
        nextIntent.putExtra(EXTRA_REQUEST_CODE, VALUE_NEXT_SONG);
        mNextPendingIntent = PendingIntent.getService(getApplicationContext(),
                VALUE_NEXT_SONG, nextIntent, 0);
        mNotificationLayout.setOnClickPendingIntent(R.id.image_next, mNextPendingIntent);
    }

    private void createPreviousPendingIntent() {
        Intent nextIntent = new Intent(getApplicationContext(), MyService.class);
        nextIntent.putExtra(EXTRA_REQUEST_CODE, VALUE_PREVIOUS_SONG);
        mPreviousPendingIntent = PendingIntent.getService(getApplicationContext(),
                VALUE_PREVIOUS_SONG, nextIntent, 0);
        mNotificationLayout.setOnClickPendingIntent(R.id.image_previous, mPreviousPendingIntent);

    }

    public void setSynSeekbarListerner(OnSyncActivityListerner listerner) {
        mListerner = listerner;
    }

    public void createMusicNotification() {
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContent(mNotificationLayout);
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPenddingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPenddingIntent);
        startForeground(CHANGE_POSITION, mBuilder.build());
    }

    private void initLayoutForNotification(int resourceLayout, String songName, boolean isPlaying) {
        mNotificationLayout = new RemoteViews(getPackageName(), resourceLayout);
        mNotificationLayout.setTextViewText(R.id.text_song_name, songName);
        if (isPlaying) {
            mNotificationLayout.setImageViewResource(R.id.image_play, R.drawable.ic_pause_black_24dp);
        } else {
            mNotificationLayout.setImageViewResource(R.id.image_play, R.drawable.ic_play_arrow_black_24dp);
        }
    }

    public void updateNotification() {
        mNotificationLayout.setTextViewText(R.id.text_song_name,
                MainActivity.SONG_NAMES[mCurrentIndex]);
        if (isPlaying()) {
            mNotificationLayout.setImageViewResource(R.id.image_play, R.drawable.ic_pause_black_24dp);
        } else {
            mNotificationLayout.setImageViewResource(R.id.image_play, R.drawable.ic_play_arrow_black_24dp);
        }
        mBuilder.setContent(mNotificationLayout);
        startForeground(CHANGE_POSITION, mBuilder.build());

    }

    public int getCurrentIndex() {
        return mCurrentIndex;
    }

    @Override
    public void create(int index) {
        mCurrentIndex = index;
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        int resourceSong = MainActivity.RESOURCE_SONG[mCurrentIndex];
        mMediaPlayer = MediaPlayer.create(this, resourceSong);
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnCompletionListener(MyService.this);
        }
    }

    @Override
    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            mListerner.syncSeekbar(mMediaPlayer.getDuration());
        } else {
            create(0);
            mMediaPlayer.start();
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null ? mMediaPlayer.isPlaying() : false;
    }

    @Override
    public int getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    @Override
    public int getCurrrentPosition() {
        return mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
    }

    @Override
    public void changeSong(int i) {
        mCurrentIndex += i;
        if (mCurrentIndex >= MainActivity.SONG_NAMES.length) {
            mCurrentIndex = 0;
        } else if (mCurrentIndex < 0) {
            mCurrentIndex = MainActivity.SONG_NAMES.length - CHANGE_POSITION;

        }
        this.create(mCurrentIndex);
        this.start();
    }

    @Override
    public void seek(int possition) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(possition);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        this.changeSong(MainActivity.NEXT_SONG);
    }

    public class LocalBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    interface OnSyncActivityListerner {
        void syncSeekbar(int max);

        void syncNotification(boolean isPlaying);
    }
}
