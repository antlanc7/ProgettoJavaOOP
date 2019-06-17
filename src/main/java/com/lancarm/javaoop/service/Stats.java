package com.lancarm.javaoop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Stats {

    public static double round(double x, int n) {
        double factor = Math.pow(10, n);
        return Math.round(x * factor) / factor;
    }

    public static int count(List list) {
        return list.size();
    }

    public static double sum(List<Number> list) {
        double s = 0;
        for (Number n : list) {
            s += n.doubleValue();
        }
        return s;
    }

    public static double avg(List<Number> list) {
        return round(sum(list) / count(list), 2);
    }

    public static double max(List<Number> list) {
        double max = list.get(0).doubleValue();
        for (Number n : list) {
            double nval = n.doubleValue();
            if (nval > max) max = nval;
        }
        return max;
    }

    public static double min(List<Number> list) {
        double min = list.get(0).doubleValue();
        for (Number n : list) {
            double nval = n.doubleValue();
            if (nval < min) min = nval;
        }
        return min;
    }

    public static double std(List<Number> list) {
        double avg = avg(list);
        double var = 0;
        for (Number n : list) {
            var += Math.pow(n.doubleValue() - avg, 2);
        }
        return round(Math.sqrt(var), 2);
    }

    public static Map<Object, Integer> uniqueElements(List list) {
        Map<Object, Integer> map = new HashMap<>();
        for (Object elem : list) {
            if (map.containsKey(elem)) {
                map.replace(elem, map.get(elem) + 1);
            } else {
                map.put(elem, 1);
            }
        }
        return map;
    }

    public static Map<String, Object> getAllStats(String fieldName, List list) {
        Map<String, Object> map = new HashMap<>();
        map.put("field", fieldName);
        if (!list.isEmpty()) {
            if (list.get(0) instanceof Number) {
                map.put("avg", avg(list));
                map.put("min", min(list));
                map.put("max", max(list));
                map.put("std", std(list));
                map.put("sum", sum(list));
                map.put("count", count(list));
                return map;
            } else {
                map.put("uniqueElements", uniqueElements(list));
            }
        }
        return map;
    }
}
