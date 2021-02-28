package me.mkdomain.alfafera.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A naplózást csinálja szebbé
 */
public class Logger extends PrintStream {

    private static final ByteArrayOutputStream memory = new ByteArrayOutputStream();

    private final String type;
    private final String prefix;

    public Logger(String type, String prefix) {
        super(new DoubleStream(System.out, memory));
        this.type = type;
        this.prefix = prefix;
    }

    public static void init() {
        System.setOut(new Logger("INFO", ""));
        System.setErr(new Logger("ERROR", "\u001B[31m"));
    }

    public static String getLogs() {
        try {
            return memory.toString(StandardCharsets.UTF_8.name()).replace("\u001B[31m", "").replace("\u001B[0m", "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void close() {
    }

    @Override
    public void print(boolean b) {
        super.print(prefix + b);
    }

    @Override
    public void print(char c) {
        super.print(prefix + c);
    }

    @Override
    public void print(int i) {
        super.print(prefix + i);
    }

    @Override
    public void print(long l) {
        super.print(prefix + l);
    }

    @Override
    public void print(float f) {
        super.print(prefix + f);
    }

    @Override
    public void print(double d) {
        super.print(prefix + d);
    }

    @Override
    public void print(@NotNull char[] s) {
        super.print(prefix + new String(s));
    }

    @Override
    public void print(@Nullable String s) {
        super.print(prefix + s);
    }

    @Override
    public void print(@Nullable Object obj) {
        super.print(prefix + obj);
    }

    @Override
    public void println() {
        super.println();
    }

    @Override
    public void println(boolean x) {
        super.println(prefix + "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + type + "   " + x + "\u001B[0m");
    }

    @Override
    public void println(char x) {
        super.println(prefix + "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + type + "   " + x + "\u001B[0m");
    }

    @Override
    public void println(int x) {
        super.println(prefix + "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + type + "   " + x + "\u001B[0m");
    }

    @Override
    public void println(long x) {
        super.println(prefix + "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + type + "   " + x + "\u001B[0m");
    }

    @Override
    public void println(float x) {
        super.println(prefix + "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + type + "   " + x + "\u001B[0m");
    }

    @Override
    public void println(double x) {
        super.println(prefix + "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + type + "   " + x + "\u001B[0m");
    }

    @Override
    public void println(@NotNull char[] x) {
        super.println(prefix + "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + type + "   " + new String(x) + "\u001B[0m");
    }

    @Override
    public void println(@Nullable String x) {
        super.println(prefix + "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + type + "   " + x + "\u001B[0m");
    }

    @Override
    public void println(@Nullable Object x) {
        super.println(prefix + "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]  " + type + "   " + x + "\u001B[0m");
    }

    static class DoubleStream extends OutputStream{

        private final OutputStream first;
        private final OutputStream second;

        public DoubleStream(OutputStream first, OutputStream second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public void write(int b) throws IOException {
            first.write(b);
            second.write(b);
        }

        @Override
        public void write(@NotNull byte[] b, int off, int len) throws IOException {
            first.write(b, off, len);
            second.write(b, off, len);
        }

        @Override
        public void write(@NotNull byte[] b) throws IOException {
            first.write(b);
            second.write(b);
        }

        @Override
        public void flush() throws IOException {
            first.flush();
            second.flush();
        }

        @Override
        public void close() throws IOException {
            first.close();
            second.close();
        }
    }
}
