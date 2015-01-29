package yourbay.me.testsamba;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import qpsamba.httpd.IStreamer;
import qpsamba.httpd.NanoStreamer;

/**
 * Created by ram on 15/1/28.
 */
public class StreamService extends Service {

    public static IStreamer iStreamer;

    @Override
    public void onCreate() {
        super.onCreate();
//        iStreamer = new CyberSmbStreamer();
        iStreamer = new NanoStreamer();
        iStreamer.start();
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
