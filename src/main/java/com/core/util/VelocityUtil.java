package com.core.util;

import com.core.config.Config;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sunpeng
 */
public class VelocityUtil {

    public static String getWebRootPath() {
        return System.getProperty("webapp.root");
    }

    public static String getTargetFile(String categoryName, long articleSerial) {
        String path = getWebRootPath() + Constant.VELOCITY_TEMPLATE_OUT_PATH + File.separator + categoryName + File.separator + getThisYear() + File.separator;
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path + articleSerial + ".html";
    }

    public static String getTargetUrl(Config config, String categoryName, long articleSerial) {

        String host = config.getDomain();
        Pattern pattern = Pattern.compile("^http://");
        Matcher matcher = pattern.matcher(host);
        if (!matcher.find()) {
            host = "http://" + host;
        }

        return host + "/"+ categoryName + "/" + getThisYear() + "/" + articleSerial + ".html";
    }

    public static String getThisYear() {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }

    public static String getBaseFile(String fileName) {
        String _fileName = StringUtils.isBlank(fileName) ? "index.html" : fileName;
        return getWebRootPath() + Constant.VELOCITY_TEMPLATE_OUT_PATH + File.separator + _fileName;
    }

}
