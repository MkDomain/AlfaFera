package me.mkdomain.alfafera.notes;

import me.mkdomain.alfafera.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A biológia jegyzetek tárolására, és cache-elésére szükséges osztály
 */
public class Note {

    private final String link;
    private final List<NoteCategory> categories;
    private final String realName;
    private final Path file;
    private byte[] image;

    /**
     * Egy adott linkbpl csinál helyi jegyzetetet
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

        //Nincs meg a gépen a jegyzet letöltve
        if (!Files.exists(file)) {
            try {
                Files.createDirectory(file.getParent());
            } catch (IOException e) {
            }
            try {
                System.out.println("\nJegyzet letöltése... [" + link + "]");
                Files.write(file, Utils.downloadFromURLWithMonitoringBar(new URL(link)), StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //Nincsen a jegyzetnek előnézete
            Path localImage = Paths.get("local/preview/" + file.getFileName().toString().substring(0, file.getFileName().toString().length() - 4) + ".jpg");
            if (!Files.exists(localImage)) {
                try {
                    Files.createDirectory(Paths.get("local/preview"));
                } catch (Exception ignored) {
                }
                this.image = Utils.renderPdfThumbnailImage(file);
                Files.write(localImage, this.image, StandardOpenOption.CREATE);
            } else {
                this.image = Files.readAllBytes(localImage);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return Megadja a helyi PDF fájlt
     */
    public Path getFile() {
        return file;
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
        this.image = Utils.renderPdfThumbnailImage(file);
        Files.write(Paths.get("local/preview/" + file.getFileName().toString().substring(0, file.getFileName().toString().length() - 4) + ".jpg"), this.image, StandardOpenOption.CREATE);
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
}
