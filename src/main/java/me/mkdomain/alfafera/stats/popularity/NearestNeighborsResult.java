package me.mkdomain.alfafera.stats.popularity;

/**
 * A k-NN algoritmus által használt értékeket tárolja
 *
 * @param <T> Az összehasonlított osztályok fajtája
 */
public class NearestNeighborsResult<T> {

    private final T result;
    private final double distance;

    /**
     * @param result   Az adott osztály
     * @param distance Távolság a fő elemtől
     */
    public NearestNeighborsResult(T result, double distance) {
        this.result = result;
        this.distance = distance;
    }

    public T getResult() {
        return result;
    }

    public double getDistance() {
        return distance;
    }
}
