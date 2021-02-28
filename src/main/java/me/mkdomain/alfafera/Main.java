package me.mkdomain.alfafera;

import io.javalin.Javalin;
import me.mkdomain.alfafera.configuration.BasicConfiguration;
import me.mkdomain.alfafera.configuration.Configuration;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class Main {

    public static int LOAD_THREAD_COUNT;
    public static boolean USE_FILE_CACHE;
    private static String ADMIN_USER;
    private static boolean GET_IP_FROM_HEADER = false;

    private static final StatisticsManager manager = new StatisticsManager();
    private static final List<Note> notes = new CopyOnWriteArrayList<>();

    private static ExecutorService threadPool;
    private static Configuration configuration;

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

    public static void main(String[] args) throws IOException, InterruptedException {
        final long start = System.currentTimeMillis();
        //Logger bekapcsolása
        Logger.init();

        System.out.println("Alfaféra indítása...");
        configuration = new BasicConfiguration();
        configuration.load(Paths.get("config.txt"));
        if (!configuration.contains("thread_count")) {
            configuration.set("thread_count", String.valueOf(Runtime.getRuntime().availableProcessors()));
        }
        if (!configuration.contains("file_cache")) {
            configuration.set("file_cache", String.valueOf(false));
        }
        if (!configuration.contains("admin_user")) {
            configuration.set("admin_user", "admin:admin");
        }
        if (!configuration.contains("get_ip_from_header")) {
            configuration.set("get_ip_from_header", String.valueOf(false));
        }
        if (!configuration.contains("discord_webhook_url")) {
            configuration.set("discord_webhook_url", "<URL>");
        }
        LOAD_THREAD_COUNT = Integer.parseInt(configuration.get("thread_count"));
        USE_FILE_CACHE = Boolean.parseBoolean(configuration.get("file_cache"));
        ADMIN_USER = configuration.get("admin_user");
        GET_IP_FROM_HEADER = Boolean.parseBoolean(configuration.get("get_ip_from_header"));
        configuration.save(Paths.get("config.txt"));

        threadPool = Executors.newFixedThreadPool(Main.LOAD_THREAD_COUNT);
        System.out.println("File információk betöltése...");
        NoteStatistics.load();

        NoteImport.importNotes();
        System.out.println("Jegyzetek egymáshoz való hasonlóságának számítása");

        //Befejezett feladatok
        final AtomicInteger finished = new AtomicInteger(0);
        //Befejezett számítások
        final AtomicInteger done = new AtomicInteger(0);
        final double all = notes.size();
        final double everyCalc = (all * (all - 1) / 2);
        executeAndUpdate(getNotes().stream().map(e -> (Runnable) () -> NoteStatistics.similarity(e, false, done)).collect(Collectors.toList()), finished);
        int current;
        while ((current = finished.get()) != all) {
            final double percent = Math.min(100, current / all);
            progressPercentage((int) (percent * 100), 100);
            System.out.print(" (" + done.get() + "/" + everyCalc + ")");
            Thread.sleep(150);
            if (current == all) break;
        }
        System.out.print("\n");

        //Frissítő szál indítása
        new UpToDateCheckerThread().start();
        System.out.println("Jegyzetek betöltve.");
        System.out.println("Szerver indítása...");
        final long serverStart = System.currentTimeMillis();
        Javalin.log = new UnLoggerSLF4J();
        Javalin.create()
                .exception(FileNotFoundException.class, (ex, ctx) -> ctx.status(404))
                .exception(NoSuchFileException.class, (ex, ctx) -> ctx.status(404))
                .exception(NoSuchElementException.class, (ex, ctx) -> ctx.status(404))
                .exception(Exception.class, new ErrorLoggerHandler())
                .get("/", new IndexHandler())
                .get("/jegyzetek", new NotesHandler())
                .get("/oldalrol", new FileHandler("html/oldalrol.html", true))
                .get("/kapcsolat", new FileHandler("html/kapcsolat.html", true))
                .get("/gyik", new FileHandler("html/faq.html", true))
                .get("/stats", new StatisticsHandler())
                .get("/logs", (ctx) -> {
                    if (ctx.header("Authorization") == null) {
                        ctx.header("WWW-Authenticate", "Basic realm=\"AlfaFera\"");
                        ctx.header("HTTP/1.0 401 Unauthorized");
                        ctx.status(401);
                        ctx.result("Bejelentkezés szükséges.");
                        return;
                    }else{
                        if (!ctx.header("Authorization").substring("Basic ".length()).equals(Base64.getEncoder().encodeToString(Main.ADMIN_USER.getBytes(StandardCharsets.UTF_8)))) {
                            ctx.header("WWW-Authenticate", "Basic realm=\"AlfaFera\"");
                            ctx.header("HTTP/1.0 401 Unauthorized");
                            ctx.status(401);
                            ctx.result("Bejelentkezés szükséges.");
                            return;
                        }
                    }



                    final String outText = getLogsSpecial().replace("\n", "<br>") + "<br><br><br><br><br>" + Logger.getLogs().replace("\n", "<br>");
                    ctx.contentType("text/html; charset=utf-8");
                    ctx.result(outText);
                })
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
                .after(ctx -> getManager().incrementServedData(Objects.nonNull(ctx.resultStream()) ? ctx.resultStream().available() : 0))
                .after(new AccessLoggerHandler())
                .start(8888);
        System.out.println("A szerver elindult " + ((System.currentTimeMillis() - start) / 1000) + "s alatt! (ebből a szerver: " + (System.currentTimeMillis() - serverStart) + "ms)");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                configuration.save(Paths.get("config.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Elérési naplózás mentése
            final String outText = (getLogsSpecial() + "\n\n\n\n\n" + Logger.getLogs()).trim();

            //Napló mentése
            try {
                ByteArrayOutputStream compressedOut = new ByteArrayOutputStream();
                GZIPOutputStream gzipOut = new GZIPOutputStream(compressedOut);
                gzipOut.write(outText.getBytes(StandardCharsets.UTF_8));
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

    public static Configuration getConfiguration() {
        return configuration;
    }

    public static boolean getIpFromHeader() {
        return GET_IP_FROM_HEADER;
    }

    public static List<Note> getNotes() {
        return notes;
    }

    public static void executeAndBlock(List<Runnable> tasks) {
        final AtomicInteger done = new AtomicInteger(0);
        final int total = tasks.size();
        executeAndUpdate(tasks, done);
        while (true) {
            if (done.get() == total) break;
        }
    }

    public static void executeAndUpdate(List<Runnable> tasks, AtomicInteger updater) {
        tasks.forEach(e -> threadPool.execute(() -> {
            try {
                e.run();
            }catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Futási hiba egy külső szálon!");
            }
            updater.getAndIncrement();
        }));
    }

    public static String getLogsSpecial() {
        final StringBuilder sb = new StringBuilder();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sb.append("======================").append("\n").append("Elérési napló\n").append("======================").append("\n");
        for (AccessLoggerHandler.LogRecord access : AccessLoggerHandler.getAccesses()) {
            sb.append(String.format("%-20s%-80s%-20s", access.getIp(), access.getUrl(), sdf.format(new Date(access.getAccessDate())))).append("\n");
        }

        //Hibák mentése
        sb.append("======================").append("\n").append("Futási hibák\n").append("======================").append("\n");
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
        return sb.toString();
    }
}
