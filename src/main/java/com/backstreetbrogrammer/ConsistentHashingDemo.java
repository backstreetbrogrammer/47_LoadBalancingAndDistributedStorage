package com.backstreetbrogrammer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashingDemo {

    private final TreeMap<Long, String> ring;
    private final int numberOfReplicas;
    private final MessageDigest md;

    public ConsistentHashingDemo(final int numberOfReplicas) throws NoSuchAlgorithmException {
        this.ring = new TreeMap<>();
        this.numberOfReplicas = numberOfReplicas;
        this.md = MessageDigest.getInstance("MD5");
    }

    public void addServer(final String server) {
        for (int i = 0; i < numberOfReplicas; i++) {
            final long hash = generateHash(server + i);
            ring.put(hash, server);
        }
    }

    public void removeServer(final String server) {
        for (int i = 0; i < numberOfReplicas; i++) {
            final long hash = generateHash(server + i);
            ring.remove(hash);
        }
    }

    public String getServer(final String key) {
        if (ring.isEmpty()) {
            return null;
        }
        long hash = generateHash(key);
        if (!ring.containsKey(hash)) {
            final SortedMap<Long, String> tailMap = ring.tailMap(hash);
            hash = tailMap.isEmpty() ? ring.firstKey() : tailMap.firstKey();
        }
        return ring.get(hash);
    }

    private long generateHash(final String key) {
        md.reset();
        md.update(key.getBytes());
        final byte[] digest = md.digest();
        final long hash = ((long) (digest[3] & 0xFF) << 24) |
                ((long) (digest[2] & 0xFF) << 16) |
                ((long) (digest[1] & 0xFF) << 8) |
                ((long) (digest[0] & 0xFF));
        return hash;
    }

    public static void main(final String[] args) throws NoSuchAlgorithmException {
        final ConsistentHashingDemo consistentHashing = new ConsistentHashingDemo(3);
        consistentHashing.addServer("server1");
        consistentHashing.addServer("server2");
        consistentHashing.addServer("server3");

        System.out.printf("key1: is present on server: %s%n", consistentHashing.getServer("key1"));
        System.out.printf("key67890: is present on server: %s%n", consistentHashing.getServer("key67890"));

        consistentHashing.removeServer("server1");
        System.out.println("After removing server1");

        System.out.printf("key1: is present on server: %s%n", consistentHashing.getServer("key1"));
        System.out.printf("key67890: is present on server: %s%n", consistentHashing.getServer("key67890"));
    }

}
