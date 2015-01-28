/******************************************************************
 *
 *	CyberUtil for Java
 *
 *	Copyright (C) Satoshi Konno 2002-2003
 *
 *	File: FileUtil.java
 *
 *	Revision:
 *
 *	01/12/03
 *		- first revision.
 *
 ******************************************************************/

package org.cybergarage.util;

public final class StringUtil {
    public final static boolean hasData(String value) {
        if (value == null)
            return false;
        if (value.length() <= 0)
            return false;
        return true;
    }

    public final static int toInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            Debug.warning(e);
        }
        return 0;
    }

    public final static long toLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            Debug.warning(e);
        }
        return 0;
    }
}

