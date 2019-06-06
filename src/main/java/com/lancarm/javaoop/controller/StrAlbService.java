package com.lancarm.javaoop.controller;

import com.lancarm.javaoop.model.StrutturaAlberghiera;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class StrAlbService {

    private List<StrutturaAlberghiera> strutture = new ArrayList<>();

    public StrAlbService() {
        String serialFileName = "dati.ser";
        if (Files.exists(Paths.get(serialFileName))) {
            caricaSeriale(serialFileName);
        } else {
            String url = "https://www.dati.gov.it/api/3/action/package_show?id=310fc617-37a6-4ad2-bcab-25bf69512693"; // URL fornitoci
            try {
                parsing(url);
                salvaSeriale(serialFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (StrutturaAlberghiera s : strutture) {
            System.out.println(s);
        }
    }

    private void parsing(String link) throws IOException {
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
                // System.out.println(nuova);
            }
        } catch (MalformedURLException e) {
            System.err.println("URL Errato");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // nel finally si chiudono i buffer eventualmente rimasti aperti
            if (br != null) br.close();
        }
    }

    private void salvaSeriale(String fileName) {
        // salvataggio tramite Serial della lista su file per evitare di fare ogni volta il parsing
        // buffer per il salvataggio tramite seriale della lista creata
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(strutture.toArray(new StrutturaAlberghiera[0]));    // la lista viene salvata come array per evitare successivi problemi di casting in lettura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void caricaSeriale(String fileName) {
        // buffer per il caricamento tramite seriale della lista creata
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            // caricamento tramite Serial della lista da file per evitare di fare ogni volta il parsing
            // la lista è salvata come array per evitare problemi di casting, quindi viene poi riconvertita in lista
            strutture = Arrays.asList((StrutturaAlberghiera[]) ois.readObject());   //readObject non prende parametri in ingresso perchè legge in fila
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Classe non trovata");
            e.printStackTrace();
        }
    }

    //TODO fare metodi per accedere ai dati parsati e poi fare la classe controller
    //metodo per retituire la lista con tutti i dati
    public List getAllData() {
        return strutture;
    }

    public StrutturaAlberghiera getStrAlb(int i) {//restituiamo la i-esima struttura
        return strutture.get(i);
    }
}
