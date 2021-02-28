package me.mkdomain.alfafera.stats.popularity;

import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.notes.Note;
import me.mkdomain.alfafera.stats.NoteStatistics;
import me.mkdomain.alfafera.utils.Utils;

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
                final StringBuilder sb = new StringBuilder();
                for (Note note : Main.getNotes()) {
                    final double points = getPont(note.name());
                    sb.append(note.name()).append(",").append(points).append("\n");
                }
                try {
                    final ByteArrayOutputStream compressedOut = new ByteArrayOutputStream();
                    final GZIPOutputStream gzipOut = new GZIPOutputStream(compressedOut);
                    gzipOut.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
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
        return Math.max(NoteStatistics.getPoints(name), 1) * Math.max(NoteStatistics.getDownloads(name), 1) * Math.max(NoteStatistics.getViews(name), 1);
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
     * Megadja két pont távolságát a 3D-s euklédeszi térben
     *
     * @param first_x  Az első pont x kordinátája
     * @param first_y  Az első pont y kordinátája
     * @param first_z  Az első pont z kordinátája
     * @param second_x A második pont x kordinátája
     * @param second_y A második pont y kordinátája
     * @param second_z A második pont z kordinátája
     * @return A távolság
     */
    private static double getDistanceThreeDimensions(double first_x, double first_y, double first_z, double second_x, double second_y, double second_z) {
        init();
        return Math.sqrt(Math.pow(Math.abs(first_x - second_x), 2) + Math.pow(Math.abs(first_y - second_y), 2) + Math.pow(Math.abs(first_z - second_z), 2));
    }

    //mindent 0 és 1 közé transzponál
    private static double smoothDistance(double first_x, double first_y, double first_z, double second_x, double second_y, double second_z) {
        init();
        double x_diff = Math.abs(first_x - second_x);
        double y_diff = Math.abs(first_y - second_y);
        double z_diff = Math.abs(first_z - second_z);
        if (x_diff > 1) {
            x_diff = 1 / x_diff;

        }
        if (y_diff > 1) {
            y_diff = 1 / y_diff;
        }
        if (z_diff > 1) {
            z_diff = 1 / z_diff;
        }
        return Math.sqrt(Math.pow(x_diff, 2) + Math.pow(y_diff, 2) + Math.pow(z_diff, 2));
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
                .map(e -> new NearestNeighborsResult<>(e, smoothDistance(
                        inPoints, 1, 1,
                        getPont(e.name()), NoteStatistics.getSimilarity(note.name(), e.name()), Utils.listSimilarity(note.getCategories(), e.getCategories()))
                ))
                .sorted(Comparator.comparingDouble(NearestNeighborsResult::getDistance))
                .limit(3)
                .collect(Collectors.toList());
    }

}
