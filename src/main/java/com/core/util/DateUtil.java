package com.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sunpeng
 */
public class DateUtil {

    public static final String FORMAT_PATTERN_MMDD = "MM-dd";
    public static final String FORMAT_PATTERN_YYMMDD = "yyyy-MM-dd";

    public static final String extDateFix(String extDateStr) throws ParseException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String result = null;

        if (StringUtils.isBlank(extDateStr)) return null;

        Date date = dateFormat.parse(extDateStr.replace("T", " "));

        result = dateFormat.format(date);

        return result;
    }

    public static final String getNameWithTime(String fullFileName){

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        return fullFileName.substring(0, fullFileName.lastIndexOf("."))
                + "_"
                + dateFormat.format(new Date())
                + fullFileName.substring(fullFileName.lastIndexOf("."), fullFileName.length());
    }

    public static final String formatDate(Date date, String pattern){
        if(date == null){
            return "";
        }
        return DateFormatUtils.format(date, pattern);
    }
}
