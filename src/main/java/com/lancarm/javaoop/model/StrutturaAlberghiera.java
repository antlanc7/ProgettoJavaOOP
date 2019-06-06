package com.lancarm.javaoop.model;

import java.io.Serializable;

/**
 * Classe che modella il singolo record del dataset csv
 */
public class StrutturaAlberghiera implements Serializable {  //Serializable serve a rendere salvabili su file gli oggetti della classe
    private String insegna, categoria, indirizzo, municipio, tipologia;
    private int camere;

    /**
     * Costruttore della classe
     * @param insegna Primo campo del file csv
     * @param categoria Secondo campo del file csv
     * @param indirizzo Terzo campo del file csv
     * @param municipio Quarto campo del file csv
     * @param tipologia Quinto campo del file csv
     * @param camere Sesto campo del file csv
     */
    public StrutturaAlberghiera(String insegna, String categoria, String indirizzo, String municipio, String tipologia, int camere) {
        this.insegna = insegna;
        this.categoria = categoria;
        this.indirizzo = indirizzo;
        this.municipio = municipio;
        this.tipologia = tipologia;
        this.camere = camere;
    }

    public String getInsegna() {
        return insegna;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public String getMunicipio() {
        return municipio;
    }

    public String getTipologia() {
        return tipologia;
    }

    public int getCamere() {
        return camere;
    }

    /**
     * Metodo toString per la stampa dell'oggetto
     * @return Restituisce una stringa contenente il valore dei vari campi
     */
    @Override
    public String toString() {
        return "StrutturaAlberghiera{" +
                "insegna='" + insegna + '\'' +
                ", categoria='" + categoria + '\'' +
                ", indirizzo='" + indirizzo + '\'' +
                ", municipio='" + municipio + '\'' +
                ", tipologia='" + tipologia + '\'' +
                ", camere=" + camere +
                '}';
    }
}
