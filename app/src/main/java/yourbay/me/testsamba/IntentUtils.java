package yourbay.me.testsamba;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author ram
 * @project BaseAndroid
 * @time Jul 29, 2014
 */
public class IntentUtils {

    /**
     * Using third part apps to pickup images
     *
     * @param activity
     * @param requestCode
     * @return
     */
    public final static boolean pickupImages(Activity activity, int requestCode) {
        if (activity == null) {
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Uri uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        // intent.setDataAndType(uri, "image/*");
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        boolean available = isIntentAvailable(activity, intent);
        if (available) {
            activity.startActivityForResult(intent, requestCode);
        }
        return available;
    }

    public final static boolean pickupVideo(Activity activity, int requestCode) {
        if (activity == null) {
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Uri uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        // intent.setDataAndType(uri, "image/*");
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        boolean available = isIntentAvailable(activity, intent);
        if (available) {
            activity.startActivityForResult(intent, requestCode);
        }
        return available;
    }

    /**
     * <pre>
     * If u r using intent to open other app(s) ,such as image/video viewer.
     * This method will tell u if this kind of app exits or not.
     * </pre>
     */
    public final static boolean isIntentAvailable(Context context, Intent intent) {
        if (context == null || intent == null) {
            return false;
        }
        try {
            return listResolves(context, intent) != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static List<ResolveInfo> listResolves(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
    }

    public final static List<ResolveInfo> listSendResolves(Context context, String type) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND).setType(type == null ? "*/*" : type);
        return listResolves(context, shareIntent);
    }

    public static final boolean startActivity(Context context, Intent intent, boolean finish) {
        if (context == null || intent == null) {
            return false;
        }

        try {
            if ((context instanceof Activity)) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (!finish) {
            return true;
        }

        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            activity.finish();
            return true;
        }
        return false;
    }

    public static String intentToString(Intent intent) {
        // TODO
        return null;
    }

    public static String bundleToString(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        Set<String> set = bundle.keySet();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                Object object = bundle.get(key);
                if (object == null) {
                    continue;
                }
                builder.append("[" + key + "			" + object + "]");
                builder.append("\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }


}
