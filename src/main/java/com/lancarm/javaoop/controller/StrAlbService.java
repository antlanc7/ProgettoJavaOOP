package com.lancarm.javaoop.controller;

import com.lancarm.javaoop.model.StrutturaAlberghiera;
import org.springframework.boot.json.BasicJsonParser;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        try {
            String link = "https://www.dati.gov.it/api/3/action/package_show?id=310fc617-37a6-4ad2-bcab-25bf69512693";
            URLConnection urlConnection = new URL(link).openConnection();
            urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:64.0) Gecko/20100101 Firefox/64.0");
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String json = br.readLine();
            System.out.println(json);
            Map map = new BasicJsonParser().parseMap(json);
            Map result = (Map) map.get("result");
            List resources = (List) result.get("resources");
            String linkcsv = "";
            for (Object r : resources) {
                Map mr = (Map) r;
                if (mr.get("format").equals("csv")) linkcsv = (String) mr.get("url");
            }
            System.out.println(linkcsv);
            URL csvurl = new URL(linkcsv);
            BufferedReader br2 = new BufferedReader(new InputStreamReader(csvurl.openStream()));
            br2.readLine();
            String line;
            while ((line = br2.readLine()) != null) {
                String[] values = line.trim().split(";");
                StrutturaAlberghiera nuova = new StrutturaAlberghiera(values[0], values[1], values[2], values[3], values[4], Integer.parseInt(values[5]));
                strutture.add(nuova);
                System.out.println(nuova);
            }
        } catch (MalformedURLException e) {
            System.err.println("URL Errato");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    //TODO fare metodi per accedere ai dati parsati e poi fare la classe controller
}
