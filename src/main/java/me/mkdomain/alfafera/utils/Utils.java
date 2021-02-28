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
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Objects;

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
        final long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
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
        final InputStream is = url.openConnection().getInputStream();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final byte[] buffer = new byte[512];
        int nRead;
        while ((nRead = is.read(buffer)) != -1) {
            bos.write(buffer, 0, nRead);
        }
        return bos.toByteArray();
    }

    /**
     * Egy PDF-ből kinyeri a szöveget
     *
     * @param file A PDF fájl
     * @return A szöveg
     */
    public static String getTextFromPdf(byte[] file) {
        try {
            final PDDocument pdDoc = PDDocument.load(file);
            final PDFTextStripper pdfStripper = new PDFTextStripper();
            final String parsedText = pdfStripper.getText(pdDoc);
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
     * @param data A fájl
     * @return A kép JPEG formátumban
     * @throws IOException ha valami nem jött össze
     */
    public static byte[] renderPdfThumbnailImage(byte[] data) throws IOException {
        //PDF fájl kiolvasása
        final ByteBuffer buf = ByteBuffer.wrap(data);

        //Kép elkészítése
        final PDFFile pdf = new PDFFile(buf);
        final PDFPage page = pdf.getPage(0);
        final Rectangle rect = new Rectangle(0, 0, (int) page.getBBox().getWidth(), (int) page.getBBox().getHeight());
        final BufferedImage bufferedImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_RGB);
        final Image image = page.getImage(rect.width, rect.height, rect, null, true, true);
        final Graphics2D bufImageGraphics = bufferedImage.createGraphics();
        bufImageGraphics.drawImage(image, 0, 0, null);

        //Kép mentése 60%-os JPEG tömörítéssel
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(0.6f);
        final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        writer.setOutput(new MemoryCacheImageOutputStream(bos));
        writer.write(null, new IIOImage(bufferedImage, null, null), jpegParams);

        return bos.toByteArray();
    }

    /**
     * ProgressBar-t csinál a konzolba
     *
     * @param remain A kitöltés mértéke
     * @param total  A kitöltött hossz
     * @param prefix A prefix ami minden sor előtt szerepel
     */
    private static void progressPercentage(int remain, int total, String prefix) {
        String start = "[";
        for (int i = 0; i < total; i++) {
            start += remain <= i ? "-" : "=";
        }
        System.out.print("\r" + prefix + start + "]");
    }

    /**
     * SHA-512 hash-t hajt végre
     *
     * @param s A bemeneti szöveg
     * @return Az SHA-512 hash hex-ben enkódolva
     */
    public static String sha512(String s) {
        return sha512(s.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * SHA-512 hash-t hajt végre
     *
     * @param s A bemeneti adat
     * @return Az SHA-512 hash hex-ben enkódolva
     */
    public static String sha512(byte[] s) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(s);
            return DatatypeConverter.printHexBinary(md.digest());
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

    public static <T> double listSimilarity(java.util.List<T> first, java.util.List<T> second) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        if (first.equals(second)) return 1;
        if (first.isEmpty() && second.isEmpty()) return 1;
        double matches = 0;
        int i = 0;
        if (first.size() >= second.size()) {
            for (T t : second) {
                if (t.equals(first.get(i))) matches++;
                i++;
            }
            return matches / first.size();
        } else {
            for (T t : first) {
                if (t.equals(second.get(i))) matches++;
                i++;
            }
            return matches / second.size();
        }
    }

}
