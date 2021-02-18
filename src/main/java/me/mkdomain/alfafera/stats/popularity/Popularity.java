package me.mkdomain.alfafera.stats.popularity;

import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.notes.Note;
import me.mkdomain.alfafera.stats.NoteStatistics;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * A népszerűség miatt szükséges
 */
public class Popularity {

    private static boolean init = false;

    /**
     * Elindítja azt a szálat ami automatikusan menti a számokat
     */
    private static void init() {
        if (init) return;
        new Thread(() -> {
            while (true) {
                StringBuilder sb = new StringBuilder();
                for (Note note : Main.getNotes()) {
                    double points = getPont(note.name());
                    sb.append(note.name()).append(",").append(points).append("\n");
                }
                try {
                    ByteArrayOutputStream compressedOut = new ByteArrayOutputStream();
                    GZIPOutputStream gzipOut = new GZIPOutputStream(compressedOut);
                    gzipOut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                    gzipOut.close();
                    compressedOut.close();
                    if (!Files.exists(Paths.get("popularity")))
                        Files.createDirectory(Paths.get("popularity"));
                    Files.write(Paths.get("popularity", System.currentTimeMillis() + ".csv.gz"), compressedOut.toByteArray(), StandardOpenOption.CREATE);
                    Thread.sleep(3600000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
        init = true;
    }

    /**
     * Megadja egy jegyzet népszerűségi pontszámát
     *
     * @param name A jegyzet neve
     * @return A jegyzet pontszáma
     */
    public static double getPont(String name) {
        init();
        return (NoteStatistics.getPoints(name) * (((NoteStatistics.getDownloads(name) + 1) * (NoteStatistics.getViews(name) + 1)) * 0.2));
    }

    /**
     * Megadja két pont távolságát a 2D-s euklédeszi térben
     *
     * @param first_x  Az első pont x kordinátája
     * @param first_y  Az első pont y kordinátája
     * @param second_x A második pont x kordinátája
     * @param second_y A második pont y kordinátája
     * @return A távolság
     */
    private static double getDistanceTwoDimensions(double first_x, double first_y, double second_x, double second_y) {
        init();
        return Math.sqrt(Math.pow(Math.abs(first_x - second_x), 2) + Math.pow(Math.abs(first_y - second_y), 2));
    }

    /**
     * @return A jegyzetek népszerűség szerint csökkenő sorrendben
     */
    public static List<Note> getNotesByPopularityDescending() {
        init();
        return Main.getNotes().stream().sorted(Comparator.comparingDouble(e -> getPont(e.name()) * -1)).collect(Collectors.toList());
    }

    /**
     * @return Megadja a legnépszerűbb jegyzetet
     */
    public static Note getMostPopularNote() {
        init();
        double currentLargest = 0;
        Note currentNote = Main.getNotes().get(0);
        for (Note note : Main.getNotes()) {
            double points = getPont(note.name());
            if (points > currentLargest) {
                currentLargest = points;
                currentNote = note;
            }
        }
        return currentNote;
    }

    /**
     * Megadja egy jegyzet leközelebbi szomszédait (k-NN algoritmus alapján)
     *
     * @param note A jegyzet
     * @return A három legközelebb álló jegyzet
     */
    public static List<NearestNeighborsResult<Note>> getNearestNeighbors(Note note) {
        init();
        double inPoints = getPont(note.name());
        return Main.getNotes().stream()
                .filter(e -> !e.name().equalsIgnoreCase(note.name()))
                .map(e -> new NearestNeighborsResult<>(e, getDistanceTwoDimensions(
                        inPoints, 1 / 2,
                        getPont(e.name()), NoteStatistics.getSimilarity(note.name(), e.name()) / (note.getCategories().get(1).equals(e.getCategories().get(1)) ? 2 : 1)
                )))
                .sorted(Comparator.comparingDouble(NearestNeighborsResult::getDistance))
                .limit(3)
                .collect(Collectors.toList());
    }

}
