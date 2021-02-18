package me.mkdomain.alfafera.notes;

import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Az eredeti oldaról importálja a jegyzeteket
 */
public class NoteImport {


    /**
     * A fő metódus
     */
    public static void importNotes() {
        try {
            HashMap<String, AbstractMap.SimpleEntry<String, List<NoteCategory>>> realNameLinkCategory = new HashMap<>();
            //Section megtalálása
            Document document = Jsoup.connect("http://bioszfera.com").get();
            Element sectionEmelt = document.getElementById("emeltszintujegyzetek");
            Element emeltLista = sectionEmelt.getElementsByTag("ul").get(0);
            for (Element li : emeltLista.getElementsByTag("a")) {
                //Ha nincs href akkor az nem jegyzet
                if (li.hasAttr("href")) {
                    //Ha nem pdf fájlra hivatkozik akkor az nem jegyzet
                    if (li.attr("href").endsWith(".pdf")) {
                        //Vannak nem működő linkek
                        if (li.attr("href").endsWith("/.pdf")) continue;
                        //Van ahol az a-n kívül van egy font tag is
                        if (li.parent().tag().toString().equalsIgnoreCase("font")) {
                            //Van, hogy nincs kategóriája a jegyzetnek
                            if (li.parent().parent().parent().parent().getElementsByTag("b").isEmpty()) {
                                realNameLinkCategory.put(li.text(), new AbstractMap.SimpleEntry<>(link(li), Arrays.asList(NoteCategory.EMELT_SZINT, NoteCategory.getBySiteName("NINCS"))));
                                continue;
                            }
                            realNameLinkCategory.put(li.text(), new AbstractMap.SimpleEntry<>(link(li), Arrays.asList(NoteCategory.EMELT_SZINT, NoteCategory.getBySiteName(li.parent().parent().parent().parent().getElementsByTag("b").get(0).text()))));
                        } else {
                            realNameLinkCategory.put(li.text(), new AbstractMap.SimpleEntry<>(link(li), Arrays.asList(NoteCategory.EMELT_SZINT, NoteCategory.getBySiteName(li.parent().parent().parent().getElementsByTag("b").get(0).text()))));
                        }
                    }
                }
            }

            Element sectionAlap = document.getElementById("alaporasjegyzetek");
            Element alapLista = sectionAlap.getElementsByTag("ul").get(0);
            for (Element li : alapLista.getElementsByTag("a")) {
                //Ha nincs href akkor az nem jegyzet
                if (li.hasAttr("href")) {
                    //Ha nem pdf fájlra hivatkozik akkor az nem jegyzet
                    if (li.attr("href").endsWith(".pdf")) {
                        //Vannak nem működő linkek
                        if (li.attr("href").endsWith("/.pdf")) continue;
                        //Van ahol az a-n kívül van egy font tag is
                        if (li.parent().tag().toString().equalsIgnoreCase("font")) {
                            //Van, hogy nincs kategóriája a jegyzetnek
                            if (li.parent().parent().parent().parent().getElementsByTag("b").isEmpty()) {
                                realNameLinkCategory.put(li.text(), new AbstractMap.SimpleEntry<>(link(li), Arrays.asList(NoteCategory.ALAP_SZINT, NoteCategory.getBySiteName("NINCS"))));
                                continue;
                            }
                            realNameLinkCategory.put(li.text(), new AbstractMap.SimpleEntry<>(link(li), Arrays.asList(NoteCategory.ALAP_SZINT, NoteCategory.getBySiteName(li.parent().parent().parent().parent().getElementsByTag("b").get(0).text()))));
                        } else {
                            realNameLinkCategory.put(li.text(), new AbstractMap.SimpleEntry<>(link(li), Arrays.asList(NoteCategory.ALAP_SZINT, NoteCategory.getBySiteName(li.parent().parent().parent().getElementsByTag("b").get(0).text()))));
                        }
                    }
                }
            }
            //Nem létező jegyzeteket nem töltünk be
            realNameLinkCategory.entrySet().removeIf(e -> {
                try {
                    if (!Utils.linkExists(new URL(e.getValue().getKey()))) return true;
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
                return false;
            });

            //A létező jegyzetek betöltése
            AtomicInteger i = new AtomicInteger(1);
            double all = realNameLinkCategory.size();
            realNameLinkCategory.forEach((k, v) -> {
                double percent = i.get() / all;
                progressPercentage((int) (percent * 100), 100);

                Main.getNotes().add(new Note(v.getKey(), v.getValue(), k));
                i.getAndIncrement();
            });
            System.out.print("\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String link(Element element) throws UnsupportedEncodingException {
        //Linkek enkódolása
        return URLEncoder.encode(element.attr("href"), StandardCharsets.UTF_8.name()).replace("%2F", "/").replace("%3A", ":");
    }

    private static void progressPercentage(int remain, int total) {
        String start = "[";
        for (int i = 0; i < total; i++) {
            start += remain <= i ? "-" : "=";
        }
        System.out.print("\r" + start + "]");
    }

}
