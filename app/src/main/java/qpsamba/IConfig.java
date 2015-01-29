package qpsamba;

import android.text.TextUtils;

/**
 * Created by ram on 15/1/19.
 */
public class IConfig {

    public static interface OnConfigListener {

        public void onConfig(IConfig config, Object obj);

    }

    private final static String HOST = "HOST";
    private final static String USER = "USER";
    private final static String PASSWORD = "PASSWORD";
    private final static String NICKNAME = "NICKNAME";

    public String host;
    public String user;
    public String password;
    public String nickName;

    public IConfig(String host, String user, String password, String nick) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.nickName = nick;
    }

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

//    public JSONObject toJson() {
//        JSONObject jo = new JSONObject();
//        try {
//            jo.put(HOST, host);
//            jo.put(USER, user);
//            jo.put(PASSWORD, password);
//            jo.put(NICKNAME, nickName);
//        } catch (Exception e) {
//        }
//        return jo;
//    }
}
