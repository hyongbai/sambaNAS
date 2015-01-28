package yourbay.me.testsamba.samba.httpd;

import android.util.Log;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.http.HTTPResponse;
import org.cybergarage.http.HTTPServerList;
import org.cybergarage.http.HTTPStatus;
import org.cybergarage.net.HostInterface;
import org.cybergarage.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import yourbay.me.testsamba.samba.SambaHelper;
import yourbay.me.testsamba.samba.SambaUtil;

/**
 * Created by ram on 15/1/28.
 */
public class CyberSmbStreamer extends Thread implements org.cybergarage.http.HTTPRequestListener, IStreamer {

    public static final String TAG = SambaHelper.TAG;
    private HTTPServerList httpServerList = new HTTPServerList();
    private static String bindIP = null;
    private static int bindPort = 2222;
    private static final int MAX_ADDRESSING_COUNT = 1000;


    @Override
    public String getIp() {
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

    @Override
    public int getPort() {
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
        int port = getPort();
        HTTPServerList hsl = getHttpServerList();
        while (hsl.open(port) == false) {
            retryCnt++;
            if (MAX_ADDRESSING_COUNT < retryCnt) {
                return;
            }
            setHTTPPort(port + 1);
            port = getPort();
        }
        hsl.addRequestListener(this);
        hsl.start();

        String ip = hsl.getHTTPServer(0).getBindAddress();
        if (ip != null && ip.contains("/")) {
            ip = ip.replaceAll("/", "");
        }
//        ip = "127.0.0.1";
        setBindIP(ip);
        if (SambaHelper.DEBUG) {
            Log.d(TAG, "SERVER:  ip=" + ip + "  bindIP=" + bindIP + "   port=" + port + "   bindPort=" + bindPort);
        }
    }

    @Override
    public void httpRequestRecieved(HTTPRequest httpReq) {
        String uri = httpReq.getURI();
        String filePaths = SambaUtil.cropStreamURL(uri);
        if (SambaHelper.DEBUG) {
            Log.d(TAG, "httpRequestRecieved uri=" + uri + "   filePaths=" + filePaths);
        }
        if (!uri.startsWith(SambaHelper.CONTENT_EXPORT_URI) || filePaths == null || !filePaths.startsWith(SambaHelper.SMB_URL_LAN)) {
            httpReq.returnBadRequest();
            return;
        }
        try {
            SmbFile file = new SmbFile(filePaths);
            long contentLen = file.length();
            String contentType = SambaUtil.getVideoMimeType(filePaths);
            InputStream contentIn = file.getInputStream();
            if (SambaHelper.DEBUG) {
                Log.d(TAG, "httpRequestRecieved contentLen=" + contentLen + "  contentType=" + contentType + "   name=" + file.getName() + "     ins=" + (contentIn != null));
            }
            if (contentLen <= 0 || StringUtil.hasData(contentType) || contentIn == null) {
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


    @Override
    public void stopStream() {
        HTTPServerList httpServerList = getHttpServerList();
        httpServerList.stop();
        httpServerList.close();
        httpServerList.clear();
        interrupt();
    }

}
