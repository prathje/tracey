package de.patrickrathje.tracey.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import de.patrickrathje.tracey.model.Group;
import de.patrickrathje.tracey.model.Secret;

public class InvitationParser {

    public static Group parseHexInvitationString(String hexInvitationString) {

        // we convert the hexString into a byte-stream
        byte[] bytes = HexConverter.HexStringToByteArray(hexInvitationString);

        // we will parse this hex string into the following parts: 2 bytes header, 2 bytes version, 8 bytes UNIX timestamp, <Secret.SECRET_LENGTH> bytes of secret, the rest simply corresponds to the name of the group
        // this means that our bytes array needs to contain at least 2+2+8+Secret.SECRET_LENGTH bytes
        // TODO: This should later be handled after the version was read
        if (bytes.length < (2+2+8+Secret.SECRET_LENGTH)) {
            return null;
        }

        ByteBuffer bb = ByteBuffer.wrap(bytes);

        // check if the header is F000 - how creative...
        if (bb.get() != (byte)0xF0 || bb.get() != (byte)0x00) {
            return null;
        }

        short version = bb.getShort();

        // only version 1 is currently supported
        if (version != 1) {
            return null;
        }

        long timeStamp = bb.getLong();
        Date date = new Date(timeStamp);

        byte[] secretBytes = new byte[Secret.SECRET_LENGTH];
        bb.get(secretBytes);
        Secret secret = Secret.fromByteArray(secretBytes);

        byte[] textBytes = new byte[bb.remaining()];
        bb.get(textBytes);
        String text = new String(textBytes, StandardCharsets.UTF_8);

        if (secret != null) {
            return new Group(date, text, secret);
        }
        return null;
    }

    public String toHexInvitationString(Group group) {
        short version = 1;

        String text = group.getText();
        Date date = group.getDateCreated();
        Secret secret = group.getSecret();

        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] bytes = new byte[2+2+8+Secret.SECRET_LENGTH+textBytes.length];

        ByteBuffer bb = ByteBuffer.wrap(bytes);

        // set header
        bb.put((byte) 0xF0);
        bb.put((byte) 0x00);

        // set version
        bb.putShort(version);

        // set timestamp
        bb.putLong(date.getTime());

        // put secret
        bb.put(secret.toByteArray());

        // add textBytes
        bb.put(textBytes);

        return HexConverter.ByteArrayToHexString(bytes);
    }

}
