package com.shinkai.lib;

public class RegresInHelpers {
    public static int getExpTotalPages(String perPage, int expTotal) {
        int perPageInt = Integer.parseInt(perPage);
        if (expTotal % perPageInt == 0) {
            return expTotal / perPageInt;
        } else {
            return (int) Math.ceil((double) expTotal / perPageInt);
        }
    }

    public static int stringToInt(String value) {
        return Integer.parseInt(value);
    }
}
