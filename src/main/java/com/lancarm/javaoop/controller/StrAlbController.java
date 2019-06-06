package com.lancarm.javaoop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
}
