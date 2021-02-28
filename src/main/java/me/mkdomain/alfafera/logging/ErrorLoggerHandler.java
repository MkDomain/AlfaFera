package me.mkdomain.alfafera.logging;

import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Hibák naplózására készült
 */
public class ErrorLoggerHandler implements ExceptionHandler<Exception> {

    private static final ArrayList<ExceptionDumpReport> exceptions = new ArrayList<>();

    public static ArrayList<ExceptionDumpReport> getExceptions() {
        return exceptions;
    }

    @Override
    public void handle(@NotNull Exception exception, @NotNull Context ctx) {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final PrintStream ps1 = new PrintStream(bos);
            exception.printStackTrace(ps1);
            ctx.contentType("text/html; charset=utf-8");
            final String stacktrace = Arrays.stream(bos.toString().split("\n"))
                    .map(e -> e.startsWith("\t") ? "<a class=\"margin\">" + e.substring(1) + "</a>" : e)
                    .collect(Collectors.joining("\n<br>"));
            exceptions.add(new ExceptionDumpReport(exception, ctx.fullUrl(), System.currentTimeMillis(), ctx));
            System.err.println("Hiba futás közben: [" + exception.getClass().getName() + "]  :  " + exception.getMessage());
            ctx.result(new String(Files.readAllBytes(Paths.get("html/exception.html"))).replace("%stacktrace%", stacktrace));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public static class ExceptionDumpReport {
        private final Exception exception;
        private final String url;
        private final long accessDate;

        private final HashMap<String, String> cookieMap;
        private final HashMap<String, String> pathParamMap;
        private final HashMap<String, String> headerMap;
        private final HashMap<String, List<String>> formParamMap;
        private final HashMap<String, List<String>> queryParamMap;
        private final HashMap<String, Object> sessionMap;

        public ExceptionDumpReport(Exception exception, String url, long accessDate, Context context) {
            this.exception = exception;
            this.url = url;
            this.accessDate = accessDate;
            this.cookieMap = new HashMap<>(context.cookieMap());
            this.pathParamMap = new HashMap<>(context.pathParamMap());
            this.formParamMap = new HashMap<>(context.formParamMap());
            this.queryParamMap = new HashMap<>(context.queryParamMap());
            this.headerMap = new HashMap<>(context.headerMap());
            this.sessionMap = new HashMap<>(context.sessionAttributeMap());
        }

        public Exception getException() {
            return exception;
        }

        public String getUrl() {
            return url;
        }

        public long getAccessDate() {
            return accessDate;
        }

        public HashMap<String, String> getCookieMap() {
            return cookieMap;
        }

        public HashMap<String, String> getPathParamMap() {
            return pathParamMap;
        }

        public HashMap<String, String> getHeaderMap() {
            return headerMap;
        }

        public HashMap<String, List<String>> getFormParamMap() {
            return formParamMap;
        }

        public HashMap<String, List<String>> getQueryParamMap() {
            return queryParamMap;
        }

        public HashMap<String, Object> getSessionMap() {
            return sessionMap;
        }
    }
}
