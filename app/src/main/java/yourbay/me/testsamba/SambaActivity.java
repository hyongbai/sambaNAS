package yourbay.me.testsamba;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import yourbay.me.testsamba.samba.Config;
import yourbay.me.testsamba.samba.ConfigDesk79;
import yourbay.me.testsamba.samba.SambaUtil;

/**
 * Created by ram on 15/1/20.
 */
public class SambaActivity extends Activity {

    protected final static String TAG = SambaUtil.TAG;
    protected final static String LOCAL_FOLDER_PATH = "/test/samba";
    protected final static String REMOTE_PARENT = "   ... ";
    protected final static String REMOTE_FOLDER_PREFIX = " > ";
    protected final static String REMOTE_FILE_PREFIX = "    ";
    protected final static int REQUEST_CODE_CHOOSE_IMAGE = 1234;
    protected final static int REQUEST_CODE_CHOOSE_VIDEO = 1235;
    protected SmbFile EMPTY_REMOTE_FILE;

    protected Config mConfig;
    protected Map<String, SmbFile> REMOTE_PATHS = new LinkedHashMap<>();
    protected String curRemoteFolder;
    protected String curRemoteFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfig = new ConfigDesk79();
        try {
            EMPTY_REMOTE_FILE = new SmbFile("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void listAndPrepare(String path) {
        List<SmbFile> FILES = SambaUtil.listFiles(mConfig, path);
        Map<String, SmbFile> MAP = SmbFileToMap(FILES);
        prepareCurrentMap(MAP);
    }

    protected void prepareCurrentMap(Map<String, SmbFile> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        synchronized (REMOTE_PATHS) {
            REMOTE_PATHS.clear();
            REMOTE_PATHS.put(REMOTE_PARENT, EMPTY_REMOTE_FILE);
            REMOTE_PATHS.putAll(map);
        }
    }


    protected final Map<String, SmbFile> SmbFileToMap(List<SmbFile> files) {
        if (files == null || files.isEmpty()) {
            return null;
        }
        Map<String, SmbFile> FILE = new LinkedHashMap<>();
        Map<String, SmbFile> FOLDER = new LinkedHashMap<>();
        for (SmbFile file : files) {
            try {
                String path = file.getPath();
                path = path.replace("smb://" + mConfig.host, "");
                if (file.isDirectory()) {
                    FOLDER.put(new StringBuilder(REMOTE_FOLDER_PREFIX).append(path).toString(), file);
                } else {
                    FILE.put(new StringBuilder(REMOTE_FILE_PREFIX).append(path).toString(), file);
                }
            } catch (SmbException e) {
                e.printStackTrace();
                FILE.put("  | " + file.getPath(), file);
            }
        }
        FOLDER.putAll(FILE);
        return FOLDER;
    }


    /* *************ACTIONS****************/
    protected final void upload(final String path) {
        new Thread() {
            @Override
            public void run() {
                boolean result = SambaUtil.upload(mConfig, path, "/samba/0upload/");
                updateResult("upload", path + " " + String.valueOf(result).toUpperCase());
                onFolderChange(path, result);
            }
        }.start();///folder
    }

    protected final void download(final String localPath, final String remotePath) {
        Log.d(TAG, "download      " + localPath);
        new Thread() {
            @Override
            public void run() {
                boolean result = SambaUtil.download(mConfig, localPath, remotePath);//"smb://192.168.2.79/samba/0upload/IMG_test_fix_exif_date.jpg"
                updateResult("download", localPath + " " + String.valueOf(result).toUpperCase());
            }
        }.start();///folder
    }

    protected void delele(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result = SambaUtil.delete(mConfig, path);
                onFolderChange(path, result);
                updateResult("DELETE", path + " " + String.valueOf(result).toUpperCase());
            }
        }).start();
    }

    protected void onFolderChange(String path, boolean result) {

    }

    protected void list() {
        curRemoteFolder = SambaUtil.getRemotePath("/");
    }

    protected void createFolder(final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result = SambaUtil.createFolder(mConfig, curRemoteFolder, name);
                onFolderChange(curRemoteFolder, result);
                updateResult("createFolder", SambaUtil.wrapPath(curRemoteFolder, name) + "       " + String.valueOf(result).toUpperCase());
            }
        }).start();
    }

    protected void updateResult(final String action, final String msg) {

    }
}
