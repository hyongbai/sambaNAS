package yourbay.me.testsamba;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import yourbay.me.testsamba.samba.SambaUtil;


public class SambaActivity extends ActionBarActivity implements View.OnClickListener {

    private final int REQUEST_CODE_CHOOSE_IMAGE = 1234;
    private TextView tvResult;
    String currentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_samba);
        findViewById(R.id.btn_connect).setOnClickListener(this);
        findViewById(R.id.btn_upload).setOnClickListener(this);
        findViewById(R.id.btn_clear).setOnClickListener(this);
        tvResult = (TextView) findViewById(R.id.tv_result);
        currentPath = "/samba/0upload/IMG_test_fix_exif_date.jpg/";
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
            currentPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + System.currentTimeMillis();
            download(currentPath);
        } else if (id == R.id.btn_connect) {
            list();
        } else if (id == R.id.btn_clear) {
            tvResult.setText("CLEARED");
        }
    }

    private final void upload(final String path) {
        new Thread() {
            @Override
            public void run() {
                boolean result = SambaUtil.upload(path, "/samba/0upload/");
                updateResult("upload", path + " " + String.valueOf(result).toUpperCase());
            }
        }.start();///folder
    }

    private final void download(final String path) {
        new Thread() {
            @Override
            public void run() {
                boolean result = SambaUtil.download(path, "/samba/0upload/IMG_test_fix_exif_date.jpg");
                updateResult("upload", path + " " + String.valueOf(result).toUpperCase());
            }
        }.start();///folder
    }

    private final void list() {
        new Thread() {
            @Override
            public void run() {
                String result = SambaUtil.listFiles("/samba/0upload/");
                updateResult("list", "Files:\n" + result);
            }
        }.start();///folder
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

}
