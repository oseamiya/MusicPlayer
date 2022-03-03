package com.oseamiya.themusicplayer;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TheMusicPlayer extends AndroidNonvisibleComponent {
  private final Context context;
  private String fileUrl;
  private MediaPlayer mediaPlayer;
  private boolean isPause = false;
  private int currentPosition = 0;
  private int factor = 0;
  ExecutorService executorService = Executors.newSingleThreadExecutor();
  public TheMusicPlayer(ComponentContainer container) {
    super(container.$form());
    context = container.$context();
  }

  @SimpleProperty
  public void Url(String url){
    this.fileUrl = url;
  }
  @SimpleEvent
  public void OnError(String error){
      EventDispatcher.dispatchEvent(this, "OnError", error);
  }
  @SimpleEvent
  public void OnLoading(){
      EventDispatcher.dispatchEvent(this,"OnLoading");
  }
  @SimpleEvent
  public void OnCompleted(){
    EventDispatcher.dispatchEvent(this, "OnCompleted");
  }
  @SimpleEvent
  public void OnBufferingUpdate(int percent){
    EventDispatcher.dispatchEvent(this, "OnBufferingUpdate", percent);
  }
  @SimpleEvent
  public void OnSeekComplete(){
      factor = 0 ;
      EventDispatcher.dispatchEvent(this, "OnSeekComplete");
  }
  @SimpleFunction
  public void Start(){
    stopPlaying();
    this.mediaPlayer = new MediaPlayer();
    this.mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
            OnBufferingUpdate(i);
        }
    });
    this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            OnCompleted();
        }
    });
    this.mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            OnSeekComplete();
        }
    });
    this.mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            OnError("Error " + i + i1);
            return false;
        }
    });
    TheMusicPlayer.this.OnLoading();
    executorService.execute(new Runnable() {
        @Override
        public void run() {
            try {
                TheMusicPlayer.this.mediaPlayer.setDataSource(fileUrl);
                TheMusicPlayer.this.mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                OnError(e.getMessage());
            }
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!mediaPlayer.isPlaying()){
                        mediaPlayer.start();
                    }
                }
            });
        }
    });
  }
  @SimpleProperty
  public int Duration(){
    return mediaPlayer == null ? 0 : mediaPlayer.getDuration();
  }
  @SimpleProperty
  public int CurrentPosition(){
      return mediaPlayer == null ? 0 : mediaPlayer.getCurrentPosition();
  }
  @SimpleProperty
  public boolean IsPlaying(){
      return mediaPlayer != null && (mediaPlayer.isPlaying() && factor==0);
  }
  @SimpleFunction
  public void StopPlaying(){
    stopPlaying();
  }
  @SimpleFunction
  public void Pause(){
      if(mediaPlayer != null){
        if(mediaPlayer.isPlaying()) {
          mediaPlayer.pause();
          this.currentPosition = mediaPlayer.getCurrentPosition();
          this.isPause = true;
        }
      }
  }
  @SimpleFunction
  public void Resume(){
      if(mediaPlayer != null){
        if(this.isPause) {
          mediaPlayer.seekTo(this.currentPosition);
          mediaPlayer.start();
          this.isPause = false;
        }
      }
  }
  @SimpleProperty
  public boolean IsPause(){
    return this.isPause;
  }
  @SimpleFunction
  public void ReleasePlayer(){
    if(this.mediaPlayer != null){
      mediaPlayer.release();
    }
  }
  @SimpleFunction
  public void SeekTo(int position){
      factor = 1;
      OnLoading();
      if(mediaPlayer != null){
          mediaPlayer.seekTo(position);
          if(!mediaPlayer.isPlaying()){
              mediaPlayer.start();
          }
      }
  }
  @SimpleFunction
  public void Reset(){
    if(mediaPlayer != null){
      mediaPlayer.reset();
    }
  }
  @SimpleFunction
  public String millisecondsToTime(int milliseconds){
    String result = "";
    String stringForSeconds = "";
    int hours = (int) (milliseconds / 3600000L);
    int minutes = (int) (milliseconds % 3600000L) / 60000;
    int seconds = (int) (milliseconds % 3600000L % 60000L / 1000L);
    if(hours > 0){
      result = hours + ":";
    }
    stringForSeconds = seconds < 10 ? "0" + seconds : "" + seconds;
    return result + minutes + ":" + stringForSeconds;
  }
  @SimpleFunction
  public void Forward(int milliseconds){
      if(mediaPlayer != null) {
          if (mediaPlayer.getCurrentPosition() < (mediaPlayer.getDuration() - milliseconds)) {
              SeekTo(mediaPlayer.getCurrentPosition() + milliseconds);
          }
      }
  }
  @SimpleFunction
  public void Replay(int milliseconds){
      if(mediaPlayer != null){
          if(mediaPlayer.getCurrentPosition() > milliseconds){
              SeekTo(mediaPlayer.getCurrentPosition() - milliseconds);
          }
      }
  }

  private void stopPlaying(){
    if(this.mediaPlayer != null){
      this.mediaPlayer.stop();
      this.mediaPlayer.release();
    }
  }
}
