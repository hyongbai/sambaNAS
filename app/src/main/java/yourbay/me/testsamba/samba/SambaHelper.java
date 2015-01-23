package yourbay.me.testsamba.samba;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

/**
 * Created by ram on 15/1/16.
 */
public class SambaHelper {
    public final static String TAG = "SambaUtil";
    public final static int IO_BUFFER_SIZE = 16 * 1024;
    private final static boolean DEBUG = true;
    public final static String SMB_URL_LAN = "smb://";
    public final static String SMB_URL_WORKGROUP = "smb://workgroup/";

    public final static List<SmbFile> listFiles(IConfig config, String fullPath) throws Exception {
        SmbFile file = new SmbFile(SambaUtil.getFullURL(config, fullPath));
        return new ArrayList<>(Arrays.asList(file.listFiles()));
    }

    public final static boolean upload(IConfig config, String localPath, String remoteFolder) throws Exception {
        File localFile = new File(localPath);
        String remoteFilePath = SambaUtil.wrapPath(remoteFolder, localFile.getName());
        remoteFilePath = SambaUtil.getFullURL(config, remoteFilePath);
        SmbFile remoteFile = new SmbFile(remoteFilePath);
        if (remoteFile.exists()) {
            remoteFilePath = SambaUtil.wrapPath(remoteFolder, SambaUtil.autoRename(localFile.getName()));
            remoteFilePath = SambaUtil.getFullURL(config, remoteFilePath);
            remoteFile = new SmbFile(remoteFilePath);
        }
        if (DEBUG) {
            Log.d(TAG, "config=" + config + "  upload      URL=" + remoteFilePath);
        }
        InputStream inS = new FileInputStream(localFile);
        SmbFileOutputStream outS = new SmbFileOutputStream(remoteFile);
        try {
            writeAndCloseStream(inS, outS, localFile.length());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean download(IConfig config, String localPath, String remotePath) throws Exception {
        if (DEBUG) {
            Log.d(TAG, "download remotePath=" + remotePath + "   local=" + localPath + "  config = " + config);
        }
        if (config != null && !TextUtils.isEmpty(config.user) && !TextUtils.isEmpty(config.password)) {
            String wrappedHost = new StringBuilder(";")//
                    .append(config.user)//
                    .append(":")//
                    .append(config.password)//
                    .append("@")//
                    .append(config.host)//
                    .toString();
            remotePath = remotePath.replace(config.host, wrappedHost);
        }
        SmbFile remoteFile = new SmbFile(remotePath);
        return download(localPath, remoteFile);
    }

    public final static boolean download(String localPath, SmbFile remoteFile) throws Exception {
        final File localFile = new File(localPath);
        final OutputStream outS = new FileOutputStream(localFile);
        final SmbFileInputStream inS = new SmbFileInputStream(remoteFile);
        final long size = remoteFile.length();
        try {
            writeAndCloseStream(inS, outS, size);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private final static void writeAndCloseStream(InputStream ins, OutputStream outs, final long totalSize) throws IOException {
        byte[] tmp = new byte[IO_BUFFER_SIZE];
        int length;
        float uploaded = 0f;
        final long startMills = System.currentTimeMillis();
        long lastMills = startMills;
        final long LOG_PRINT_DURATION_DIVIDER = 500;
        try {
            while ((length = ins.read(tmp)) != -1) {
                outs.write(tmp, 0, length);
                uploaded += length;

                //DELTA TIME
                final long currentMill = System.currentTimeMillis();
                final long delta = currentMill - lastMills;
                if (delta <= LOG_PRINT_DURATION_DIVIDER && uploaded < totalSize) {
                    continue;
                }

                lastMills = currentMill;

                //SPEED
                final float speed = ((float) length) / delta;
                final float avegSpeed = uploaded / (currentMill - startMills);

                //PROGRESS
                final float progress = (totalSize <= 0) ? -1 : (uploaded * 100) / totalSize;
                if (DEBUG) {
                    Log.d(TAG, "writeAndCloseStream progress:" + progress + "    speed=" + speed + "/" + avegSpeed);
                }

                //TODO add progress while uploading
                //TODO upload speed!!
            }
        } catch (Exception e) {
            throw e;
        } finally {
            ins.close();
            outs.flush();
            outs.close();
        }
    }

    public final static boolean createFolder(IConfig config, String parent, String name) throws Exception {
        parent = SambaUtil.getFullURL(config, parent);
        String mURL = SambaUtil.wrapPath(parent, name);
        if (DEBUG) {
            Log.d(TAG, "config=" + config + "createFolder      URL=" + mURL + " parent=" + parent + " name=" + name);
        }
        SmbFile remoteFile = new SmbFile(mURL);
        if (!remoteFile.exists()) {
            remoteFile.mkdir();
            return true;
        }
        return false;
    }


    public final static boolean delete(IConfig config, String path) throws Exception {
        path = SambaUtil.getFullURL(config, path);
        if (DEBUG) {
            Log.d(TAG, "config=" + config + "delete      URL=" + path);
        }
        SmbFile remoteFile = new SmbFile(path);
        if (remoteFile.exists()) {
            remoteFile.delete();
            return true;
        }
        return false;
    }


    public final static SmbFile[] listWorkGroup() {
        try {
            if (DEBUG) {
                Log.d(TAG, "listWorkGroup");
            }
            SmbFile f = new SmbFile(SMB_URL_WORKGROUP);
            return f.listFiles();
        } catch (MalformedURLException murlE) {
            murlE.printStackTrace();
        } catch (IOException ioE) {
            ioE.printStackTrace();
        }
        return null;
    }

    public final static String[] listWorkGroupPath() throws Exception {
        if (DEBUG) {
            Log.d(TAG, "listWorkGroupPath");
        }
        SmbFile f = new SmbFile(SMB_URL_WORKGROUP);
        SmbFile[] files = f.listFiles();
        if (DEBUG) {
            Log.d(TAG, "listWorkGroupPath   " + Arrays.toString(f.list()));
        }
        if (files == null || files.length == 0) {
            return null;
        }
        List<String> l = new ArrayList<>();
        for (SmbFile sf : files) {
            l.add(sf.getName());
        }
        String[] wg = l.toArray(new String[l.size()]);
        return wg;
    }

    public final static String[] listServerPath() throws Exception {
        if (DEBUG) {
            Log.d(TAG, "listServerPath");
        }
        SmbFile f = new SmbFile(SMB_URL_LAN);
        SmbFile[] files = f.listFiles();
        if (DEBUG) {
            Log.d(TAG, "listServerPath   " + Arrays.toString(f.list()));
        }
        if (files == null || files.length == 0) {
            return null;
        }
        List<String> l = new ArrayList<>();
        for (SmbFile sf : files) {
            l.add(sf.getName());
        }
        String[] wg = l.toArray(new String[l.size()]);
        return wg;
    }


}