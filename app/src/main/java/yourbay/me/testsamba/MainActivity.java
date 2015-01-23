package yourbay.me.testsamba;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import jcifs.smb.SmbFile;
import yourbay.me.testsamba.samba.IConfig;
import yourbay.me.testsamba.samba.OnConfigListener;
import yourbay.me.testsamba.samba.SambaUtil;
import yourbay.me.testsamba.util.DialogUtil;
import yourbay.me.testsamba.util.IntentUtils;
import yourbay.me.testsamba.util.UriUtil;


public class MainActivity extends SambaActivity {
    //    private final static String TAG = "MainActivity";
    private Spinner fileSpinner;
    private Spinner wgSpinner;
    private TextView tvResult;
    private TextView tvSelectedFile;
    private TextView tvSelectedWG;
    private EditText editText;

    protected ArrayAdapter<String> fileAdapter;
    protected ArrayAdapter<String> wgAdapter;

    protected boolean isAutoSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_samba);
        tvResult = (TextView) findViewById(R.id.tv_result);
        tvSelectedFile = (TextView) findViewById(R.id.tv_selected_file);
        tvSelectedWG = (TextView) findViewById(R.id.tv_selected_workgroup);
        initSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_samba, menu);
        return true;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_change_host) {
//            showAccountDialog();
        } else if (id == R.id.action_smb_home) {
            listRoot();
        } else if (id == R.id.action_upload) {
            IntentUtils.pickupImages(this, REQUEST_CODE_CHOOSE_IMAGE);
        } else if (id == R.id.action_upload_video) {
            IntentUtils.pickupVideo(this, REQUEST_CODE_CHOOSE_IMAGE);
        } else if (id == R.id.action_play_video) {
            playVideo(curRemoteFile);
        } else if (id == R.id.action_download) {
            download(genLocalPath(), curRemoteFile);
        } else if (id == R.id.action_clear) {
            tvResult.setText("CLEARED");
        } else if (id == R.id.action_create_folder) {
            showCreateDialog();
        } else if (id == R.id.action_delete_file) {
            showDeleteDialog(curRemoteFile);
        } else if (id == R.id.action_delete_folder) {
            showDeleteDialog(curRemoteFolder);
        } else if (id == R.id.action_list_workgroup) {
            listWorkGroup();
//            listServer();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showCreateDialog() {
        editText = DialogUtil.showInput(this, "Please input folder name", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != DialogInterface.BUTTON_POSITIVE) {
                    return;
                }
                createFolder(editText.getText().toString());
            }
        });
    }

    private void showDeleteDialog(final String path) {
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(this, "INVALID FILE/FOLDER", Toast.LENGTH_LONG).show();
            return;
        }
        DialogUtil.showConfirmDialog(//
                this,//
                new StringBuilder("Confirm to Delete \n\"").append(path).append("\"?").toString(), //
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which != DialogInterface.BUTTON_POSITIVE) {
                            return;
                        }
                        delele(path);
                    }
                });
    }

    private void showAccountDialog() {
        DialogUtil.showInputAccoutDialog(//
                this,//
                new OnConfigListener() {
                    @Override
                    public void onConfig(IConfig config, Object obj) {
                        Log.d(TAG, "" + config);
                    }
                }
        );
    }

    /* * handler Spinner****/
    private void initSpinner() {
        //------------
        wgSpinner = (Spinner) findViewById(R.id.sp_workgroup);
        wgAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
        wgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wgSpinner.setAdapter(wgAdapter);
        wgSpinner.setOnItemSelectedListener(//
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String host = wgAdapter.getItem(position);
                        Log.d(TAG, "wgSpinner onItemSelected  " + host);
                        if (TextUtils.isEmpty(host)) {
                            return;
                        }
                        mConfig.updateHost(host);
                        Log.d(TAG, "wgSpinner onItemSelected  " + mConfig);
                        listRoot();
                        tvSelectedWG.setText("Current Host:" + mConfig.host);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        Log.d(TAG, "wgSpinner   onNothingSelected  curRemoteFolder=" + curRemoteFolder + "    isAutoSelected=" + isAutoSelected);
                    }
                }
        );
        listWorkGroup();

        //------------
        fileSpinner = (Spinner) findViewById(R.id.sp_file);
        fileAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
        fileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fileSpinner.setAdapter(fileAdapter);
        fileSpinner.setOnItemSelectedListener(//
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String name = fileAdapter.getItem(position);
                        onFileSelected(name);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        Log.d(TAG, "fileAdapter onNothingSelected  curRemoteFolder=" + curRemoteFolder + "    isAutoSelected=" + isAutoSelected);
                    }
                }
        );
