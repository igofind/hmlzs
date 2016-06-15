/**
 * READONLY
 */
package com.core.util;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Maintained by: 4what
 *
 * @author xhchen
 */
public abstract class JsonUtil {
    private static JsonFactory factory = new JsonFactory();

    private static ObjectMapper mapper = new ObjectMapper();

    public static <T> T fromJson(String s, Class<T> type) {
        try {
            return mapper.readValue(s, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int getInt(String s, String key) {
        JsonParser jp = match(s, key);
        if (jp != null) {
            try {
                return Integer.parseInt(jp.getText());
            } catch (Exception e) {
            } finally {
                try {
                    jp.close();
                } catch (Exception e) {
                }
            }
        }
        return -100;
    }

    public static String getText(String s, String key) {
        JsonParser jp = match(s, key);
        if (jp != null) {
            try {
                return jp.getText();
            } catch (Exception e) {
            } finally {
                try {
                    jp.close();
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    public static JsonParser match(String s, String key) {
        if (s == null || key == null || "".equals(s) || "".equals(key)) {
            return null;
        }
        try {
            JsonParser jp = factory.createJsonParser(s);
            JsonToken t;
            while ((t = jp.nextToken()) != null) {
                if (!JsonToken.FIELD_NAME.equals(t) || !jp.getCurrentName().equals(key)) {
                    continue;
                }
                jp.nextToken();
                return jp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJson(Object obj) {
        try {

            return mapper.writerWithView(System.class).writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
