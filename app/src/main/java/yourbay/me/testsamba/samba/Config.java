package yourbay.me.testsamba.samba;

/**
 * Created by ram on 15/1/19.
 */
public class Config {
    public String host;
    public String user;
    public String password;
    public String nickName;

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
