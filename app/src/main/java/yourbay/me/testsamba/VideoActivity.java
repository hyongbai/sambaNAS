package yourbay.me.testsamba;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.FileDescriptor;

import yourbay.me.testsamba.samba.SambaUtil;
import yourbay.me.testsamba.util.IntentUtils;

/**
 * Created by ram on 15/1/22.
 */
public class VideoActivity extends Activity {

    public static Context context;
    public final static String URL_TEST_LOCAL_VIDEO_PATH = "/storage/emulated/0/test/samba/test.mp4";
    public final static String URL_TEST_LOCAL_3GP_PATH = "/storage/emulated/0/baile.3gp";
    public final static String URL_TEST_REMOTE_3gp = "http://f.pepst.com/c/d/EF25EB/480964-8231/ssc3/home/005/tikowap.wap/albums/baile.3gp";
    public final static String URL_TEST_REMOTE_MP4 = "http://vjs.zencdn.net/v/oceans.mp4";
    //    public final static String TAG = "VideoActivity";
    public final static String TAG = "VideoActivity";
    public final static String ACTION_KEY_URL = "URL";
    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private int position;
    private String mURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_video);
        initSurface();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_video, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_open_3rd) {
            IntentUtils.openVideo(this, mURL);
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop();
    }

    private void initSurface() {
        Intent intent = getIntent();
        if (!intent.hasExtra(ACTION_KEY_URL)) {
            return;
        }
        mediaPlayer = new MediaPlayer();
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                log("surfaceDestroyed");
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                log("surfaceCreated");
                setData();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                log("surfaceChanged");
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                log("onError    what=" + what + "  extra=" + extra);
                return false;
            }
        });
    }

    private void setData() {
        try {
            String url = getIntent().getStringExtra(ACTION_KEY_URL);
            url = SambaUtil.wrapStreamURL(url, TransferService.iStreamer.getIp(), TransferService.iStreamer.getPort());
            mURL = url;
//            FileDescriptor fd = null;
//            fd = new FileInputStream(URL_TEST_LOCAL_3GP_PATH).getFD();
//            ParcelFileDescriptor pfd = SFileDescriptorCreator.createDescriptor(url);
//            fd = pfd.getFileDescriptor();
            final Object source = url;
            new Thread() {
                @Override
                public void run() {
                    prepare(source);
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepare(Object source) {
        try {
            log("prepare     FD =  " + String.valueOf(source));
            //reset
            mediaPlayer.reset();
            log("reset");

            //audio
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            log("setAudioStreamType");

            //DataSource
            if (source instanceof String) {
                mediaPlayer.setDataSource(String.valueOf(source));
            } else if (source instanceof FileDescriptor) {
                mediaPlayer.setDataSource((FileDescriptor) source);
            }
            log("setDataSource");

            //attach surfaceView
            mediaPlayer.setDisplay(surfaceView.getHolder());
            log("setDisplay");

            //prepare
            mediaPlayer.prepare();
            log("prepare");

            //start
            mediaPlayer.start();
            log("start");
        } catch (Exception e) {
            e.printStackTrace();
            log("prepare    Exception:" + e.getMessage() + " " + e.getClass().getName());
        }
    }

    private void stop() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            position = mediaPlayer.getCurrentPosition();
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void pause() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            position = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        }
    }

    private void resume() {
        if (mediaPlayer == null || mediaPlayer.isPlaying()) {
            return;
        }
//        mediaPlayer.seekTo(position);
//        mediaPlayer.s
    }

    private void log(String msg) {
        Log.d(TAG, "VideoActivity   " + msg);
    }

}
