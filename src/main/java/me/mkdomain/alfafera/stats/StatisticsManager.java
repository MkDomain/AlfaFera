package me.mkdomain.alfafera.stats;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Általános statisztikákat kezel
 */
public class StatisticsManager {

    private long servedData;
    private long cachedData;

    public StatisticsManager() {
        try {
            //Statisztikák betöltése (ha van honnan)
            if (Files.exists(Paths.get("statistics.info"))) {
                DataInputStream in = new DataInputStream(new FileInputStream("statistics.info"));
                servedData = in.readLong();
                cachedData = in.readLong();
            }
            System.out.println("Statisztikák betöltve! (Kiszolgált: " + servedData + ",  Cachelt: " + cachedData + ")");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Elmenti a statisztikákat
     */
    public void save() {
        try {
            File out = new File("statistics.info");
            if (!out.exists()) out.createNewFile();
            DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(out));
            dataOut.writeLong(servedData);
            dataOut.writeLong(cachedData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Növeli a kiszolgált adatmennyiséget
     *
     * @param by Ennyivel növeli
     */
    public void incrementServedData(int by) {
        servedData += by;
    }

    /**
     * Növeli a kiszolgált cachelt adatmennyiséget
     *
     * @param by Ennyivel növeli
     */
    public void incrementCachedData(int by) {
        cachedData += by;
    }

    /**
     * @return Az összes kiszolgált adat (byte-ban)
     */
    public long getServedData() {
        return servedData;
    }

    /**
     * @return Az összes kiszolgált cachelt adat (byte-ban)
     */
    public long getCachedData() {
        return cachedData;
    }
}
