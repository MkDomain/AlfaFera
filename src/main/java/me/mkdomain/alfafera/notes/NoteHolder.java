package me.mkdomain.alfafera.notes;

import me.mkdomain.alfafera.Main;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A jegyzetek tárolásáért felel
 * Vagy fájlban vagy cacheben
 */
public class NoteHolder {

    private static final ConcurrentHashMap<Integer, Byte[]> notes = new ConcurrentHashMap<>();

    public static byte[] getNote(int id, String name) {
        if (!Main.USE_FILE_CACHE) {
            Byte[] b = notes.get(id);
            byte[] out = new byte[b.length];
            int i = 0;
            for (Byte aByte : b) {
                out[i] = aByte;
                i++;
            }
            return decompress(out);
        }else{
            try {
                return decompress(Files.readAllBytes(Paths.get("local", name + ".gz")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

    public static void addNote(int id, byte[] data, String name) {
        if (!Main.USE_FILE_CACHE) {
            byte[] compressed = compress(data);

            Byte[] out = new Byte[compressed.length];
            int i = 0;
            for (byte b : compressed) {
                out[i] = b;
                i++;
            }
            notes.put(id, out);
        }else{
            try {
                if (!Files.exists(Paths.get("local")))
                    Files.createDirectory(Paths.get("local"));
                Files.write(Paths.get("local", name + ".gz"), compress(data), StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean needDownload(int id, String name) {
        return !((Main.USE_FILE_CACHE && Files.exists(Paths.get("local", name + ".gz"))) || notes.containsKey(id));
    }

    private static byte[] compress(byte[] in) {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final GZIPOutputStream gzos = new GZIPOutputStream(out);
            gzos.write(in);
            gzos.close();
            out.close();
            return out.toByteArray();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return in;
    }

    private static byte[] decompress(byte[] in) {
        try {
            final GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(in));
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            int nRead;
            byte[] buffer = new byte[256];
            while ((nRead = gzis.read(buffer)) != -1) {
                out.write(buffer, 0, nRead);
            }
            gzis.close();
            out.close();
            return out.toByteArray();
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        return in;
    }

}
