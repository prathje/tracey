package de.patrickrathje.tracey.model;

import java.util.Random;

public class Secret {

    public static final int SECRET_LENGTH = 64;

    private byte[] bytes = new byte[Secret.SECRET_LENGTH];

    public Secret() {
        // we generate a new random key
        new Random().nextBytes(bytes);
    }

    private Secret(byte[] tempBytes) {
        for (int i = 0; i < SECRET_LENGTH; i++) {
            this.bytes[i] = tempBytes[i];
        }
    }


    public static Secret fromByteArray(byte[] bytes) {
        if (bytes.length != SECRET_LENGTH) {
            return null;
        }
        else {
            return new Secret(bytes);
        }
    }

    public byte[] toByteArray() {
        byte[] bytesCopy = new byte[Secret.SECRET_LENGTH];
        for (int i = 0; i < SECRET_LENGTH; i++) {
            bytesCopy[i] = this.bytes[i];
        }
        return bytesCopy;
    }
}
