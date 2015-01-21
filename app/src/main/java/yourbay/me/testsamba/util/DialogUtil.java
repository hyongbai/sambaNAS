package yourbay.me.testsamba.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import yourbay.me.testsamba.samba.Config;
import yourbay.me.testsamba.samba.ConfigManual;
import yourbay.me.testsamba.samba.OnConfigListener;

/**
 * Created by ram on 15/1/20.
 */
public class DialogUtil {

    public final static EditText showInput(Context context, String hint, final DialogInterface.OnClickListener onClickListener) {
        int paddingTop = (int) (context.getResources().getDisplayMetrics().density * 50);
        int padding = (int) (context.getResources().getDisplayMetrics().density * 20);
        LinearLayout ll = new LinearLayout(context);
        ll.setPadding(padding, paddingTop, padding, padding);
        EditText et = new EditText(context);
        et.setHint(hint);
        et.setLayoutParams(new LinearLayout.LayoutParams(//
                LinearLayout.LayoutParams.MATCH_PARENT, //
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ll.addView(et);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder//
                .setView(ll)//
                .setPositiveButton(android.R.string.yes, onClickListener)//
                .setNegativeButton(android.R.string.cancel, onClickListener);
        builder.show();
        return et;
    }

    public final static void showConfirmDialog(Context context, String tips, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder//
                .setMessage(tips)//
                .setPositiveButton(android.R.string.yes, onClickListener)//
                .setNegativeButton(android.R.string.cancel, onClickListener);
        builder.show();
    }

    public final static void showInputAccoutDialog(final Context context, final OnConfigListener listener) {
        //-------------------
        int paddingTop = (int) (context.getResources().getDisplayMetrics().density * 50);
        int padding = (int) (context.getResources().getDisplayMetrics().density * 20);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(padding, paddingTop, padding, padding);

        //-------------------
        final EditText etHost = genEditText(context, "*Host");
        ll.addView(etHost);
        final EditText etUser = genEditText(context, "*User");
        ll.addView(etUser);
        final EditText etPwd = genEditText(context, "*Password");
        ll.addView(etPwd);
        final EditText etNick = genEditText(context, "NickName");
        ll.addView(etNick);


        //-------------------
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //-------------------
        DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != Dialog.BUTTON_POSITIVE) {
                    return;
                }
                //----------
                String host = etHost.getText().toString();
                if (TextUtils.isEmpty(host)) {
                    builder.show();
                    Toast.makeText(context, "Please input host", Toast.LENGTH_LONG).show();
                    return;
                }

                //----------
                String user = etUser.getText().toString();
                String password = etPwd.getText().toString();
                if ((TextUtils.isEmpty(user) || TextUtils.isEmpty(password)) && !TextUtils.isEmpty(user + password)) {
                    builder.show();
                    Toast.makeText(context, "User and password must be both inputed or both EMPTY", Toast.LENGTH_LONG).show();
                    return;
                }
                Config config = new ConfigManual(host, user, password, etNick.getText().toString());
                listener.onConfig(config, dialog);
            }
        };

        //-------------------
        builder//
                .setView(ll)//
                .setPositiveButton(android.R.string.yes, onClickListener)//
                .setNegativeButton(android.R.string.cancel, onClickListener);
        builder.show();
    }

    public final static EditText genEditText(Context context, String hint) {
        EditText et = new EditText(context);
        et.setHint(hint);
        et.setLayoutParams(new LinearLayout.LayoutParams(//
                LinearLayout.LayoutParams.MATCH_PARENT, //
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return et;
    }

}
