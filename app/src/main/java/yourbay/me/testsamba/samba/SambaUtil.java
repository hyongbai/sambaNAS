package yourbay.me.testsamba.samba;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

/**
 * Created by ram on 15/1/16.
 */
public class SambaUtil {
    public final static String TAG = "SambaUtil";
    public final static int IO_BUFFER_SIZE = 16 * 1024;

    public final static String listFiles(String path) {
        Config config = new ConfigDesk79();
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, config.user, config.password);
        String URL = new StringBuilder("smb://").append(config.host).append(path).toString();//.append(":445")
        try {
            Log.d(TAG, "listFiles   URL=" + URL);
            SmbFile file = new SmbFile(URL, auth);
            String fileStr = strsToString(file.listFiles());
            Log.d(TAG, "listFiles   SmbFile:" + fileStr);
            Log.d(TAG, "listFiles   strs:" + strsToString(file.list()));
            return fileStr;
        } catch (SmbException smb) {
            smb.printStackTrace();
        } catch (MalformedURLException murlE) {
            murlE.printStackTrace();
        } catch (Exception ioE) {
            ioE.printStackTrace();
        }
        return null;
    }

    public final static boolean upload(String localPath, String remoteFolder) {
        Config config = new ConfigDesk79();
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, config.user, config.password);
        File localFile = new File(localPath);
        String mURL = new StringBuilder("smb://").append(config.host).append(remoteFolder).append(localFile.getName()).toString();
        try {
            Log.d(TAG, "upload      URL=" + mURL);
            SmbFile remoteFile = new SmbFile(mURL, auth);
            InputStream inS = new FileInputStream(localFile);
            SmbFileOutputStream outS = new SmbFileOutputStream(remoteFile);
            try {
                writeStream(inS, outS, localFile.length());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                inS.close();
                outS.flush();
                outS.close();
            }
        } catch (SmbException smb) {
            smb.printStackTrace();
        } catch (MalformedURLException murlE) {
            murlE.printStackTrace();
        } catch (IOException ioE) {
            ioE.printStackTrace();
        }
        return false;
    }

    public final static boolean download(String localPath, String remoteFolder) {
        Config config = new ConfigDesk79();
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, config.user, config.password);
        File localFile = new File(localPath);
        String mURL = new StringBuilder("smb://").append(config.host).append(remoteFolder).append(localFile.getName()).toString();
        try {
            Log.d(TAG, "upload      URL=" + mURL);
            SmbFile remoteFile = new SmbFile(mURL, auth);
            OutputStream outS = new FileOutputStream(localFile);
            SmbFileInputStream inS = new SmbFileInputStream(remoteFile);
            try {
                writeStream(inS, outS, localFile.length());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                inS.close();
                outS.flush();
                outS.close();
            }
        } catch (SmbException smb) {
            smb.printStackTrace();
        } catch (MalformedURLException murlE) {
            murlE.printStackTrace();
        } catch (IOException ioE) {
            ioE.printStackTrace();
        }
        return false;
    }

    public final static String strsToString(SmbFile[] strs) {
        if (strs == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(strs.length);
        for (SmbFile smbFile : strs) {
            builder.append("\n");
            builder.append("[");
            builder.append(smbFile.getPath());
            builder.append("]");
        }
        return builder.toString();
    }

    public final static String strsToString(String[] strs) {
        if (strs == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (String str : strs) {
            builder.append("\n");
            builder.append("[");
            builder.append(str);
            builder.append("]");
        }
        return builder.toString();
    }


    public final static void writeStream(InputStream ins, OutputStream outs, final long totalSize) throws IOException {
        byte[] tmp = new byte[IO_BUFFER_SIZE];
        int length;
        float uploaded = 0f;
        long lastMills = System.currentTimeMillis();
        while ((length = ins.read(tmp)) != -1) {
            outs.write(tmp, 0, length);
            uploaded += length;

            //DELTA TIME
            final long currentMill = System.currentTimeMillis();
            final long delta = currentMill - lastMills;
            lastMills = currentMill;

            //SPEED
            final float speed = ((float) length) / delta;

            //PROGRESS
            final float progress = (uploaded * 100) / totalSize;
            Log.d(TAG, "writeStream progress:" + progress + "    speed=" + speed);

            //TODO add progress while uploading
            //TODO upload speed!!
        }
    }

}
