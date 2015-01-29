package qpsamba;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by ram on 15/1/23.
 */
public class SambaUtil {


    public static final String getVideoMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (TextUtils.isEmpty(extension)) {
            return null;
        }
        extension = extension.toLowerCase();
        if (!SambaHelper.VIDEOS.contains(extension)) {
            return null;
        }
        return new StringBuilder("video/").append(extension).toString();
    }

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
        return path.substring(0, index);
    }

    public final static String autoRename(String name) {
        String nakedName = getNameWithoutSuffix(name);
        String suffix = name.replace(nakedName, "");
        return new StringBuilder(nakedName).append("-").append(System.currentTimeMillis()).append(suffix).toString();
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

    public final static String wrapSmbPath(String parent, String name) {
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

    public final static String getSmbRootURL(IConfig config) {
        String simpleUrl = new StringBuilder("smb://").append(config.host).append("/").toString();
        return getSmbFullURL(config, simpleUrl);
    }

    /**
     * According to  <a href="https://jcifs.samba.org/src/docs/api/jcifs/smb/SmbFile.html">SMB URL Examples</a> :<br>
     * smb://domain;username:password@server/{PATH}
     */
    public final static String getSmbFullURL(IConfig config, String url) {
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


    public final static String cropStreamSmbURL(String url) {
//        if (SambaHelper.DEBUG) {
//            Log.d(TAG, "cropStreamSmbURL " + url);
//        }
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        if (url.length() <= SambaHelper.CONTENT_EXPORT_URI.length()) {
            return url;
        }
        if (!url.startsWith(SambaHelper.CONTENT_EXPORT_URI)) {
            return url;
        }
        String filePaths = SambaHelper.SMB_URL_LAN + url.substring(SambaHelper.CONTENT_EXPORT_URI.length());
        int indexOf = filePaths.indexOf("&");
        if (indexOf != -1) {
            filePaths = filePaths.substring(0, indexOf);
        }
        return filePaths;
    }

    public final static String wrapStreamSmbURL(String url, String ip, int port) {
        try {
            url = url.substring(SambaHelper.SMB_URL_LAN.length());
            url = URLEncoder.encode(url, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder("http://")
                .append(ip)//
                .append(File.pathSeparator)//
                .append(port)//
                .append(SambaHelper.CONTENT_EXPORT_URI);//
        builder.append(url);
        return builder.toString();
    }


//    public static final String getLocalIpAddress() {
//        try {
//            Enumeration<NetworkInterface> infos = NetworkInterface
//                    .getNetworkInterfaces();
//            while (infos.hasMoreElements()) {
//                NetworkInterface niFace = infos.nextElement();
//                Enumeration<InetAddress> enumIpAddr = niFace.getInetAddresses();
//                while (enumIpAddr.hasMoreElements()) {
//                    InetAddress mInetAddress = enumIpAddr.nextElement();
//                    if (!mInetAddress.isLoopbackAddress()
//                            && InetAddressUtils.isIPv4Address(mInetAddress
//                            .getHostAddress())) {
//                        return mInetAddress.getHostAddress().toString();
//                    }
//                }
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

}
