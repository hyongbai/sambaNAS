package yourbay.me.testsamba;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.LinearLayout;

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

}
