package me.mkdomain.alfafera.notes;

import me.mkdomain.alfafera.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A biológia jegyzetek tárolására, és cache-elésére szükséges osztály
 */
public class Note {

    private static int ids = 0;

    private final String link;
    private final List<NoteCategory> categories;
    private final String realName;
    private final Path file;
    private final int id;
    private byte[] image;

    /**
     * Egy adott linkből csinál helyi jegyzetetet
     *
     * @param link A link
     */
    public Note(String link, List<NoteCategory> categories, String realName) {
        this.link = link;
        this.categories = categories;
        this.realName = realName;
        String[] arr = new String[0];
        try {
            arr = URLDecoder.decode(link, "UTF-8").split("\\/");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        this.file = Paths.get(("local/" + arr[arr.length - 1]).replace("+", " "));
        this.id = ids;
        ids++;
        load();
    }

    private void load() {
        //Jegyzet betöltése a memóriába
        try {
            boolean downloaded = false;
            if (NoteHolder.needDownload(this.id, file.getFileName().toString())) {
                NoteHolder.addNote(this.id, Utils.downloadFromURL(new URL(link)), file.getFileName().toString());
                downloaded = true;
            }
            this.image = Utils.renderPdfThumbnailImage(NoteHolder.getNote(this.id, this.file.getFileName().toString()));
            System.out.println("Jegyzet betöltve [" + realName + "]" + (downloaded ? " (letöltve)" : ""));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return Megadja a PDF fájl indexképét binárisan
     */
    public byte[] getImage() {
        return image;
    }

    /**
     * @return Megadja az ember által olvasható nevét a jegyzetnek
     */
    public String name() {
        return realName;
    }

    /**
     * @return Ember által olvasható formátumúra formázza a kategóriákat
     */
    public String categories() {
        return categories.stream().map(NoteCategory::getName).collect(Collectors.joining(", "));
    }

    /**
     * Kiszámoltatja a jegyzet képét az UpToDateCheckerThread-nek van rá szüksége
     *
     * @throws IOException ha valami nem jött össze
     */
    public void updateImage() throws IOException {
        this.image = Utils.renderPdfThumbnailImage(NoteHolder.getNote(this.id, this.file.getFileName().toString()));
    }

    /**
     * @return Megadja a jegyzet linkjét
     */
    public String getLink() {
        return link;
    }

    /**
     * @return Megadja a jegyzet kategóriájit
     */
    public List<NoteCategory> getCategories() {
        return categories;
    }

    public int getId() {
        return id;
    }

    public Path getFile() {
        return file;
    }
}
