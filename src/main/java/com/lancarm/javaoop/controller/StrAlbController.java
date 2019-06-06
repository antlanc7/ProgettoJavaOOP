package com.lancarm.javaoop.controller;

import com.lancarm.javaoop.model.StrutturaAlberghiera;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public List getAllData (){
        return service.getAllData();
    }

    @GetMapping("/data/{id}")
    public StrutturaAlberghiera getStrAlbById(@PathVariable int id){
        return service.getStrAlb(id);
    }
}