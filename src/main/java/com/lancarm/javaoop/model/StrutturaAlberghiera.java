package com.lancarm.javaoop.model;

/**
 * Classe che modella il singolo record del dataset csv
 */
public class StrutturaAlberghiera {
    private String insegna, categoria, indirizzo, municipio, tipologia;
    private int camere;

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
