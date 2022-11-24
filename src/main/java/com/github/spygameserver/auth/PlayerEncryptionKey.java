package com.github.spygameserver.auth;

import org.json.JSONObject;
import org.json.JSONTokener;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PlayerEncryptionKey {

    private static final String CIPHER_TYPE = "AES";

    private int playerId = -1;
    private SecretKey secretKey = null;
    private Cipher cipher = null;

    public void initialize(int playerId, byte[] premasterSecret) {
        this.playerId = playerId;
        this.secretKey = new SecretKeySpec(premasterSecret, CIPHER_TYPE);

        Cipher cipher = null;

        try {
            cipher = Cipher.getInstance(CIPHER_TYPE);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }

        this.cipher = cipher;
    }

    public boolean isInitialized() {
        return playerId != -1;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String encryptJSONObject(JSONObject jsonObject) {
        if (!isInitialized()) {
            throw new IllegalStateException("Cannot encrypt without an initialized encryption key.");
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] objectBytesToEncrypt = jsonObject.toString().getBytes();
            byte[] encryptedBytes = cipher.doFinal(objectBytesToEncrypt);

            return new String(encryptedBytes);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public JSONObject decryptJSONObject(String encryptedJSONObject) {
        if (!isInitialized()) {
            throw new IllegalStateException("Cannot decrypt without an initialized encryption key.");
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] encryptedBytes = encryptedJSONObject.getBytes();
            byte[] decryptedObjectBytes = cipher.doFinal(encryptedBytes);

            String stringJSONObject = new String(decryptedObjectBytes);
            JSONTokener jsonTokener = new JSONTokener(stringJSONObject);

            return new JSONObject(jsonTokener);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
