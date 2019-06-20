package com.lancarm.javaoop.service;

import com.lancarm.javaoop.model.StrutturaAlberghiera;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class StrAlbService {

    private List<StrutturaAlberghiera> strutture = new ArrayList<>();
    private List<Map> metadata;

    public StrAlbService() {
        String serialFileName = "dati.ser";
        if (Files.exists(Paths.get(serialFileName))) {
            caricaSeriale(serialFileName);
            System.out.println("Dataset ricaricato da disco");
        } else {
            String url = "https://www.dati.gov.it/api/3/action/package_show?id=310fc617-37a6-4ad2-bcab-25bf69512693"; // URL fornitoci
            try {
                parsing(url);
                System.out.println("Dataset parsato da file csv da remoto");
                salvaSeriale(serialFileName);
                System.out.println("Dataset salvato su disco locale");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Field[] fields = StrutturaAlberghiera.class.getDeclaredFields();//estrae gli attributi della classe struttura alberghiera

        metadata = new ArrayList<>();

        for (Field f : fields) {
            Map<String, String> map = new HashMap<>();
            //andiamo ad inserire le coppie chiave valore
            map.put("alias", f.getName());
            map.put("sourceField", f.getName().toUpperCase());//nome del campo in csv
            //touppercase serve per convertire il nome in maiuscolo
            map.put("type", f.getType().getSimpleName());
            metadata.add(map);
        }
        /* Stampa la lista generata per debug
        for (StrutturaAlberghiera s : strutture) {
            System.out.println(s);
        }
        */
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
            // System.out.println(json);   // stampo in console il json letto per debug
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
            // System.out.println(linkcsv);    // stampo in console l'URL per debug
            URL csvurl = new URL(linkcsv);  // apro la connessione all'URL
            br = new BufferedReader(new InputStreamReader(csvurl.openStream()));    // apro il buffer di lettura
            br.readLine();  // salto la prima riga (header) leggendola "a vuoto"
            String line;
            while ((line = br.readLine()) != null) {    // leggo il file riga per riga fino alla fine
                // trim elimina i caratteri non visibili, split divide la riga in corrispondenza del separatore
                String[] csvLineSplitted = line.trim().split(";");
                // vado a estrarre i valori dei singoli campi dalla riga effettuando eventuali conversioni
                String insegna = csvLineSplitted[0].trim().replaceAll("\"", "").toUpperCase();
                String categoria = csvLineSplitted[1].trim();
                String indirizzo = csvLineSplitted[2].trim();
                String municipio = csvLineSplitted[3].trim().split(" ")[1];
                String tipologia = csvLineSplitted[4].trim();
                int camere = Integer.parseInt(csvLineSplitted[5].trim());
                // creo l'oggetto StrutturaAlberghiera sulla base dei valori parsati
                StrutturaAlberghiera nuova = new StrutturaAlberghiera(insegna, categoria, indirizzo, municipio, tipologia, camere);
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

    //metodo per retituire la lista con tutti i dati
    public List getAllData() {
        return strutture;
    }

    public StrutturaAlberghiera getStrAlb(int i) {//restituiamo la i-esima struttura
        if (i < strutture.size()) return strutture.get(i);
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Id: '"+i+"' does not exist");
    }

    public List getMetadata() {
        return metadata;
    }

    public Map getStats(String fieldName) {
        return Stats.getAllStats(fieldName, getFieldValues(fieldName));
    }

    public List<Map> getStats() {
        Field[] fields = StrutturaAlberghiera.class.getDeclaredFields();// questo ci da l'elenco di tutti gli attributi della classe
        List<Map> list = new ArrayList<>();
        for (Field f : fields) {
            String fieldName = f.getName();//f è l'oggetto di tipo fieldsName estrae il nome del campo corrente
            list.add(getStats(fieldName));//va ad aggiungere alla lista  la mappa che contiene le statistiche del campo fieldName
        }
        return list;
    }

    private List getFieldValues(String fieldName) {
        List<Object> values = new ArrayList<>();
        try {
            //serve per scorrere tutte le strutture ed estrarre i valori del campo fieldName
            for (StrutturaAlberghiera s : strutture) {
                Method getter = StrutturaAlberghiera.class.getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
                Object value = getter.invoke(s);
                values.add(value);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field '" + fieldName + "' does not exist");
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return values;
    }

    public List<StrutturaAlberghiera> getFilteredData(String fieldName, String operator, Object ref) {
        List<Integer> indexes = Filter.select(getFieldValues(fieldName), operator, ref);
        List<StrutturaAlberghiera> out = new ArrayList<>();
        for (int i : indexes) {
            out.add(strutture.get(i));
        }
        return out;
    }

    public Map getFilteredStats(String fieldToStats, String fieldToFilter, String operator, Object ref) {
        List<Integer> indexes = Filter.select(getFieldValues(fieldToFilter), operator, ref);
        List allValues = getFieldValues(fieldToStats);
        List<Object> filteredValues = new ArrayList<>();
        for (int i : indexes) {
            filteredValues.add(allValues.get(i));
        }
        return Stats.getAllStats(fieldToStats, filteredValues);
    }

    public List<Map> getFilteredStats(String fieldToFilter, String operator, Object ref) {
        Field[] fields = StrutturaAlberghiera.class.getDeclaredFields();// questo ci da l'elenco di tutti gli attributi della classe
        List<Map> list = new ArrayList<>();
        for (Field f : fields) {
            String fieldName = f.getName();//f è l'oggetto di tipo field, getName estrae il nome del campo corrente
            list.add(getFilteredStats(fieldName, fieldToFilter, operator, ref));//va ad aggiungere alla lista  la mappa che contiene le statistiche del campo fieldName filtrato
        }
        return list;
    }


}
