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

//controller sping che gestisce le chiamate dell'utente
@RestController
public class StrAlbController {
    //creo un oggetto di tipo Service
    private StrAlbService service;

    @Autowired //stiamo dicendo al controllore che lui dipende da service, ovvero stiamo iniettando una dipendenza
    public StrAlbController(StrAlbService service) {
        this.service = service;
    }

    //metodi per la comunicazione con il client
    @GetMapping("/data")
    public List getAllData() {
        return service.getAllData();
    }

    @GetMapping("/data/{id}")
    public StrutturaAlberghiera getStrAlbById(@PathVariable int id) {
        return service.getStrAlb(id);
    }

    @GetMapping("/metadata")
    public List getMetadata() {
        return service.getMetadata();
    }

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

    @PostMapping("/data")
    public List getFilteredData(@RequestBody String body) {
        Map<String, Object> filter = parseFilter(body);
        String fieldName = (String) filter.get("field");
        String operator = (String) filter.get("operator");
        Object refValue = filter.get("ref");
        return service.getFilteredData(fieldName, operator, refValue);
    }

    @PostMapping("/stats")
    public List<Map> getFilteredStats(@RequestParam(value="field",required = false, defaultValue = "") String fieldName, @RequestBody String body) {
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
