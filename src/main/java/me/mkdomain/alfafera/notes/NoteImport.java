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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Az eredeti oldaról importálja a jegyzeteket
 */
public class NoteImport {


    /**
     * A fő metódus
     */
    public static void importNotes() {
        try {
            System.out.print("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + "INFO" + "   " + "Jegyzetek lekérése az eredeti oldalról...");
            final HashMap<String, AbstractMap.SimpleEntry<String, List<NoteCategory>>> realNameLinkCategory = new HashMap<>();
            //Section megtalálása
            final Document document = Jsoup.connect("http://bioszfera.com").get();
            final Element sectionEmelt = document.getElementById("emeltszintujegyzetek");
            final Element emeltLista = sectionEmelt.getElementsByTag("ul").get(0);
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

            final Element sectionAlap = document.getElementById("alaporasjegyzetek");
            final Element alapLista = sectionAlap.getElementsByTag("ul").get(0);
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
            System.out.print("\r[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + "INFO" + "   " + "Jegyzetek lekérve\n");
            System.out.print("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + "INFO" + "   " + "Hibás jegyzetek kiszűrése...");
            final int before = realNameLinkCategory.size();
            //Nem létező jegyzeteket nem töltünk be
            List<String> keysToRemove = new CopyOnWriteArrayList<>();
            Main.executeAndBlock(realNameLinkCategory.entrySet().stream().map(e -> (Runnable) () -> {
                try {
                    if (!Utils.linkExists(new URL(e.getValue().getKey()))) keysToRemove.add(e.getKey());
                } catch (MalformedURLException malformedURLException) {
                    throw new RuntimeException(malformedURLException);
                }
            }).collect(Collectors.toList()));
            keysToRemove.forEach(realNameLinkCategory::remove);
            System.out.print("\r[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + "INFO" + "   " + "Kiszűrve " + (before - realNameLinkCategory.size()) + "db jegyzet\n");
            System.out.println("Jegyzetek betöltése (" + realNameLinkCategory.size() + ")");
            System.out.println("=======================================================================");

            //A létező jegyzetek betöltése
            Main.executeAndBlock(realNameLinkCategory.entrySet().stream().map((e) -> (Runnable) () -> Main.getNotes().add(new Note(e.getValue().getKey(), e.getValue().getValue(), e.getKey()))).collect(Collectors.toList()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String link(Element element) throws UnsupportedEncodingException {
        //Linkek enkódolása
        return URLEncoder.encode(element.attr("href"), StandardCharsets.UTF_8.name()).replace("%2F", "/").replace("%3A", ":");
    }

}
