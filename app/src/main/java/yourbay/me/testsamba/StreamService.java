package yourbay.me.testsamba;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import yourbay.me.testsamba.samba.httpd.IStreamer;
import yourbay.me.testsamba.samba.httpd.NanoSmbStreamer;

/**
 * Created by ram on 15/1/28.
 */
public class StreamService extends Service {

    public static IStreamer iStreamer;

    @Override
    public void onCreate() {
        super.onCreate();
//        iStreamer = new CyberSmbStreamer();
        iStreamer = new NanoSmbStreamer();
        iStreamer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        iStreamer.stopStream();
    }
}
