package yourbay.me.testsamba.samba;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

/**
 * Created by ram on 15/1/27.
 */
public class SFileDescriptorCreator {

    public final static String TAG = "SFileDescriptorCreator";

    public final static ParcelFileDescriptor createDescriptor(String url) {
        ParcelFileDescriptor[] pipe = null;
        try {
            pipe = ParcelFileDescriptor.createPipe();
            ParcelFileDescriptor readSide = pipe[0];
            ParcelFileDescriptor writeSide = pipe[1];
//            write(url, new AssetFileDescriptor(writeSide, 0, -1).createOutputStream());
            write(url, new ParcelFileDescriptor.AutoCloseOutputStream(writeSide));
            Log.e(TAG, "SFileDescriptorCreator  CREATE SUCCEED");
            return readSide;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "SFileDescriptorCreator  CREATE FAILED");
        return null;
    }

    private final static void write(final String url, final OutputStream out) {
        new Thread() {
            @Override
            public void run() {
                try {
                    final SmbFile smbFile = new SmbFile(url);
                    final InputStream in = new SmbFileInputStream(smbFile);
                    final long size = smbFile.length();
                    Log.i(TAG, "SFileDescriptorCreator  write    START  in=" + (in != null) + "  out=" + (out != null) + "    totalSize=" + size + " url=" + url);
//                  Log.i(TAG, "SFileDescriptorCreator  write    printHeader=" + printHeader(smbFile));
                    Log.i(TAG, "SFileDescriptorCreator  write    LENGTH:" + smbFile.getContentLength() + "/" + smbFile.length());
                    SambaHelper.writeAndCloseStream(in, out, size);
                    Log.i(TAG, "SFileDescriptorCreator  write    END");

//                    File f = new File(VideoActivity.URL_TEST_LOCAL_3GP_PATH);
//                    SambaHelper.writeAndCloseStream(new FileInputStream(f.getPath()), out, -1);
//                    SambaHelper.writeAndCloseStream(VideoActivity.context.getAssets().open("baile.3gp"), out, -1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private final static String printHeader(SmbFile smbFile) {
        Map<String, List<String>> headers = smbFile.getHeaderFields();
        StringBuilder builder = new StringBuilder(Arrays.toString(headers.values().toArray()));
        Set<Map.Entry<String, List<String>>> sets = headers.entrySet();
        for (Map.Entry<String, List<String>> entry : sets) {
            builder.append(entry.getKey());
            builder.append(Arrays.toString(entry.getValue().toArray()));
        }
        return builder.toString();
    }

}
