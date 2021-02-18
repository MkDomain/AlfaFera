package me.mkdomain.alfafera;

import io.javalin.Javalin;
import me.mkdomain.alfafera.handlers.*;
import me.mkdomain.alfafera.logging.AccessLoggerHandler;
import me.mkdomain.alfafera.logging.ErrorLoggerHandler;
import me.mkdomain.alfafera.notes.Note;
import me.mkdomain.alfafera.notes.NoteImport;
import me.mkdomain.alfafera.notes.UpToDateCheckerThread;
import me.mkdomain.alfafera.stats.NoteStatistics;
import me.mkdomain.alfafera.stats.StatisticsManager;
import me.mkdomain.alfafera.utils.Logger;
import me.mkdomain.alfafera.utils.UnLoggerSLF4J;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class Main {

    private static final StatisticsManager manager = new StatisticsManager();
    private static final List<Note> notes = new ArrayList<>();

    private static boolean getIpFromHeader = false;
    private static Javalin app;

    private static void progressPercentage(int remain, int total) {
        String start = "[";
        for (int i = 0; i < total; i++) {
            start += remain <= i ? "-" : "=";
        }
        System.out.print("\r" + start + "]");
    }

    public static StatisticsManager getManager() {
        return manager;
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0)
            if (args.length == 1) getIpFromHeader = Boolean.parseBoolean(args[0]);
        Logger.init();
        System.out.println("Alfaféra indítása...");
        System.out.println("Jegyzetek betöltése...");
        NoteImport.importNotes();

        if (Files.exists(Paths.get("similarity.info"))) {
            for (String readAllLine : Files.readAllLines(Paths.get("similarity.info"))) {
                if (!readAllLine.contains("= ")) continue;
                String key = readAllLine.split("= ")[0];
                double value = Double.parseDouble(readAllLine.split("= ")[1]);
                NoteStatistics.getSimilarityMap().put(key, value);
            }
        }
        NoteStatistics.load();

        double i = 1;
        double all = notes.size();
        for (Note note : getNotes()) {
            double percent = Math.max(100, i / all);
            progressPercentage((int) (percent * 100), 100);
            NoteStatistics.similarity(note, false);
            i++;
        }
        System.out.print("\n");
        new UpToDateCheckerThread().start();
        System.out.println("Jegyzetek betöltve.");
        System.out.println("Szerver indítása...");
        long start = System.currentTimeMillis();
        Javalin.log = new UnLoggerSLF4J();
        app = Javalin.create()
                .exception(FileNotFoundException.class, (ex, ctx) -> ctx.status(404))
                .exception(NoSuchFileException.class, (ex, ctx) -> ctx.status(404))
                .exception(NoSuchElementException.class, (ex, ctx) -> ctx.status(404))
                .exception(Exception.class, new ErrorLoggerHandler())
                .get("/", new IndexHandler())
                .get("/jegyzetek", new NotesHandler())
                .get("/oldalrol", new FileHandler("html/oldalrol.html", true))
                .get("/kapcsolat", new FileHandler("html/kapcsolat.html", true))
                .get("/gyik", new FileHandler("html/faq.html", true))
                .get("stats", new StatisticsHandler())
                .get("/jegyzet/:name/kep", new NoteImageHandler())
                .get("/jegyzet/:name/pdf", new NoteViewHandler())
                .get("/jegyzet/:name/megtekintes", new BetterNoteViewHandler())
                .get("/view/:name/viewer.js", new BetterNoteViewScriptHandler())
                .get("/view/:name/viewer", new BetterNoteViewViewerHandler())
                .get("/view/build/pdf.worker.js", new FileHandler("html/assets/pdf.worker.js", false))
                .get("/jegyzet/letoltes/:name/", new NoteDownloadHandler())
                .get("/jegyzet/:name", new NoteHandler())
                .get("assets/*", new AssetsHandler())
                .post("/kapcsolat-action", new ContactPostHandler())
                /*.get("/stop", ctx -> System.exit(0))*/
                .after(ctx -> getManager().incrementServedData(Objects.nonNull(ctx.resultStream()) ? ctx.resultStream().available() : 0))
                .after(new AccessLoggerHandler())
                .start(8888);
        System.out.println("A szerver elindult " + (System.currentTimeMillis() - start) + "ms alatt!");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //Elérési naplózás mentése
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sb.append("======================").append("\n").append("Elérési napló\n").append("======================").append("\n");
            for (AccessLoggerHandler.LogRecord access : AccessLoggerHandler.getAccesses()) {
                sb.append(String.format("%-20s%-80s%-20s", access.getIp(), access.getUrl(), sdf.format(new Date(access.getAccessDate())))).append("\n");
            }

            //Hibák mentése
            sb.append("======================").append("\n").append("Futtatási hibák\n").append("======================").append("\n");
            for (ErrorLoggerHandler.ExceptionDumpReport report : ErrorLoggerHandler.getExceptions()) {
                sb.append(String.format("Dátum: %-20s\nUrl: %-80s\nHiba fajtája: %-20s\nHiba üzenete: %-40s\nSütik: %-150s\nFejlécek: %-150s\nÚtvonal paraméterek: %-150s\nSession adatok: %-150s\nForm paraméterek: %-150s\nLekérési paraméterek: %-150s\n\n", sdf.format(new Date(report.getAccessDate())),
                        report.getUrl(),
                        report.getException().getClass().getName(),
                        report.getException().getMessage(),
                        "(" + report.getCookieMap().entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(", ")) + ")",
                        "(" + report.getHeaderMap().entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(", ")) + ")",
                        "(" + report.getPathParamMap().entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(", ")) + ")",
                        "(" + report.getSessionMap().entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(", ")) + ")",
                        "(" + report.getFormParamMap().entrySet().stream().map(e -> e.getKey() + ":[" + String.join(", ", e.getValue()) + "]").collect(Collectors.joining(", ")) + ")",
                        "(" + report.getQueryParamMap().entrySet().stream().map(e -> e.getKey() + ":[" + String.join(", ", e.getValue()) + "]").collect(Collectors.joining(", ")) + ")"
                )).append("\n");
            }

            //Napló mentése
            try {
                ByteArrayOutputStream compressedOut = new ByteArrayOutputStream();
                GZIPOutputStream gzipOut = new GZIPOutputStream(compressedOut);
                gzipOut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                gzipOut.close();
                compressedOut.close();
                if (!Files.exists(Paths.get("logs")))
                    Files.createDirectory(Paths.get("logs"));
                Files.write(Paths.get("logs", System.currentTimeMillis() + ".log.gz"), compressedOut.toByteArray(), StandardOpenOption.CREATE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }


            //Statisztikák mentése
            getManager().save();
            try {
                NoteStatistics.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    public static boolean getIpFromHeader() {
        return getIpFromHeader;
    }

    public static List<Note> getNotes() {
        return notes;
    }
}
