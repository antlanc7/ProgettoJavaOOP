package com.lancarm.javaoop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Classe che contiene i metodi per il calcolo dei valori statistici
 * Astratta poiché contiene solo metodi statici, pertnato non è necessario istanziarla per utilizzarla
 */
public abstract class Stats {

    /**
     * Metodo ausiliario per l'arrotondamento dei numeri decimali
     * @param x numero da arrotondare
     * @param n numero di cifre dopo la virgola desiderate
     * @return  numero arrotondato a n cifre decimali
     */
    public static double round(double x, int n) {
        double factor = Math.pow(10, n);    //calcola il fattore moltiplicativo per spostare la virgola di n posti
        return Math.round(x * factor) / factor;     //arrotonda ad intero il numero con la virgola spostata, dopodiché la rimette a posto dividendo
    }

    /**
     * Metodo che conta gli elementi di una lista
     * @param list lista di valori da contare
     * @return  dimensione della lista
     */
    public static int count(List list) {
        return list.size();     //basta usare il metodo size() delle liste
    }

    /**
     * Metodo che somma tra loro gli elementi di una lista numerica
     * @param list lista di numeri da sommare
     * @return somma degli elementi
     */
    public static double sum(List<Number> list) {
        double s = 0;
        for (Number n : list) {
            s += n.doubleValue();       //sommo tutti i numeri n della lista nella variabile s
        }
        return s;
    }

    /**
     * Metodo che calcola la media degli elementi di una lista numerica
     * @param list lista di numeri su cui calcolare la media
     * @return media degli elementi
     */
    public static double avg(List<Number> list) {
        return round(sum(list) / count(list), 2);       // media = somma / dimensione
    }

    /**
     * Metodo che estrae il valore massimo tra gli elementi di una lista
     * @param list lista di numeri dai quali trovare il massimo
     * @return valore massimo della lista
     */
    public static double max(List<Number> list) {
        double max = list.get(0).doubleValue();     //classico algoritmo di ricerca max
        for (Number n : list) {
            double nval = n.doubleValue();
            if (nval > max) max = nval;
        }
        return max;
    }

    /**
     * Metodo che estrae il valore minimo tra gli elementi di una lista
     * @param list lista di numeri dai quali trovare il minimo
     * @return valore minimo della lista
     */
    public static double min(List<Number> list) {       //analogo a sopra
        double min = list.get(0).doubleValue();
        for (Number n : list) {
            double nval = n.doubleValue();
            if (nval < min) min = nval;
        }
        return min;
    }

    /**
     * Metodo che calcola la deviazione standard degli elementi di una lista
     * @param list lista di numeri dei quali calcolare la dev. std.
     * @return deviazione standard dei valori della lista
     */
    public static double std(List<Number> list) {   //devstd = radice della sommatoria degli (xi-xmedio)^2
        double avg = avg(list);
        double var = 0;
        for (Number n : list) {
            var += Math.pow(n.doubleValue() - avg, 2);
        }
        return round(Math.sqrt(var), 2);
    }

    /**
     * Metodo che conta le occorrenze di ogni elemento all'interno di una lista
     * @param list lista di valori
     * @return Map che ha come chiavi gli elementi della lista e come relativi valori il numero di occorrenze
     */
    public static Map<Object, Integer> uniqueElements(List list) {
        Map<Object, Integer> map = new HashMap<>();
        for (Object elem : list) {
            if (map.containsKey(elem)) {        //se la mappa contiene già la chiave (elemento già trovato precedentemente)
                map.replace(elem, map.get(elem) + 1);   //incrementa il valore, ovvero il contatore, di uno
            } else {
                map.put(elem, 1);       // altrimenti inserisce nella mappa la nuova chiave trovata con contatore inizializzato a uno
            }
        }
        return map;
    }

    /**
     * Metodo che utilizzando gli altri della classe, restituisce tutti valori statistici di un certo campo del dataset
     * @param fieldName nome del campo dal quale si è estratta la lista di valori
     * @param list lista dei valori del campo (eventualmente  già filtrata)
     * @return Map che ha come chiavi i nomi delle statistiche calcolabili sul campo e associati i rispettivi valori
     */
    public static Map<String, Object> getAllStats(String fieldName, List list) {
        Map<String, Object> map = new HashMap<>();
        map.put("field", fieldName);
        if (!list.isEmpty()) {
            if (list.get(0) instanceof Number) {        // calcola le statistiche numeriche
                map.put("avg", avg(list));
                map.put("min", min(list));
                map.put("max", max(list));
                map.put("std", std(list));
                map.put("sum", sum(list));
                map.put("count", count(list));
                return map;
            } else {        // calcola le statistiche non numeriche
                map.put("uniqueElements", uniqueElements(list));
                map.put("count", count(list));
            }
        }
        return map;
    }
}
