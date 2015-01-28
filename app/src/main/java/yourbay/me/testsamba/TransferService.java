package yourbay.me.testsamba;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import yourbay.me.testsamba.samba.SmbStreamTransfer;

/**
 * Created by ram on 15/1/28.
 */
public class TransferService extends Service {

    SmbStreamTransfer transfer;

    @Override
    public void onCreate() {
        super.onCreate();
        transfer = new SmbStreamTransfer();
        transfer.start();
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
        transfer.stopTransfer();
    }
}
