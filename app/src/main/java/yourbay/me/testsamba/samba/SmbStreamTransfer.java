package yourbay.me.testsamba.samba;

import android.util.Log;
import android.webkit.MimeTypeMap;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPResponse;
import org.cybergarage.http.HTTPServerList;
import org.cybergarage.http.HTTPStatus;
import org.cybergarage.net.HostInterface;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by ram on 15/1/28.
 */
public class SmbStreamTransfer extends Thread implements org.cybergarage.http.HTTPRequestListener {

    public static final String CONTENT_EXPORT_URI = "/smb=";
    public static final String TAG = "SmbStreamTransfer";
    private HTTPServerList httpServerList = new HTTPServerList();
    private static String bindIP = null;
    private static int bindPort = 2222;
    private static final int MAX_ADDRESSING_COUNT = 1000;


    public String getBindIP() {
        return bindIP;
    }

    public void setBindIP(String bindIP) {
        this.bindIP = bindIP;
    }

    public HTTPServerList getHttpServerList() {
        return httpServerList;
    }

    public void setHttpServerList(HTTPServerList httpServerList) {
        this.httpServerList = httpServerList;
    }

    public int getHTTPPort() {
        return bindPort;
    }

    public void setHTTPPort(int hTTPPort) {
        bindPort = hTTPPort;
    }

    public void run() {
//        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
//            throw new UnsupportedOperationException("You CAN'T start SmbStreamTransfer in UI thread");
//        }
        HostInterface.USE_ONLY_IPV4_ADDR = true;
        int retryCnt = 0;
        int port = getHTTPPort();
        HTTPServerList hsl = getHttpServerList();
        while (hsl.open(port) == false) {
            retryCnt++;
            if (MAX_ADDRESSING_COUNT < retryCnt) {
                return;
            }
            setHTTPPort(port + 1);
            port = getHTTPPort();
        }
        hsl.addRequestListener(this);
        hsl.start();

        String ip = hsl.getHTTPServer(0).getBindAddress();
        if (ip != null && ip.contains("/")) {
            ip = ip.replaceAll("/", "");
        }
        setBindIP(ip);
        if (SambaHelper.DEBUG) {
            Log.d(TAG, "SERVER:  ip=" + ip + "  bindIP=" + bindIP + "   port=" + port + "   bindPort=" + bindPort);
        }
    }

    @Override
    public void httpRequestRecieved(HTTPRequest httpReq) {
        String uri = httpReq.getURI();
        String filePaths = cropURL(uri);
        if (SambaHelper.DEBUG) {
            Log.d(TAG, "httpRequestRecieved uri=" + uri + "   filePaths=" + filePaths);
        }
        if (filePaths == null || !filePaths.startsWith(SambaHelper.SMB_URL_LAN)) {
            if (SambaHelper.DEBUG) {
                Log.d(TAG, "httpRequestRecieved NOT AN SAMBA URL");
            }
            httpReq.returnBadRequest();
            return;
        }
        try {
            SmbFile file = new SmbFile(filePaths);
            long contentLen = file.length();
            String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(filePaths));
            InputStream contentIn = file.getInputStream();
            if (SambaHelper.DEBUG) {
                Log.d(TAG, "httpRequestRecieved contentLen=" + contentLen + "  contentType=" + contentType + "   name=" + file.getName() + "     ins=" + (contentIn != null));
            }
            if (contentLen <= 0 || contentType.length() <= 0
                    || contentIn == null) {
                httpReq.returnBadRequest();
                return;
            }
            HTTPResponse httpRes = new HTTPResponse();
            httpRes.setContentType(contentType);
            httpRes.setStatusCode(HTTPStatus.OK);
            httpRes.setContentLength(contentLen);
            httpRes.setContentInputStream(contentIn);
            httpReq.post(httpRes);
            contentIn.close();
        } catch (MalformedURLException e) {
            httpReq.returnBadRequest();
            printException(e);
            return;
        } catch (SmbException e) {
            httpReq.returnBadRequest();
            printException(e);
            return;
        } catch (IOException e) {
            httpReq.returnBadRequest();
            printException(e);
            return;
        }
    }

    private void printException(Exception e) {
        e.printStackTrace();
        if (SambaHelper.DEBUG) {
            Log.d(TAG, "printException " + e.getMessage());
        }
    }

    public final static String cropURL(String url) {
//        if (SambaHelper.DEBUG) {
//            Log.d(TAG, "cropURL " + url);
//        }
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        if (url.length() <= CONTENT_EXPORT_URI.length()) {
            return url;
        }
        String filePaths = SambaHelper.SMB_URL_LAN + url.substring(CONTENT_EXPORT_URI.length());
        int indexOf = filePaths.indexOf("&");
        if (indexOf != -1) {
            filePaths = filePaths.substring(0, indexOf);
        }
        return filePaths;
    }

    public final static String wrapURL(String url) {
        try {
            url = url.substring(SambaHelper.SMB_URL_LAN.length());
            url = URLEncoder.encode(url, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder builder = new StringBuilder("http://").append(bindIP).append(File.pathSeparator).append(bindPort).append(CONTENT_EXPORT_URI);
        builder.append(url);
        if (SambaHelper.DEBUG) {
            Log.d(TAG, "wrapURL " + url + "  " + builder.toString());
        }
        return builder.toString();
    }

    public void stopTransfer() {
        HTTPServerList httpServerList = getHttpServerList();
        httpServerList.stop();
        httpServerList.close();
        httpServerList.clear();
        interrupt();
    }

}
