package yourbay.me.testsamba.samba;

import android.text.TextUtils;

import jcifs.smb.SmbFile;

/**
 * Created by ram on 15/1/23.
 */
public class SambaUtil {

    public final static String getFileName(String path) {
        if (path == null) {
            return path;
        }
        if (!path.contains("/")) {
            return path;
        }
        int index = path.lastIndexOf("/");
        if (index < 0 || index + 1 >= path.length()) {
            return path;
        }
        return path.substring(index + 1);
    }

    public final static String getNameWithoutSuffix(String path) {
        path = getFileName(path);
        if (path == null) {
            return path;
        }
        if (!path.contains(".")) {
            return path;
        }
        int index = path.lastIndexOf(".");
        if (index <= 0 || index + 1 >= path.length()) {
            return null;
        }
        return path.substring(0, index + 1);
    }

    public final static String autoRename(String name) {
        String nakedName = getNameWithoutSuffix(name);
        String suffix = name.replace(nakedName, "");
        return new StringBuilder(nakedName).append("-").append(System.currentTimeMillis()).append(suffix).toString();
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
        StringBuilder builder = new StringBuilder(strs.length);
        for (String str : strs) {
            builder.append("\n");
            builder.append("[");
            builder.append(str);
            builder.append("]");
        }
        return builder.toString();
    }

    public final static String wrapPath(String parent, String name) {
        if (TextUtils.isEmpty(parent) || TextUtils.isEmpty(name)) {
            return null;
        }
        StringBuilder builder = new StringBuilder(parent);
        if (!parent.endsWith("/")) {
            builder.append("/");
        }
        if (name.endsWith("/")) {
            int index = name.lastIndexOf("/");
            name = name.substring(0, index - 1);
        }
        builder.append(name);
        return builder.toString();
    }

    public final static String getRootURL(IConfig config) {
        String simpleUrl = new StringBuilder("smb://").append(config.host).append("/").toString();
        return getFullURL(config, simpleUrl);
    }

    /**
     * According to  <a href="https://jcifs.samba.org/src/docs/api/jcifs/smb/SmbFile.html">SMB URL Examples</a> :<br>
     * smb://domain;username:password@server/{PATH}
     */
    public final static String getFullURL(IConfig config, String url) {
        if (config == null || TextUtils.isEmpty(url)) {
            return url;
        }
        if (TextUtils.isEmpty(config.user) || TextUtils.isEmpty(config.password)) {
            return url;
        }
        StringBuilder wrappedHost = new StringBuilder(";")//
                .append(config.user)//
                .append(":")//
                .append(config.password)//
                .append("@")//
                .append(config.host)//
                ;
        if (url.contains(wrappedHost.toString())) {
            return url;
        }
        //TODO add port or something else
        url = url.replace(config.host, wrappedHost.toString());
        return url;
    }
}
