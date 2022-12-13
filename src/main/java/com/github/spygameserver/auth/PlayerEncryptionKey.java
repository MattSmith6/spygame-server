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

/**
 * The encryption key used to decrypt and encrypt packet communication outside of the login phase.
 */
public class PlayerEncryptionKey {

    private static final String CIPHER_TYPE = "AES";

    private int playerId = -1;
    private SecretKey secretKey = null;
    private Cipher cipher = null;

    /**
     * Initializes this object with the player's id and shared premaster secret (used to generate the secret key)
     * @param playerId the player id
     * @param premasterSecret the shared premaster secret
     */
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

    /**
     * Encrypts the JSONObject to a String, using the established encryption key
     * @param jsonObject the JSONObject to encrypt
     * @return the encrypted String form of the JSONObject
     */
    public String encryptJSONObject(JSONObject jsonObject) {
        // Precondition to ensure that we have an established encryption key (successful login) before attempting to use encrypted packets
        if (!isInitialized()) {
            throw new IllegalStateException("Cannot encrypt without an initialized encryption key.");
        }

        try {
            // Initialize the encryption cipher with the provided secret key
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // Convert the JSONObject to its bytes, then encrypt the bytes
            byte[] objectBytesToEncrypt = jsonObject.toString().getBytes();
            byte[] encryptedBytes = cipher.doFinal(objectBytesToEncrypt);

            // Return the encrypted bytes in the form of a String
            return new String(encryptedBytes);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * Decrypts the provided encrypted JSONObject, in String form, using the established encryption key
     * @param encryptedJSONObject the encrypted JSONObject, in String form
     * @return the decrypted JSONObject
     */
    public JSONObject decryptJSONObject(String encryptedJSONObject) {
        // Precondition to ensure that we have an established encryption key (successful login) before attempting to use encrypted packets
        if (!isInitialized()) {
            throw new IllegalStateException("Cannot decrypt without an initialized encryption key.");
        }

        try {
            // Change the mode to decrypt with the provided key
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Convert the String to a byte array, then decrypt into its bytes
            byte[] encryptedBytes = encryptedJSONObject.getBytes();
            byte[] decryptedObjectBytes = cipher.doFinal(encryptedBytes);

            // Create a string from the decrypted bytes, then parse the JSON with the tokener
            String stringJSONObject = new String(decryptedObjectBytes);
            JSONTokener jsonTokener = new JSONTokener(stringJSONObject);

            // Return the parsed JSON in JSONObject form
            return new JSONObject(jsonTokener);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
