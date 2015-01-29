package qpsamba.httpd;

/**
 * Created by ram on 15/1/28.
 */
public interface IStreamer {

    public void start();

    public void stopStream();

    public int getPort();

    public String getIp();
}
