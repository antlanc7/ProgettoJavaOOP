package com.lancarm.javaoop.controller;

import com.lancarm.javaoop.model.StrutturaAlberghiera;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StrAlbService {

    private List<StrutturaAlberghiera> strutture = new ArrayList<>();

    public StrAlbService() {
        String link = "https://www.dati.gov.it/api/3/action/package_show?id=310fc617-37a6-4ad2-bcab-25bf69512693";  // URL fornitoci

    }
    private void parsing (String link){
        // Inizializzo i buffer
        BufferedReader br = null;   // buffer per il parsing
        try {

            URLConnection urlConnection = new URL(link).openConnection();   // apro la connessione all'URL
            urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:64.0) Gecko/20100101 Firefox/64.0"); // aggiungo alla connessione l'user-agent dato che il protocollo è httpS
            br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream())); // apro il buffer di lettura del json ottenuto dall'URL
            // Dato che il JSON è scritto in un'unica riga mi basta una sola lettura
            String json = br.readLine();    // leggo dal buffer il json e lo salvo come stringa
            br.close();     // chiudo il buffer
            System.out.println(json);   // stampo in console il json letto per debug
            Map map = new BasicJsonParser().parseMap(json); // passo la stringa del json al parser di Spring che mi restituisce la mappa chiave-valore associata
            // navigo nella mappa fino all'URL del file csv
            Map result = (Map) map.get("result");   // il metodo get della classe Map mi restituisce un generico Object -> Devo fare il casting
            List resources = (List) result.get("resources");
            String linkcsv = "";
            // Scorro tutte le risorse cercando quella con formato csv -> a quel punto estraggo l'URL
            for (Object r : resources) {
                Map mr = (Map) r;
                if (mr.get("format").equals("csv")) linkcsv = (String) mr.get("url");
            }
            System.out.println(linkcsv);    // stampo in console l'URL per debug
            URL csvurl = new URL(linkcsv);  // apro la connessione all'URL
            br = new BufferedReader(new InputStreamReader(csvurl.openStream()));    // apro il buffer di lettura
            br.readLine();  // salto la prima riga (header) leggendola "a vuoto"
            String line;
            while ((line = br.readLine()) != null) {    // leggo il file riga per riga fino alla fine
                // trim elimina i caratteri non visibili, split divide la riga in corrispondenza del separatore
                String[] values = line.trim().split(";");
                // creo l'oggetto StrutturaAlberghiera sulla base dei valori parsati dal csv
                StrutturaAlberghiera nuova = new StrutturaAlberghiera(values[0], values[1], values[2], values[3], values[4], Integer.parseInt(values[5]));
                // aggiunge l'oggetto appena creato alla lista
                strutture.add(nuova);
                // stampa l'oggetto in console per debug
                System.out.println(nuova);
            }


        } catch (MalformedURLException e) {
            System.err.println("URL Errato");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // nel finally si chiudono i buffer eventualmente rimasti aperti
            if (br != null) br.close();
            if (oos != null) oos.close();
        }

    }
    private void salvaSeriale(String fileName) throws IOException {
        ObjectOutputStream oos = null;  // buffer per il salvataggio tramite seriale della lista creata
        try {
            //Salvataggio tramite Serial della lista su file per evitare di fare ogni volta il parsing
            oos = new ObjectOutputStream(new FileOutputStream("dati.ser"));
            oos.writeObject(strutture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            // nel finally si chiudono i buffer eventualmente rimasti aperti
            if (oos != null) oos.close();
        }
    }

    //TODO fare metodi per accedere ai dati parsati e poi fare la classe controller
}
