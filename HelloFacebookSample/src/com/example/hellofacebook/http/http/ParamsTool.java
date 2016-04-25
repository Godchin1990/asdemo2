package com.example.hellofacebook.http.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ParamsTool {
    /**
     * 把数据源HashMap转换成json
     *
     * @param map
     */
    public static String hashMapToJson(HashMap map) {
        String string = "{";
        for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry e = (Map.Entry) it.next();
            string += "'" + e.getKey() + "':";
            string += "'" + e.getValue() + "',";
        }
        string = string.substring(0, string.lastIndexOf(","));
        string += "}";
        return string;
    }

    /**
     * 得到参数列表字符串
     *
     * @param method      请求类型 get or  post
     * @param paramValues 参数map对象
     * @return 参数列表字符串
     */
    public static String getParams(String method, Map<String, String> paramValues) {
        String params = "";
        Set<String> key = paramValues.keySet();
        String beginLetter = "";
        if (method.equalsIgnoreCase("get")) {
            beginLetter = "?";
        }

        for (Iterator<String> it = key.iterator(); it.hasNext(); ) {
            String s = (String) it.next();
            if (params.equals("")) {
                params += beginLetter + s + "=" + paramValues.get(s);
            } else {
                params += "&" + s + "=" + paramValues.get(s);
            }
        }
        return params;
    }


    /**
     * 按照key排序得到参数列表字符串
     *
     * @param method      请求类型 get or  post
     * @param paramValues 参数map对象
     * @return 参数列表字符串
     */
    public static String getParamsOrderByKey(String method, Map<String, String> paramValues) {
        String params = "";
        Set<String> key = paramValues.keySet();
        String beginLetter = "";
        if (method.equalsIgnoreCase("get")) {
            beginLetter = "?";
        }
        List<String> paramNames = new ArrayList<String>(paramValues.size());
        paramNames.addAll(paramValues.keySet());
        Collections.sort(paramNames);
        for (String paramName : paramNames) {

            if (params.equals("")) {
                params += beginLetter + paramName + "=" + paramValues.get(paramName);
            } else {
                params += "&" + paramName + "=" + paramValues.get(paramName);
            }
        }

        return params;
    }

    private static String getFileName(String path) {
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }
}