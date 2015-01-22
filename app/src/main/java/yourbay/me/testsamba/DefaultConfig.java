package yourbay.me.testsamba;

import yourbay.me.testsamba.samba.IConfig;

/**
 * Created by ram on 15/1/19.
 */
public class DefaultConfig extends IConfig {
    public DefaultConfig() {
        user = "ram";
        password = "1234";
        host = "192.168.2.79";
        nickName = "RAM-ELEM";
    }
}