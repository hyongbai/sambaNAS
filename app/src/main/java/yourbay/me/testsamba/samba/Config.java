package yourbay.me.testsamba.samba;

import android.text.TextUtils;

/**
 * Created by ram on 15/1/19.
 */
public class Config {
    public String host;
    public String user;
    public String password;
    public String nickName;

    public void updateHost(String newHost) {
        if (TextUtils.isEmpty(newHost)) {
            return;
        }
        while (newHost.startsWith("/")) {
            newHost = newHost.substring(1);
        }
        while (newHost.endsWith("/")) {
            newHost = newHost.substring(0, newHost.length() - 1);
        }
//        StringBuilder builder = new StringBuilder(newHost);
//        this.host = isWorkGroup ? builder.append("/").toString() : builder.toString();
        this.host = newHost;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        builder.append("\nhost:");
        builder.append(host);
        builder.append("\nuser:");
        builder.append(user);
        builder.append("\npassword:");
        builder.append(password);
        builder.append("\nnickName:");
        builder.append(nickName);
        return builder.toString();
    }
}
