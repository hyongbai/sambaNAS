package yourbay.me.testsamba;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbFile;
import qpsamba.IConfig;
import qpsamba.SambaHelper;
import qpsamba.SambaUtil;

/**
 * Created by ram on 15/1/20.
 */
public class SambaActivity extends Activity {

    protected final static String TAG = SambaHelper.TAG;
    protected final static String LOCAL_FOLDER_PATH = "/test/samba";
    protected final static String REMOTE_PARENT = "   ... ";
    protected final static String REMOTE_FOLDER_PREFIX = " > ";
    protected final static String REMOTE_FILE_PREFIX = "    ";
    protected final static int REQUEST_CODE_CHOOSE_IMAGE = 1234;
    protected final static int REQUEST_CODE_CHOOSE_VIDEO = 1235;
    protected SmbFile EMPTY_REMOTE_FILE;

    protected IConfig mConfig;
    protected Map<String, SmbFile> REMOTE_PATHS = new LinkedHashMap<>();
    protected String curRemoteFolder;
    protected String curRemoteFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfig = new DefaultConfig();
        try {
            EMPTY_REMOTE_FILE = new SmbFile("");
        } catch (Exception e) {
        }
    }

    protected void listAndPrepare(String path) {
        try {
            List<SmbFile> FILES = SambaHelper.listFiles(mConfig, path);
            Map<String, SmbFile> MAP = SmbFileToMap(FILES);
            prepareCurrentMap(MAP);
        } catch (Exception e) {
            handleException(e);
        }
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
                final String path = removeHost(file.getPath());
                if (file.isDirectory()) {
                    FOLDER.put(new StringBuilder(REMOTE_FOLDER_PREFIX).append(path).toString(), file);
                } else {
                    FILE.put(new StringBuilder(REMOTE_FILE_PREFIX).append(path).toString(), file);
                }
            } catch (Exception e) {
                e.printStackTrace();
                FILE.put("  | " + file.getPath(), file);
            }
        }
        FOLDER.putAll(FILE);
        return FOLDER;
    }

    protected final String removeHost(String path) {
        if (TextUtils.isEmpty(path)) {
            return path;
        }
        if (path.startsWith("smb://")) {
            path = path.replace("smb://", "");
        }
        if (!path.contains("/")) {
            return path;
        }
        int index = path.indexOf("/");
        path = path.substring(index);
        return path;
    }


    /* *************ACTIONS****************/
    protected final void upload(final String path) {
        new Thread() {
            @Override
            public void run() {

                boolean result = false;
                try {
                    result = SambaHelper.upload(mConfig, path, curRemoteFolder);
                } catch (Exception e) {
                    handleException(e);
                }
                updateResult("upload", path + " " + String.valueOf(result).toUpperCase());
                onRemoteFolderChange(curRemoteFolder, result);
            }
        }.start();///folder
    }

    protected final void download(final String localPath, final String remoteFolder) {
        Log.d(TAG, "download      " + localPath);
        new Thread() {
            @Override
            public void run() {
                boolean result = false;
                try {
                    result = SambaHelper.download(mConfig, localPath, remoteFolder);//"smb://192.168.2.79/samba/0upload/IMG_test_fix_exif_date.jpg"
                } catch (Exception e) {
                    handleException(e);
                }
                updateResult("download", localPath + " " + String.valueOf(result).toUpperCase());
            }
        }.start();///folder
    }

    protected final void downloadInFullPath(final String localPath, final String remoteFolder) {
        Log.d(TAG, "download      " + localPath);
        new Thread() {
            @Override
            public void run() {
                boolean result = false;
                try {
                    result = SambaHelper.download(mConfig, localPath, remoteFolder);//"smb://192.168.2.79/samba/0upload/IMG_test_fix_exif_date.jpg"
                } catch (Exception e) {
                    handleException(e);
                }
                updateResult("download", localPath + " " + String.valueOf(result).toUpperCase());
            }
        }.start();///folder
    }

    protected void delele(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result = false;
                try {
                    result = SambaHelper.delete(mConfig, path);
                } catch (Exception e) {
                    handleException(e);
                }
                onRemoteFolderChange(curRemoteFolder, result);
                updateResult("DELETE", path + " " + String.valueOf(result).toUpperCase());
            }
        }).start();
    }

    protected void onRemoteFolderChange(String path, boolean result) {
        //TODO by child
    }

    protected void listWorkGroup() {
        new Thread() {
            @Override
            public void run() {
                String[] paths = null;
                try {
                    paths = SambaHelper.listWorkGroupPath();
                } catch (Exception e) {
                    handleException(e);
                }
                final String[] ps = paths;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onListWorkgroup(ps);
                    }
                });
                updateResult("listWorkGroup", Arrays.toString(paths));
            }
        }.start();
    }

    protected void listServer() {
        new Thread() {
            @Override
            public void run() {
                String[] paths = null;
                try {
                    paths = SambaHelper.listServerPath();
                } catch (Exception e) {
                    handleException(e);
                }
                final String[] ps = paths;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onListWorkgroup(ps);
                    }
                });
                updateResult("listWorkGroup", SambaUtil.strsToString(paths));
            }
        }.start();
    }

    protected void onListWorkgroup(String[] paths) {

    }

    protected void listRoot() {
        curRemoteFolder = SambaUtil.getSmbRootURL(mConfig);
    }

    protected void createFolder(final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean result = false;
                try {
                    result = SambaHelper.createFolder(mConfig, curRemoteFolder, name);
                } catch (Exception e) {
                    handleException(e);
                }
                onRemoteFolderChange(curRemoteFolder, result);
                updateResult("createFolder", SambaUtil.wrapSmbFileUrl(curRemoteFolder, name) + "       " + String.valueOf(result).toUpperCase());
            }
        }).start();
    }

    protected void updateResult(final String action, final String msg) {

    }

    /**
     * MalformedURLException
     * SmbException
     */
    protected void handleException(Exception e) {
        e.printStackTrace();
        updateResult("ERROR", e.getClass().getSimpleName() + ": \"" + e.getMessage() + "\"");
        if (e instanceof SmbAuthException) {
            updateResult("handleException", "AUTH ERROR!!! " + e.getMessage());
        }
    }
}
