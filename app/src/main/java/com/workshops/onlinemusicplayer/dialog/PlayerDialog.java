package com.workshops.onlinemusicplayer.dialog;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.workshops.onlinemusicplayer.R;
import com.workshops.onlinemusicplayer.fragment.HomeFragment;
import com.workshops.onlinemusicplayer.helper.MusicLibraryHelper;
import com.workshops.onlinemusicplayer.model.Music;
import com.workshops.onlinemusicplayer.player.PlayerListener;
import com.workshops.onlinemusicplayer.player.PlayerManager;
import com.workshops.onlinemusicplayer.player.PlayerQueue;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.workshops.onlinemusicplayer.view.MainActivity;
import com.workshops.onlinemusicplayer.view.PlayPauseView;

import java.util.Locale;

public class PlayerDialog extends BottomSheetDialog implements SeekBar.OnSeekBarChangeListener, PlayerListener, View.OnClickListener {

    private final PlayerManager playerManager;
    private final PlayerQueue playerQueue;

    private final ImageView albumArt;
    private final ImageView repeatControl;
    private final ImageView shuffleControl;
    private final ImageView prevControl;
    private final ImageView nextControl;
    private final PlayPauseView playPauseControl;
    private final TextView songName;
    private final TextView songAlbum;
    private final TextView currentDuration;
    private final TextView totalDuration;
    private final SeekBar songProgress;

    private final Animation animation;

    private Boolean dragging = false;

    public PlayerDialog(@NonNull Context context, PlayerManager playerManager) {
        super(context);
        setContentView(R.layout.dialog_player);

        this.playerManager = playerManager;
        this.playerManager.attachListener(this);
        playerQueue = playerManager.getPlayerQueue();

        albumArt = findViewById(R.id.album_art);
        repeatControl = findViewById(R.id.control_repeat);
        shuffleControl = findViewById(R.id.control_shuffle);
        prevControl = findViewById(R.id.control_prev);
        nextControl = findViewById(R.id.control_next);
        playPauseControl = findViewById(R.id.control_play_pause);
        songName = findViewById(R.id.song_name);
        songAlbum = findViewById(R.id.song_album);
        currentDuration = findViewById(R.id.current_duration);
        totalDuration = findViewById(R.id.total_duration);
        songProgress = findViewById(R.id.song_progress);
        animation = AnimationUtils.loadAnimation(context,
                R.anim.rotate_anim);

        ImageView close_dialog_music = findViewById(R.id.close_dialog_music);
        close_dialog_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(getContext(), MainActivity.class));
            }
        });

        setUpUi();
        setUpListeners();
    }


    private void setUpListeners() {
        songProgress.setOnSeekBarChangeListener(this);
        repeatControl.setOnClickListener(this);
        prevControl.setOnClickListener(this);
        playPauseControl.setOnClickListener(this);
        nextControl.setOnClickListener(this);
        shuffleControl.setOnClickListener(this);
        currentDuration.setText(getContext().getString(R.string.zero_time));
    }

    private void setUpUi() {
        Music music = playerManager.getCurrentMusic();

        songName.setText(music.getName());
        songAlbum.setText(String.format(Locale.getDefault(), "%s • %s",
                music.getSinger(), music.getCategory()));

        Glide.with(getContext().getApplicationContext())
                .load(music.getImage())
                .placeholder(R.drawable.billie_eilish_bad_guy)
                .into(albumArt);

        int icon = playerManager.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play;
        playPauseControl.setImageResource(icon);

        if(playerManager.isPlaying()) albumArt.startAnimation(animation);

        if (playerQueue.isShuffle()) shuffleControl.setAlpha(1f);
        else shuffleControl.setAlpha(0.3f);

        int repeat = playerQueue.isRepeat() ? R.drawable.ic_controls_repeat_one : R.drawable.ic_controls_repeat;
        repeatControl.setImageResource(repeat);

        totalDuration.setText(MusicLibraryHelper.formatDurationTimeStyle(playerManager.getDuration()));

        if (playerManager.getCurrentPosition() < 100)
            currentDuration.setText(MusicLibraryHelper
                    .formatDurationTimeStyle(percentToPosition(playerManager.getCurrentPosition())));
    }

    private int percentToPosition(int percent) {
        return (playerManager.getDuration() * percent) / 100;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        currentDuration.setText(MusicLibraryHelper.formatDurationTimeStyle(percentToPosition(progress)));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        dragging = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        playerManager.seekTo(percentToPosition(seekBar.getProgress()));
        dragging = false;
    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onStateChanged(int state) {
        if (state == State.PLAYING) {
            playPauseControl.setImageResource(R.drawable.ic_pause);
            albumArt.startAnimation(animation);
        }
        else {
            playPauseControl.setImageResource(R.drawable.ic_play);
            albumArt.clearAnimation();
        }

    }

    @Override
    public void onPositionChanged(int position) {
        if (!dragging)
            songProgress.setProgress(position);
    }

    @Override
    public void onMusicSet(Music music) {
        setUpUi();
    }

    @Override
    public void onPlaybackCompleted() {
    }

    @Override
    public void onRelease() {
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.control_repeat) setRepeat();
        else if (id == R.id.control_shuffle) setShuffle();
        else if (id == R.id.control_prev) playerManager.playPrev();
        else if (id == R.id.control_next) playerManager.playNext();
        else if (id == R.id.control_play_pause) playerManager.playPause();

        setUpUi();
    }

    private void setRepeat() {
        playerQueue.setRepeat((!playerQueue.isRepeat()));
    }

    private void setShuffle() {
        playerQueue.setShuffle((!playerQueue.isShuffle()));
    }

    private void closeDialog() {

    }
}
