package com.lancarm.javaoop.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Filter {

    private static final List<String> operators = Arrays.asList("$eq", "$not", "$in", "$nin", "$gt", "$gte", "$lt", "$lte", "$bt");

    public static boolean check(Object value, String operator, Object ref) {
        if (operators.contains(operator)) {  //se l'operatore è valido
            if (value instanceof Number) {   //se il valore da controllare è numerico
                double numValue = ((Number) value).doubleValue();   //lo converto in double
                if (ref instanceof Number) {  //se il riferimento è un singolo numero
                    double numRef = ((Number) ref).doubleValue();
                    switch (operator) {
                        case "$eq":
                            return numValue == numRef;      //i break non servono perché tanto c'è il return che esce dal metodo
                        case "$not":
                            return numValue != numRef;
                        case "$gt":
                            return numValue > numRef;
                        case "$gte":
                            return numValue >= numRef;
                        case "$lt":
                            return numValue < numRef;
                        case "$lte":
                            return numValue <= numRef;
                        default:
                            String message = "Invalid operator: '" + operator + "' for given operands: '" + value + "' , '" + ref + "'";
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                    }
                } else if (ref instanceof List) {  //se il riferimento è una lista
                    List listRef = ((List) ref);
                    if (!listRef.isEmpty() && listRef.get(0) instanceof Number) {// se il riferimento è una lista non vuota di numeri
                        // converto la lista generica in una lista di double
                        List<Double> listRefNum = new ArrayList<>();
                        for (Object elem : listRef) {
                            listRefNum.add(((Number) elem).doubleValue());
                        }
                        switch (operator) {
                            case "$in":
                                return listRefNum.contains(numValue);
                            case "$nin":
                                return !listRefNum.contains(numValue);
                            case "$bt":
                                double vInf = listRefNum.get(0);
                                double vSup = listRefNum.get(1);
                                return numValue >= vInf && numValue <= vSup;
                            default:
                                String message = "Invalid operator: '" + operator + "' for given operands: '" + value + "' , '" + ref + "'";
                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                        }
                    } else
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ref object: '" + ref + "'");
                } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ref object: '" + ref + "'");
            } else if (value instanceof String) {
                String strValue = ((String) value);
                if (ref instanceof String) {
                    String strRef = ((String) ref);
                    switch (operator) {
                        case "$eq":
                            return strValue.equals(strRef);      //i break non servono perché tanto c'è il return che esce dal metodo
                        case "$not":
                            return !strValue.equals(strRef);
                        default:
                            String message = "Invalid operator: '" + operator + "' for given operands: '" + value + "' , '" + ref + "'";
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                    }
                } else if (ref instanceof List) {  //se il riferimento è una lista
                    List listRef = ((List) ref);
                    if (!listRef.isEmpty() && listRef.get(0) instanceof String) {// se il riferimento è una lista non vuota di stringhe
                        // converto la lista generica in una lista di String
                        List<String> listRefStr = new ArrayList<>();
                        for (Object elem : listRef) {
                            listRefStr.add((String) elem);
                        }
                        switch (operator) {
                            case "$in":
                                return listRefStr.contains(strValue);
                            case "$nin":
                                return !listRefStr.contains(strValue);
                            default:
                                String message = "Invalid operator: '" + operator + "' for given operands: '" + value + "' , '" + ref + "'";
                                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                        }
                    } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ref object: '" + ref + "'");
                } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ref object: '" + ref + "'");
            } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid value object: '" + value + "'");
        } else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid operator: " + operator);
    }


    public static List<Integer> select(List values, String operator, Object ref) {
        List<Integer> indexes = new ArrayList<>();
        for (int i=0; i<values.size(); i++){
            if (check(values.get(i), operator, ref))
                indexes.add(i);
        }
        return indexes;
    }

}
