package yourbay.me.testsamba.samba.httpd;

import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import yourbay.me.testsamba.samba.SambaHelper;
import yourbay.me.testsamba.samba.SambaUtil;

/**
 * Created by ram on 15/1/13.
 */
public class NanoSmbStreamer extends NanoHTTPD implements IStreamer {
    public final static String TAG = SambaHelper.TAG;
    public final static int DEFAULT_SERVER_PORT = 8000;
    private int serverPort;

    public NanoSmbStreamer() {
        this(DEFAULT_SERVER_PORT);
    }

    public NanoSmbStreamer(int port) {
        super(null, port);
        this.serverPort = port;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();
        String uri = session.getUri();
        return respond(headers, uri);
    }

    private Response respond(Map<String, String> headers, String uri) {
        String smbUri = SambaUtil.cropStreamURL(uri);
        String mimeTypeForFile = SambaUtil.getVideoMimeType(uri);
        if (SambaHelper.DEBUG) {
            Log.d(SambaHelper.TAG, "respond headers=" + (Arrays.toString(headers.values().toArray())) + "    smbUri=" + smbUri + "  MIME=" + mimeTypeForFile);
        }
        Response response = null;
//        response = serveFile(headers, smbUri, mimeTypeForFile);
        try {
            SmbFile smbFile = new SmbFile(smbUri);
            InputStream copyStream = new BufferedInputStream(new SmbFileInputStream(smbFile));
            response = serveSmbFile(smbUri, headers, copyStream, smbFile, mimeTypeForFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (SambaHelper.DEBUG) {
            Log.d(SambaHelper.TAG, "respond response=" + (response != null));
        }
        return response != null ? response : createResponse(
                Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                "Error 404, file not found.");
    }


//    private Response serveFile(Map<String, String> header, String file, String mime) {
//        Response res;
//        try {
//            InputStream cInputStream;
////            cInputStream = new BufferedInputStream(//
////                    new SmbFile("smb://;ram:1234@192.168.2.79/samba/1420027389002.jpg")//
////                            .getInputStream());
////            cInputStream = new FileInputStream("/sdcard/DCIM/Camera/IMG_20150114_231632.jpg");
//            cInputStream = new FileInputStream(VideoActivity.URL_TEST_LOCAL_VIDEO_PATH);
////            mime = "image/jpeg";
//            mime = "video/mp4";
//            long fileLen = cInputStream.available();
//            String etag = Integer.toHexString((file + "" + fileLen).hashCode());
//            long startFrom = 0;
//            long endAt = -1;
//            String range = header.get("range");
//            if (range != null) {
//                if (range.startsWith("bytes=")) {
//                    range = range.substring("bytes=".length());
//                    int minus = range.indexOf('-');
//                    try {
//                        if (minus > 0) {
//                            startFrom = Long.parseLong(range
//                                    .substring(0, minus));
//                            endAt = Long.parseLong(range.substring(minus + 1));
//                        }
//                    } catch (NumberFormatException ignored) {
//                        ignored.printStackTrace();
//                    }
//                }
//            }
//
//            if (range != null && startFrom >= 0) {
//                if (startFrom >= fileLen) {
//                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE,
//                            MIME_PLAINTEXT, "");
//                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
//                    res.addHeader("ETag", etag);
//                } else {
//                    if (endAt < 0) {
//                        endAt = fileLen - 1;
//                    }
//                    long newLen = endAt - startFrom + 1;
//                    if (newLen < 0) {
//                        newLen = 0;
//                    }
//                    final long dataLen = newLen;
//                    cInputStream.skip(startFrom);
//                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime,
//                            cInputStream);
//                    res.addHeader("Content-Length", "" + dataLen);
//                    res.addHeader("Content-Range", "bytes " + startFrom + "-"
//                            + endAt + "/" + fileLen);
//                    res.addHeader("ETag", etag);
//                }
//            } else {
//                if (etag.equals(header.get("if-none-match")))
//                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
//                else {
//                    res = createResponse(Response.Status.OK, mime, cInputStream);
//                    res.addHeader("Content-Length", "" + fileLen);
//                    res.addHeader("ETag", etag);
//                }
//            }
//        } catch (Exception ioe) {
//            ioe.printStackTrace();
//            res = createResponse(Response.Status.FORBIDDEN,
//                    MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
//        }
//        return res;
//    }


//    private Response serveFile(Map<String, String> header, String filePath, String mime) {
//        Response res;
//        try {
//            if (filePath == null || !filePath.startsWith(SambaHelper.SMB_URL_LAN)) {
//                return null;
//            }
//            SmbFile file = new SmbFile(filePath);
//            if (SambaHelper.DEBUG) {
//                Log.d(SambaHelper.TAG, "serveFile headers=" + file.length());
//            }
//            InputStream cInputStream = file.getInputStream();
//            long fileLength = cInputStream.available();
//            String eTag = Integer.toHexString((filePath + "" + fileLength).hashCode());
//            long rangeStart = 0;
//            long rangeEnd = -1;
//            String range = header.get("range");
//            if (range != null) {
//                if (range.startsWith("bytes=")) {
//                    range = range.substring("bytes=".length());
//                    int minus = range.indexOf('-');
//                    try {
//                        if (minus > 0) {
//                            rangeStart = Long.parseLong(range.substring(0, minus));
//                            rangeEnd = Long.parseLong(range.substring(minus + 1));
//                        }
//                    } catch (NumberFormatException ignored) {
//                        ignored.printStackTrace();
//                    }
//                }
//            }
//
//            if (rangeStart >= fileLength || rangeStart < 0) {
//                rangeStart = 0;
//            }
//
//            if (rangeEnd < 0 || rangeEnd >= fileLength) {
//                rangeEnd = fileLength - 1;
//            }
//
//            long rangeLength = rangeEnd - rangeStart + 1;
//            if (rangeLength < 0) {
//                rangeLength = 0;
//            }
//            cInputStream.skip(rangeStart);
//            res = createResponse(Response.Status.PARTIAL_CONTENT, mime, cInputStream);
//            res.addHeader("Content-Length", "" + rangeLength);
//            res.addHeader("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
//            res.addHeader("ETag", eTag);
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//            res = createResponse(Response.Status.FORBIDDEN,
//                    MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
//        }
//        return res;
//    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, InputStream inputStream) {
        Response res = new Response(status, mimeType, inputStream);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, String message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    private Response createNonBufferedResponse(Response.Status status, String mimeType, InputStream message, Long len) {
//        Response res = new Response(status, mimeType, message);
        Response res = new NonBufferedResponse(status, mimeType, message, len);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    public static final String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> infos = NetworkInterface
                    .getNetworkInterfaces();
            while (infos.hasMoreElements()) {
                NetworkInterface niFace = infos.nextElement();
                Enumeration<InetAddress> enumIpAddr = niFace.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress mInetAddress = enumIpAddr.nextElement();
                    if (!mInetAddress.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(mInetAddress
                            .getHostAddress())) {
                        return mInetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void start() {
        try {
            super.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopStream() {
        closeAllConnections();
    }

    @Override
    public int getPort() {
        return serverPort;
    }

    @Override
    public String getIp() {
        return getLocalIpAddress();
    }

    private Response serveSmbFile(String smbFileUrl, Map<String, String> header, InputStream is, SmbFile smbFile, String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((smbFile.getName() + smbFile.getLastModified() + "" + smbFile.length()).hashCode());

            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // Change return code and add Content-Range header when skipping is requested
            long fileLen = smbFile.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE, LatestNanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }

                    final long dataLen = newLen;//
                    is.skip(startFrom);

                    res = createNonBufferedResponse(Response.Status.PARTIAL_CONTENT, mime, is, fileLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (etag.equals(header.get("if-none-match")))
                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
                else {
                    res = createNonBufferedResponse(Response.Status.OK, mime, is, fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = createResponse(Response.Status.FORBIDDEN, LatestNanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }

        return res;
    }

    public static class NonBufferedResponse extends NanoHTTPD.Response {
        private Long available;

        /**
         * Basic constructor.
         */
        public NonBufferedResponse(Status status, String mimeType, InputStream data, Long available) {
            super(status, mimeType, data);
            this.available = available;
        }


        @Override
        protected void sendAsFixedLength(OutputStream outputStream, PrintWriter pw) throws IOException {
            // Need to set Content-Length as range END - START
            long pending = (long) (data != null ? available : 0); // This is to support partial sends, see serveFile()
            String string = header.get("Content-Range"); // Such as bytes 203437551-205074073/205074074
            if(string != null) {
                if(string.startsWith("bytes ")) {
                    string = string.substring("bytes ".length());
                }
                Long start = Long.parseLong(string.split("-")[0]);
                pw.print("Content-Length: "+ (pending - start) +"\r\n");
            } else {
                pw.print("Content-Length: "+pending+"\r\n");
            }


            pw.print("\r\n");
            pw.flush();

            if (requestMethod != Method.HEAD && data != null) {
                int BUFFER_SIZE = 16 * 1024;
                byte[] buff = new byte[BUFFER_SIZE];
                while (pending > 0) {
                    // Note the ugly cast to int to support > 2gb files. If pending < BUFFER_SIZE we can safely cast anyway.
                    int read = data.read(buff, 0, ((pending > BUFFER_SIZE) ? BUFFER_SIZE : (int) pending));
                    if (read <= 0) {
                        break;
                    }
                    outputStream.write(buff, 0, read);

                    pending -= read;
                }
            }
        }

    }


}
