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


/**
 * Classe che carica il dataset e ne gestisce l'accesso
 */
@Service
public class StrAlbService {

    private List<StrutturaAlberghiera> strutture = new ArrayList<>();
    private List<Map> metadata = new ArrayList<>();

    /**
     * Costruttore che carica il dataset facendo il parsing del csv da remoto oppure ricaricando un parsing precedente da file seriale cache
     */
    public StrAlbService() {
        String serialFileName = "dataset.ser";
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


        //La parte seguente del costruttore genera i metadati e li conserva in modo da restituirli istantaneamente al momento della richiesta

        Field[] fields = StrutturaAlberghiera.class.getDeclaredFields();//estrae gli attributi della classe struttura alberghiera

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

    /**
     * Metodo che esegue il parsing dei dati dal csv
     *
     * @param link url del json contenente il link del file csv
     * @throws IOException in caso di errori nella lettura dai buffer
     */
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

    /**
     * Metodo che esegue il salvataggio tramite seriale java in cache locale della lista di oggetti parsati
     *
     * @param fileName nome del file cache da creare
     */
    private void salvaSeriale(String fileName) {
        // salvataggio tramite Serial della lista su file per evitare di fare ogni volta il parsing
        // buffer per il salvataggio tramite seriale della lista creata
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(strutture.toArray(new StrutturaAlberghiera[0]));    // la lista viene salvata come array per evitare successivi problemi di casting in lettura
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo che esegue il caricamento tramite seriale java in cache locale della lista di oggetti parsati da una sessione precedente
     *
     * @param fileName nome del file cache da leggere
     */
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

    /**
     * Restituisce l'intero dataset
     *
     * @return la lista completa degli oggetti
     */
    public List getAllData() {
        return strutture;
    }

    /**
     * Restituisce l'oggetto corrispondente all'id passato
     *
     * @param id id-indice dell'oggetto richiesto
     * @return l'oggetto corrispondente al valore di id ricevuto
     */
    public StrutturaAlberghiera getStrAlb(int id) {//restituiamo la i-esima struttura
        if (id < strutture.size()) return strutture.get(id);
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id: '" + id + "' does not exist");
    }

    /**
     * Restituisce la lista dei metadati
     *
     * @return lista dei metadati
     */
    public List getMetadata() {
        return metadata;
    }

    /**
     * Restituisce le statistiche relative ad un certo campo
     *
     * @param fieldName nome del campo
     * @return Map contenente le statistiche
     */
    public Map getStats(String fieldName) {
        return Stats.getAllStats(fieldName, getFieldValues(fieldName));
    }

    /**
     * Restituisce le statistiche relative a tutti i campi
     *
     * @return lista di mappe contenenti le statistiche relative ad ogni campo
     */
    public List<Map> getStats() {
        Field[] fields = StrutturaAlberghiera.class.getDeclaredFields();// questo ci da l'elenco di tutti gli attributi della classe
        List<Map> list = new ArrayList<>();
        for (Field f : fields) {
            String fieldName = f.getName();//f è l'oggetto di tipo fieldsName estrae il nome del campo corrente
            list.add(getStats(fieldName));//va ad aggiungere alla lista  la mappa che contiene le statistiche del campo fieldName
        }
        return list;
    }

    /**
     * Metodo che estrae dalla lista di oggetti (dataset) la lista dei valori relativi ad un singolo campo
     *
     * @param fieldName campo del quale estrarre i valori
     * @return lista dei valori del campo richiesto
     */
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

    /**
     * Restituisce la lista di oggetti che soddisfano il filtro
     *
     * @param fieldName campo da filtrare
     * @param operator  operatore di confronto
     * @param ref       valore di riferimento
     * @return lista di oggetti che soddisfano il filtro
     */
    public List<StrutturaAlberghiera> getFilteredData(String fieldName, String operator, Object ref) {
        List<Integer> indexes = Filter.select(getFieldValues(fieldName), operator, ref);    //esegue il filtraggio ricavando la lista degli indici degli oggetti
        List<StrutturaAlberghiera> out = new ArrayList<>(); //costruisce la lista di oggetti a partire da quella degli indici
        for (int i : indexes) {
            out.add(strutture.get(i));
        }
        return out;
    }

    /**
     * Restituisce le statistiche relative ad un campo considerando solo i valori di oggetti che soddisfano il filtro
     *
     * @param fieldToStats  campo del quale si richiedono le statistiche
     * @param fieldToFilter campo sul quale si richiede il filtraggio
     * @param operator      operatore di confronto
     * @param ref           valore di riferimento
     * @return Mappa contenente le statistiche relative al campo passato come primo parametro
     */
    public Map getFilteredStats(String fieldToStats, String fieldToFilter, String operator, Object ref) {
        List<Integer> indexes = Filter.select(getFieldValues(fieldToFilter), operator, ref);
        List allValues = getFieldValues(fieldToStats);
        List<Object> filteredValues = new ArrayList<>();
        for (int i : indexes) {
            filteredValues.add(allValues.get(i));
        }
        return Stats.getAllStats(fieldToStats, filteredValues);
    }

    /**
     * Restituisce le statistiche relative a tutti i campi considerando solo i valori di oggetti che soddisfano il filtro
     *
     * @param fieldToFilter campo sul quale si richiede il filtraggio
     * @param operator      operatore di confronto
     * @param ref           valore di riferimento
     * @return lista delle mappe contenenti le statistiche di ogni campo
     */
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
