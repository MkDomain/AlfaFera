package me.mkdomain.alfafera.utils;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import io.javalin.http.Context;
import me.mkdomain.alfafera.Main;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Base64;

/**
 * Pár gyakrabban használt hasznos dolgok
 */
public class Utils {

    /**
     * Megformáz egy méretet byte-ból emberként olvasható mennyiségbe
     *
     * @param bytes A méret
     * @return Egy szöveg ami a méretet tartalmazza olvasható formában
     */
    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f%cb", value / 1024.0, ci.current());
    }

    /**
     * Letölt egy adott URL-ről
     *
     * @param url Az URL
     * @return A bináris reprezentációja a letöltött adatnak
     * @throws IOException ha valami nem jött össze
     */
    public static byte[] downloadFromURL(URL url) throws IOException {
        InputStream is = url.openConnection().getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int nRead;
        while ((nRead = is.read(buffer)) != -1) {
            bos.write(buffer, 0, nRead);
        }
        return bos.toByteArray();
    }

    /**
     * Letölt egy URL-ről, de közben mutatja, hogy hány százaléknál tart
     *
     * @param url Az URL
     * @return A bináris reprezentációja a letöltött adatnak
     * @throws IOException ha valami nem jött össze
     */
    public static byte[] downloadFromURLWithMonitoringBar(URL url) throws IOException {
        InputStream is = url.openConnection().getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        double all = Integer.parseInt(url.openConnection().getHeaderField("content-length"));
        byte[] buffer = new byte[512];
        int nRead;
        double readTotal = 0;
        while ((nRead = is.read(buffer)) != -1) {
            bos.write(buffer, 0, nRead);
            readTotal += nRead;
            double percent = Math.ceil((readTotal / all) * 100) / 100;
            progressPercentage((int) (percent * 100), 100);
        }
        System.out.println();
        return bos.toByteArray();
    }

    /**
     * Egy PDF-ből kinyeri a szöveget
     *
     * @param file A PDF fájl
     * @return A szöveg
     */
    public static String getTextFromPdf(Path file) {
        try {
            PDDocument pdDoc = PDDocument.load(file.toFile());
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String parsedText = pdfStripper.getText(pdDoc);
            pdDoc.close();
            return parsedText.replaceAll("[^A-Za-z0-9. ]+", "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * Egy PDF fáj első oldalából csinál képet
     *
     * @param file A fájl
     * @return A kép JPEG formátumban
     * @throws IOException ha valami nem jött össze
     */
    public static byte[] renderPdfThumbnailImage(Path file) throws IOException {
        //PDF fájl kiolvasása
        File pdfFile = file.toFile();
        RandomAccessFile raf = new RandomAccessFile(pdfFile, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

        //Kép elkészítése
        PDFFile pdf = new PDFFile(buf);
        PDFPage page = pdf.getPage(0);
        Rectangle rect = new Rectangle(0, 0, (int) page.getBBox().getWidth(), (int) page.getBBox().getHeight());
        BufferedImage bufferedImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
        Image image = page.getImage(rect.width, rect.height, rect, null, true, true);
        Graphics2D bufImageGraphics = bufferedImage.createGraphics();
        bufImageGraphics.drawImage(image, 0, 0, null);

        //Kép mentése 60%-os JPEG tömörítéssel
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(0.6f);
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        writer.setOutput(new MemoryCacheImageOutputStream(bos));
        writer.write(null, new IIOImage(bufferedImage, null, null), jpegParams);

        return bos.toByteArray();
    }

    /**
     * ProgressBar-t csinál a konzolba
     *
     * @param remain A kitöltés mértéke
     * @param total  A kitöltött hossz
     */
    private static void progressPercentage(int remain, int total) {
        String start = "[";
        for (int i = 0; i < total; i++) {
            start += remain <= i ? "-" : "=";
        }
        System.out.print("\r" + start + "]");
    }

    /**
     * MD5 hash-t hajt végre
     *
     * @param s A bemeneti szöveg
     * @return Az MD5 hash Base64-ben enkódolva
     */
    public static String md5(String s) {
        return md5(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * MD5 hash-t hajt végre
     *
     * @param s A bemeneti adat
     * @return Az MD5 hash Base64-ben enkódolva
     */
    public static String md5(byte[] s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s);
            return Base64.getEncoder().encodeToString(s);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * Megnézi, hogy egy adott link létezik-e
     *
     * @param url A link
     * @return true ha létezik
     */
    public static boolean linkExists(URL url) {
        try {
            url.openConnection().getInputStream().close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Megadja a a kliens ip címét
     *
     * @param ctx A lekérési kontextus
     * @return A kliens ip címe
     */
    public static String getIp(Context ctx) {
        return Main.getIpFromHeader() ? ctx.header("X-Real-IP") : ctx.ip();
    }

}
