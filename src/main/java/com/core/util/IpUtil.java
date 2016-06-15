package com.core.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by sunpeng
 */
public class IpUtil {

    public static String getIp(HttpServletRequest request) {
        String ip;
        String forwarded = request.getHeader("X-Forwarded-For");
        String realIp = request.getHeader("X-Real-IP");
        String remoteAddr = request.getRemoteAddr();
        if (realIp == null) {
            if (forwarded == null) {
                ip = remoteAddr;
            } else {
                ip = remoteAddr + "/" + forwarded;
            }
        } else {
            if (realIp.equals(forwarded)) {
                ip = realIp;
            } else {
                ip = realIp + "/" + forwarded.replaceAll(", " + realIp, "");
            }
        }
        return ip;
    }
}