//        curRemoteFolder = SambaUtil.getFullURL(mConfig, "/");
//        loadToSpinner(curRemoteFolder);
    }

    private void onFileSelected(String name) {
        Log.d(TAG, "onFileSelected  curRemoteFolder=" + curRemoteFolder + " name=\"" + name + "\"    isAutoSelected=" + isAutoSelected);
        if (isAutoSelected) {
            isAutoSelected = false;
            return;
        }
        if (name.equals(REMOTE_PARENT)) {
            if (curRemoteFolder != null) {
            }
            return;
        }
        SmbFile file = REMOTE_PATHS.get(name);
        tvSelectedFile.setText(file.getName());
        if (name.startsWith(REMOTE_FOLDER_PREFIX)) {
            curRemoteFolder = file.getPath();
            loadToSpinner(curRemoteFolder);
        } else if (name.startsWith(REMOTE_FILE_PREFIX)) {
            curRemoteFile = file.getPath();
            curRemoteFolder = file.getParent();
        }
        updateSelected();
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
                        fileAdapter.clear();
                        fileAdapter.addAll(REMOTE_PATHS.keySet());
                        fileAdapter.notifyDataSetChanged();
                        isAutoSelected = true;
                        fileSpinner.setSelection(0);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void listRoot() {
        super.listRoot();
        loadToSpinner(curRemoteFolder);
    }


    private final void playVideo(String path) {
        if (!path.toLowerCase().endsWith(".mp4")) {
            Toast.makeText(this, "NOT a MP4 file", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(VideoActivity.ACTION_KEY_URL, path);
        intent.setClass(this, VideoActivity.class);
        startActivity(intent);
    }


    private void updateSelected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvSelectedFile.setText("FOLDER:\n");
                tvSelectedFile.append("     ");
                tvSelectedFile.append(String.valueOf(curRemoteFolder));
                tvSelectedFile.append("\n");

                tvSelectedFile.append("FILE:\n");
                tvSelectedFile.append("     ");
                tvSelectedFile.append(curRemoteFile == null ? "NONE FILE SELECTED!" : curRemoteFile);
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

    @Override
    protected void updateResult(final String action, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String ACTION_STR = String.valueOf(action).toUpperCase();
                int MAX_LENGTH = 35;
                int length = ACTION_STR.length();
                final String DIVIDER = "=";
                while (length <= MAX_LENGTH) {
                    ACTION_STR = length % 2 == 0 ? ACTION_STR + DIVIDER : DIVIDER + ACTION_STR;
                    length++;
                }
                StringBuilder builder = new StringBuilder("\n");
                builder.append(ACTION_STR);
                builder.append("\n");
                builder.append(msg);
                tvResult.append(builder);
                Log.d(TAG, "updateResult    " + builder);
            }
        });
    }

    @Override
    protected void onRemoteFolderChange(String path, boolean result) {
        loadToSpinner(path);
    }

    @Override
    protected void onListWorkgroup(String[] paths) {
        if (paths == null) {
            return;
        }
        wgAdapter.clear();
        wgAdapter.addAll(paths);
        wgAdapter.notifyDataSetChanged();
        wgSpinner.setSelection(0);
    }

}
