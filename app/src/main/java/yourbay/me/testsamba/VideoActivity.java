package yourbay.me.testsamba;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.VideoView;

/**
 * Created by ram on 15/1/22.
 */
public class VideoActivity extends Activity {

    public final static String ACTION_KEY_URL = "URL";
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoView = new VideoView(this);
        setContentView(videoView);
        checkUrl();
        MediaPlayer mediaPlayer = new MediaPlayer();
    }

    private void checkUrl() {
        try {
            Intent intent = getIntent();
            if (intent.hasExtra(ACTION_KEY_URL)) {
                String url = intent.getStringExtra(ACTION_KEY_URL);
                play(url);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void play(String url) {
        videoView.setVideoPath(url);
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoView.pause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        videoView.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
    }
}
