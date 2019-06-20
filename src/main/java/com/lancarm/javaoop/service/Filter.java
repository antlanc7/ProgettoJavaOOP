package com.lancarm.javaoop.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe astratta che implementa i metodi per filtrare i campi
 */
public abstract class Filter {

    //Lista degli operatori di confronto implementati
    private static final List<String> operators = Arrays.asList("$eq", "$not", "$in", "$nin", "$gt", "$gte", "$lt", "$lte", "$bt");

    /**
     * Metodo che effettua il confronto il valore value e il riferimento ref, in base all'operatore passato
     *
     * @param value    valore da controllare
     * @param operator operatore di confronto
     * @param ref      valore di riferimento
     * @return boolean
     */
    public static boolean check(Object value, String operator, Object ref) {
        if (operators.contains(operator)) {  //se l'operatore è valido, cioè è contenuto nella lista degli operatori implementati
            if (value instanceof Number) {   //se il valore da controllare è numerico
                double numValue = ((Number) value).doubleValue();   //lo converto in double
                if (ref instanceof Number) {  //se il riferimento è un singolo numero
                    double numRef = ((Number) ref).doubleValue(); // lo converto in double
                    switch (operator) {     //effettua il confronto corrispondente all'operatore
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
                        default:    // quando l'operatore non è adeguato per i valori passati
                            String message = "Invalid operator: '" + operator + "' for given operands: '" + value + "' , '" + ref + "'";
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message); //serve a restituire un messaggio di errore in formato JSON al client
                    }
                } else if (ref instanceof List) {  //se il riferimento è una lista
                    List listRef = ((List) ref); //lo converto in lista generica
                    if (!listRef.isEmpty() && listRef.get(0) instanceof Number) {// se lista non è vuota e contiene numeri
                        // le seguenti istruzioni convertono la lista generica in una lista di double
                        List<Double> listRefNum = new ArrayList<>();
                        for (Object elem : listRef) {
                            listRefNum.add(((Number) elem).doubleValue());
                        } // fino a qui
                        switch (operator) { //come sopra vado a effettuare i controlli
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
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ref list is empty or contains invalid elements"); // se la lista è vuota o non contiene numeri
                } else
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ref object: '" + ref + "' not compatible with value: '" + value + "'");  // se il riferimento non è compatibile con il valore
            } else if (value instanceof String) {   // se il valore è una stringa
                String strValue = ((String) value); // lo converto
                if (ref instanceof String) {        // se il riferimento è una singola stringa
                    String strRef = ((String) ref); // converto anche lui
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
                    } else
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ref list is empty or contains invalid elements"); // se la lista è vuota o non contiene stringhe
                } else
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ref object: '" + ref + "' not compatible with value: '" + value + "'");  // se il riferimento non è compatibile con il valore
            } else
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid value object: '" + value + "'"); // se il valore da controllare non è valido
        } else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid operator: " + operator);  // se l'operatore non è valido
    }


    /**
     * Metodo che filtra una la lista dei valori del campo
     *
     * @param values   lista valori da controllare
     * @param operator operatore di confronto
     * @param ref      valore di riferimento
     * @return lista di interi contenente gli indici dei valori che soddisfano il filtro
     */
    public static List<Integer> select(List values, String operator, Object ref) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            if (check(values.get(i), operator, ref))        // per ogni elemento della lista, se soddisfa il controllo (check)
                indexes.add(i);         // aggiungo il suo indice alla lista
        }
        return indexes;         //restituisco la lista degli indici
    }

    /**
     * @return lista degli operatori validi per i filtri
     */
    public static List<String> getOperators() {
        return operators;
    }
}
