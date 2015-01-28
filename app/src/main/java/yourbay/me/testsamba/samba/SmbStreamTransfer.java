package yourbay.me.testsamba.samba;

import android.util.Log;
import android.webkit.MimeTypeMap;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPResponse;
import org.cybergarage.http.HTTPServerList;
import org.cybergarage.http.HTTPStatus;

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
        bindIP = hsl.getHTTPServer(0).getBindAddress();
        Log.d(TAG, "SERVER = " + bindIP + ":" + port);
    }

    @Override
    public void httpRequestRecieved(HTTPRequest httpReq) {
        String uri = httpReq.getURI();
        if (uri.startsWith(CONTENT_EXPORT_URI) == false) {
            httpReq.returnBadRequest();
            return;
        }
        String filePaths = cropURL(uri);
        try {
            SmbFile file = new SmbFile(filePaths);
            long contentLen = file.length();
            String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(filePaths));
            InputStream contentIn = file.getInputStream();
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
            return;
        } catch (SmbException e) {
            httpReq.returnBadRequest();
            return;
        } catch (IOException e) {
            httpReq.returnBadRequest();
            return;
        }
    }


    public final static String cropURL(String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        String filePaths = SambaHelper.SMB_URL_LAN + url.substring(CONTENT_EXPORT_URI.length());
        int indexOf = filePaths.indexOf("&");
        if (indexOf != -1) {
            filePaths = filePaths.substring(0, indexOf);
        }
        return filePaths;
    }

    public final static String wrapURL(String url) {
        StringBuilder builder = new StringBuilder("http://").append(bindIP).append(":").append(bindPort).append(CONTENT_EXPORT_URI);
        url = url.substring(SambaHelper.SMB_URL_LAN.length());
        builder.append(url);
        try {
            url = URLEncoder.encode(url, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    public void stopTransfer() {
        HTTPServerList httpServerList = getHttpServerList();
        httpServerList.stop();
        httpServerList.close();
        httpServerList.clear();
        interrupt();
    }

}
