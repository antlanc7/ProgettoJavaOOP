package com.lancarm.javaoop.controller;

import com.lancarm.javaoop.model.StrutturaAlberghiera;
import com.lancarm.javaoop.service.StrAlbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller Spring che gestisce le richieste dell'utente (client)
 */
@RestController
public class StrAlbController {

    //creo una variabile della classe Service
    private StrAlbService service;

    /**
     * Costruttore che con l'annotazione @Autowired viene lanciato automaticamente all'avvio da Spring e esegue il collegamento al Service
     *
     * @param service riferimento all'istanza del service inizializzata da Spring
     */
    @Autowired //stiamo dichiarando che il controllore dipende da service, ovvero stiamo iniettando una dipendenza
    public StrAlbController(StrAlbService service) {
        this.service = service;
    }

    //metodi per la comunicazione con il client che gestiscono le richieste GET e POST

    /**
     * Metodo che gestisce la richiesta GET alla rotta "/data", restituisce l'intero dataset
     *
     * @return lista di tutti gli oggetti del dataset
     */
    //la rotta è la parte dell'url dopo dominio:porta es.: localhost:8080/data
    @GetMapping("/data")
    public List getAllData() {
        return service.getAllData();
    }

    /**
     * Metodo che gestisce la richiesta GET alla rotta "/data/{id}", restituisce il record del dataset corrispondente a {id}
     * {id} è pertanto da sosttuire con l'id del record desiderato
     *
     * @param id id del record desiderato
     * @return oggetto corrispondente all'id richiesto
     */
    @GetMapping("/data/{id}")
    public StrutturaAlberghiera getStrAlbById(@PathVariable int id) {
        return service.getStrAlb(id);
    }

    /**
     * Metodo che gestisce la richiesta GET alla rotta "/metadata", restituisce i metadata
     *
     * @return lista dei metadata
     */
    @GetMapping("/metadata")
    public List getMetadata() {
        return service.getMetadata();
    }

    /**
     * Metodo che gestisce la richiesta GET alla rotta "/stats", restituisce le statistiche
     *
     * @param fieldName parametro opzionale per richiedere le statistiche di un solo campo
     * @return lista contenente le statistiche richieste
     */
    @GetMapping("/stats")
    public List getStats(@RequestParam(value = "field", required = false, defaultValue = "") String fieldName) {
        if (fieldName.equals("")) {
            return service.getStats();
        } else {
            List<Map> list = new ArrayList<>();
            list.add(service.getStats(fieldName));
            return list;
        }
    }

    /**
     * Metodo ausiliario che esegue il parsing del filtro passato tramite body di una POST
     *
     * @param body body della richiesta POST contenente un filtro
     * @return mappa contenente i parametri del filtro: campo, operatore, valore di riferimento
     */
    private static Map<String, Object> parseFilter(String body) {
        Map<String, Object> parsedBody = new BasicJsonParser().parseMap(body);
        String fieldName = parsedBody.keySet().toArray(new String[0])[0];
        Object rawValue = parsedBody.get(fieldName);
        Object refValue;
        String operator;
        if (rawValue instanceof Map) {
            Map filter = (Map) rawValue;
            //System.out.println(filter);
            operator = ((String) filter.keySet().toArray()[0]).toLowerCase();
            refValue = filter.get(operator);
        } else {
            operator = "$eq";
            refValue = rawValue;
        }
        Map<String, Object> filter = new HashMap<>();
        filter.put("operator", operator);
        filter.put("field", fieldName);
        filter.put("ref", refValue);
        return filter;
    }

    /**
     * Metodo che gestisce una richiesta POST alla rotta "/data", resituisce la lista dei record che soddisfano il filtro
     *
     * @param body body della richiesta POST che contiene il filtro
     * @return lista di oggetti che soddisfano il filtro
     */
    @PostMapping("/data")
    public List getFilteredData(@RequestBody String body) {
        Map<String, Object> filter = parseFilter(body);
        String fieldName = (String) filter.get("field");
        String operator = (String) filter.get("operator");
        Object refValue = filter.get("ref");
        return service.getFilteredData(fieldName, operator, refValue);
    }

    /**
     * Metodo che gestisce la richiesta POST alla rotta "/stats", restiuisce le statistiche sul campo richiesto (opzionale) o su tutti i campi, considerando soltanto i record che soddisfano il filtro
     *
     * @param fieldName campo di cui si richiedono le statistiche (opzionale)
     * @param body      body della richiesta POST che contiene il filtro
     * @return lista contenente le statistiche richieste
     */
    @PostMapping("/stats")
    public List<Map> getFilteredStats(@RequestParam(value = "field", required = false, defaultValue = "") String fieldName, @RequestBody String body) {
        Map<String, Object> filter = parseFilter(body);
        String fieldToFilter = (String) filter.get("field");
        String operator = (String) filter.get("operator");
        Object refValue = filter.get("ref");
        if (fieldName.equals("")) {
            return service.getFilteredStats(fieldToFilter, operator, refValue);
        } else {
            List<Map> list = new ArrayList<>();
            list.add(service.getFilteredStats(fieldName, fieldToFilter, operator, refValue));
            return list;
        }
    }

}
