package com.lancarm.javaoop.controller;

import com.lancarm.javaoop.model.StrutturaAlberghiera;
import com.lancarm.javaoop.service.StrAlbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Field;
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
    public List getAllData(@RequestParam(value = "field", required = false, defaultValue = "") String fieldName) {
        if (fieldName.equals("")) {
            return service.getAllData();
        } else return service.getFieldValues(fieldName);
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

}
