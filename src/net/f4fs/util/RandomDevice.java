package net.f4fs.util;

import java.util.Random;


/**
 * RandomDevice for global usage of pseudo-random numbers.
 *
 * Usage:
 *
 * RandomDevice.INSTANCE.nextLong
 *
 * Created by samuel on 31.03.15.
 */
public enum RandomDevice {
    // To provide reproducible outcomes.
    INSTANCE(new Random(System.currentTimeMillis()));

    private Random rand;

    private RandomDevice(Random rand) {
        this.rand = rand;
    }

    /**
     * Returns a pseudorandom, uniformly distributed int value between 0 (inclusive)
     * and the specified value (exclusive), drawn from this random number generator's sequence.
     *
     * @param n Bound
     * @return 0 <= value < n
     */
    public synchronized int nextInt(int n) {
        return rand.nextInt(n);
    }

    /**
     * Returns a pseudorandom, uniformly distributed int value between 0 (inclusive)
     * and the specified value (exclusive), drawn from this random number generator's sequence.
     *
     * @param n Bound
     * @return 0 <= value < n
     */
    public synchronized long nextLong(long n) {
        return rand.nextLong();
    }

    public Random getRand() {
        return rand;
    }
}
