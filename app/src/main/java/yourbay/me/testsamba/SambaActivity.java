package yourbay.me.testsamba;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import yourbay.me.testsamba.samba.Config;
import yourbay.me.testsamba.samba.ConfigDesk79;
import yourbay.me.testsamba.samba.SambaUtil;


public class SambaActivity extends ActionBarActivity implements View.OnClickListener {

    //    private final static String TAG = "SambaActivity";
    private final static String TAG = SambaUtil.TAG;
    private final static String LOCAL_FOLDER_PATH = "/test/samba";
    private final static String REMOTE_PARENT = "   ... ";
    private final static String REMOTE_FOLDER_PREFIX = " > ";
    private final static String REMOTE_FILE_PREFIX = "    ";
    private final static int REQUEST_CODE_CHOOSE_IMAGE = 1234;
    private SmbFile EMPTY_REMOTE_FILE;
    private TextView tvResult;
    private TextView tvCurrent;
    private String curRemoteFolder;
    private String curRemoteFile;
    private Map<String, SmbFile> REMOTE_PATHS = new LinkedHashMap<>();
    private ArrayAdapter<String> adapter;
    private Config mConfig;
    private boolean isAutoSelected = false;
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfig = new ConfigDesk79();
        try {
            EMPTY_REMOTE_FILE = new SmbFile("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_samba);
        findViewById(R.id.btn_list).setOnClickListener(this);
        findViewById(R.id.btn_upload).setOnClickListener(this);
        findViewById(R.id.btn_down).setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);
        tvResult = (TextView) findViewById(R.id.tv_result);
        tvCurrent = (TextView) findViewById(R.id.tv_selected);
        curRemoteFolder = "/samba/0upload/IMG_test_fix_exif_date.jpg/";
        initSpinner();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_samba, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_smb_home) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode != REQUEST_CODE_CHOOSE_IMAGE) {
            return;
        }
        try {
            Uri selectedImage = data.getData();
            String path = UriUtil.getImagePath(this, selectedImage);
            upload(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_upload) {
            IntentUtils.pickupImages(this, REQUEST_CODE_CHOOSE_IMAGE);
        } else if (id == R.id.btn_down) {
            download(genLocalPath(), curRemoteFile);
        } else if (id == R.id.btn_list) {
            list();
        } else if (id == R.id.btn_clear) {
            tvResult.setText("CLEARED");
        }
    }

    private void initSpinner() {
        mSpinner = (Spinner) findViewById(R.id.spinner);
        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        mSpinner.setOnItemSelectedListener(//
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String name = adapter.getItem(position);
                        onFileSelected(name);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        Log.d(TAG, "onNothingSelected  curRemoteFolder=" + curRemoteFolder + "    isAutoSelected=" + isAutoSelected);
                    }
                }

        );

        loadToSpinner(SambaUtil.getRemotePath("/")

        );
    }

    private void onFileSelected(String name) {
        Log.d(TAG, "onFileSelected  curRemoteFolder=" + curRemoteFolder + " name=\"" + name + "\"    isAutoSelected=" + isAutoSelected);
        if (isAutoSelected) {
            tvCurrent.setText("/");
            isAutoSelected = false;
            return;
        }
//                boolean isRoot = curRemoteFolder.replace("smb://" + mConfig.host + "/", "").trim().split("/").length <= 1;
        if (name.equals(REMOTE_PARENT)) {
            if (curRemoteFolder != null) {
            }
            return;
        }
        SmbFile file = REMOTE_PATHS.get(name);
        tvCurrent.setText(file.getName());
        if (name.startsWith(REMOTE_FOLDER_PREFIX)) {
            curRemoteFolder = file.getPath();
            loadToSpinner(curRemoteFolder);
        } else if (name.startsWith(REMOTE_FILE_PREFIX)) {
            curRemoteFile = file.getPath();
            curRemoteFolder = file.getParent();
        }
    }

    private void loadToSpinner(final String path) {
        Log.d(TAG, "loadToSpinner    " + path);
        new Thread(new Runnable() {
            @Override
            public void run() {
                listAndPrepare(path);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        adapter.addAll(REMOTE_PATHS.keySet());
                        adapter.notifyDataSetChanged();
                        isAutoSelected = true;
                        mSpinner.setSelection(0);
                    }
                });
            }
        }).start();
    }

    private void listAndPrepare(String path) {
        List<SmbFile> FILES = SambaUtil.listFiles(mConfig, path);
        Map<String, SmbFile> MAP = listToMap(FILES);
        prepareCurrentMap(MAP);
    }

    private void prepareCurrentMap(Map<String, SmbFile> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        synchronized (REMOTE_PATHS) {
            REMOTE_PATHS.clear();
            REMOTE_PATHS.put(REMOTE_PARENT, EMPTY_REMOTE_FILE);
            REMOTE_PATHS.putAll(map);
        }
    }

    private Map<String, SmbFile> listToMap(List<SmbFile> files) {
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

    /**
     * *************ACTIONS***************
     */
    private final void upload(final String path) {
        new Thread() {
            @Override
            public void run() {
                boolean result = SambaUtil.upload(mConfig, path, "/samba/0upload/");
                updateResult("upload", path + " " + String.valueOf(result).toUpperCase());
            }
        }.start();///folder
    }

    private final void download(final String localPath, final String remotePath) {
        Log.d(TAG, "download      " + localPath);
        new Thread() {
            @Override
            public void run() {
                boolean result = SambaUtil.download(mConfig, localPath, remotePath);//"smb://192.168.2.79/samba/0upload/IMG_test_fix_exif_date.jpg"
                updateResult("download", localPath + " " + String.valueOf(result).toUpperCase());
            }
        }.start();///folder
    }

    private final void list() {
        curRemoteFolder = SambaUtil.getRemotePath("/");
        loadToSpinner(curRemoteFolder);
    }


    private void updateResult(final String action, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvResult.append("\n");
                tvResult.append("=========" + String.valueOf(action).toUpperCase() + "========");
                tvResult.append("\n");
                tvResult.append(msg);
            }
        });
    }

    private final String genLocalPath() {
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + LOCAL_FOLDER_PATH);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String name = SambaUtil.getFileName(curRemoteFile);
        if (name == null) {
            name = String.valueOf(System.currentTimeMillis());
        }
        File f = new File(new StringBuilder(folder.getAbsolutePath()).append("/").append(name).toString());
        return f.getAbsolutePath();
    }

}
