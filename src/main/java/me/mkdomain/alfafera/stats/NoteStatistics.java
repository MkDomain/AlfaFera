package me.mkdomain.alfafera.stats;

import info.debatty.java.stringsimilarity.Cosine;
import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.notes.Note;
import me.mkdomain.alfafera.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A jegyzetek statisztikáját tárolja
 */
public class NoteStatistics {

    private static final HashMap<String, Integer> points = new HashMap<>();
    private static final HashMap<String, Integer> views = new HashMap<>();
    private static final HashMap<String, Integer> downloads = new HashMap<>();

    private static final HashMap<String, Long> next = new HashMap<>();
    private static final HashMap<String, Long> nextView = new HashMap<>();
    private static final HashMap<String, Long> nextDownload = new HashMap<>();
    private static final HashMap<String, Double> similarity = new HashMap<>();

    /**
     * Beállítja két jegyzet hasonlóságát
     *
     * @param first  Az első jegyzet neve
     * @param second A második jegyzet neve
     * @param points A hasonlóság százalékban
     */
    public static void setSimilarity(String first, String second, double points) {
        similarity.put(first + "-" + second, points);
    }

    /**
     * Kiszámolja egy jegyzet hasonlóságát a többi jegyzethez
     *
     * @param note        A jegyzet
     * @param ignoreCache A cache ignorálása
     */
    public static void similarity(Note note, boolean ignoreCache) {
        List<Note> others = new ArrayList<>(Main.getNotes());
        others.remove(note);
        String inText = Utils.getTextFromPdf(note.getFile());
        for (Note other : others) {
            if (ignoreCache) {
                similarity.remove(note.name() + "-" + other.name());
                similarity.remove(other.name() + "-" + note.name());
            }
            if (!ignoreCache && (similarity.containsKey(note.name() + "-" + other.name()) || similarity.containsKey(other.name() + "-" + note.name())))
                continue;
            String otherText = Utils.getTextFromPdf(other.getFile());
            double point = new Cosine().similarity(inText, otherText);
            setSimilarity(note.name(), other.name(), point);
        }

        StringBuilder sb = new StringBuilder();
        similarity.forEach((k, v) -> sb.append(k).append("= ").append(v).append("\n"));
        try {
            Files.write(Paths.get("similarity.info"), sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Megadja azt a Map-et amiben a hasonlóság található
     */
    public static HashMap<String, Double> getSimilarityMap() {
        return similarity;
    }

    /**
     * Megadja két jegyzet hassonlóságát
     *
     * @param first  Az első jegyzet neve
     * @param second A második jegyzet neve
     * @return A hasonlóság százalékban
     */
    public static double getSimilarity(String first, String second) {
        if (similarity.containsKey(first + "-" + second)) return similarity.get(first + "-" + second);
        return similarity.get(second + "-" + first);
    }

    /**
     * Egy adott jegyzet pontját növeli
     *
     * @param name A jegyzet neve
     * @param ip   A kliens ip címe
     */
    public static void addPoints(String name, String ip) {
        if (next.getOrDefault(Utils.md5(ip + name), (long) 0) > System.currentTimeMillis()) return;
        points.put(name, points.getOrDefault(name, 0) + 1);
        next.put(Utils.md5(ip + name), System.currentTimeMillis() + 8 * 60 * 60 * 1000);
    }

    /**
     * Megadja egy jegyzet pontszámát
     *
     * @param name A jegyzet neve
     * @return A jegyzet pontszáma
     */
    public static int getPoints(String name) {
        return points.getOrDefault(name, 0);
    }


    /**
     * Egy adott jegyzet megtekintését növeli
     *
     * @param name A jegyzet neve
     * @param ip   A kliens ip címe
     */
    public static void addView(String name, String ip) {
        if (nextView.getOrDefault(Utils.md5(ip + name), (long) 0) > System.currentTimeMillis()) return;
        views.put(name, views.getOrDefault(name, 0) + 1);
        nextView.put(Utils.md5(ip + name), System.currentTimeMillis() + 30 * 60 * 1000);
    }

    /**
     * Megadja egy jegyzet megtekintéseit
     *
     * @param name A jegyzet neve
     * @return A megtekintések száma
     */
    public static int getViews(String name) {
        return views.getOrDefault(name, 0);
    }


    /**
     * egy adott jegyzet letöltéseit növeli
     *
     * @param name A jegyzet neve
     * @param ip   A kliens ip címe
     */
    public static void addDownload(String name, String ip) {
        if (nextDownload.getOrDefault(Utils.md5(ip + name), (long) 0) > System.currentTimeMillis()) return;
        downloads.put(name, downloads.getOrDefault(name, 0) + 1);
        nextDownload.put(Utils.md5(ip + name), System.currentTimeMillis() + 30 * 60 * 1000);
    }

    /**
     * Megadja egy jegyzet letöltési számát
     *
     * @param name A jegyzet neve
     * @return A letöltések száma
     */
    public static int getDownloads(String name) {
        return downloads.getOrDefault(name, 0);
    }

    /**
     * Megadja az összes kattintást
     *
     * @return Az összes kattintás száma
     */
    public static int getALlPoints() {
        return points.values().stream().mapToInt(e -> e).sum();
    }

    public static void save() throws IOException {
        StringBuilder sb = new StringBuilder();
        points.forEach((name, points) -> {
            int views = getViews(name);
            int downloads = getDownloads(name);
            sb.append(name).append("= ").append(views).append(":").append(downloads).append(":").append(points).append("\n");
        });
        Files.write(Paths.get("note_statistics.info"), sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
    }

    public static void load() throws IOException {
        if (!Files.exists(Paths.get("note_statistics.info"))) return;
        for (String readAllLine : Files.readAllLines(Paths.get("note_statistics.info"))) {
            String name = readAllLine.split("= ")[0];
            String raw = readAllLine.split("= ")[1];

            int views = Integer.parseInt(raw.split(":")[1]);
            int downloads = Integer.parseInt(raw.split(":")[1]);
            int points = Integer.parseInt(raw.split(":")[1]);
            if (views > 0) NoteStatistics.views.put(name, views);
            if (downloads > 0) NoteStatistics.downloads.put(name, downloads);
            if (points > 0) NoteStatistics.points.put(name, points);
        }
    }

}
