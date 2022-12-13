package com.github.spygameserver.auth;

import com.github.glusk.caesar.Bytes;
import com.github.glusk.caesar.Hex;
import com.github.glusk.caesar.PlainText;
import com.github.glusk.caesar.hashing.ImmutableMessageDigest;
import com.github.glusk.srp6_variables.SRP6ClientSessionProof;
import com.github.glusk.srp6_variables.SRP6CustomIntegerVariable;
import com.github.glusk.srp6_variables.SRP6Exception;
import com.github.glusk.srp6_variables.SRP6IntegerVariable;
import com.github.glusk.srp6_variables.SRP6Multiplier;
import com.github.glusk.srp6_variables.SRP6RandomEphemeral;
import com.github.glusk.srp6_variables.SRP6ScramblingParameter;
import com.github.glusk.srp6_variables.SRP6ServerPublicKey;
import com.github.glusk.srp6_variables.SRP6ServerSessionProof;
import com.github.glusk.srp6_variables.SRP6ServerSharedSecret;
import com.github.glusk.srp6_variables.SRP6SessionKey;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.table.PlayerAuthenticationTable;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * A class designed to respond to the SRP-6 handshake initiated by the client. In the README of the SRP-6 Variables
 * GitHub, a lot of the code used in this class was provided for public use. See: https://github.com/Glusk/srp6-variables
 *
 * Additionally, many variables may be named singular letters with no comments. Since SRP-6 is a mathematical proof,
 * these variables have meaning, a full list of the variable definitions can be found here: https://www.rfc-editor.org/rfc/rfc5054#section-2.1
 */
public class ServerAuthenticationHandshake {

    public static final SRP6IntegerVariable N = new SRP6CustomIntegerVariable(
            new Hex(
                    "EEAF0AB9 ADB38DD6 9C33F80A FA8FC5E8 60726187 75FF3C0B"
                            + "9EA2314C 9C256576 D674DF74 96EA81D3 383B4813 D692C6E0"
                            + "E0D5D8E2 50B98BE4 8E495C1D 6089DAD1 5DC7D7B4 6154D6B6"
                            + "CE8EF4AD 69B15D49 82559B29 7BCF1885 C529F566 660E57EC"
                            + "68EDBC3C 05726CC0 2FD4CBF4 976EAA9A FD5138FE 8376435B"
                            + "9FC61D2F C0EB06E3"
            ),
            ByteOrder.BIG_ENDIAN
    );

    public static final SRP6IntegerVariable g = new SRP6CustomIntegerVariable(
            BigInteger.valueOf(2)
    );

    public static final ImmutableMessageDigest IMD = getSHA256Hash();

    public static ImmutableMessageDigest getSHA256Hash() {
        try {
            return new ImmutableMessageDigest(
                    MessageDigest.getInstance("SHA-256")
            );
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;

    private final AuthenticationDatabase authenticationDatabase;
    private final PlayerAuthenticationTable authenticationTable;

    private final SecureRandom secureRandom;

    private final PlainText I;
    private Bytes s;
    private SRP6IntegerVariable v;

    private SRP6IntegerVariable b;
    private SRP6IntegerVariable k;
    private SRP6IntegerVariable B;
    private Bytes M2;
    private SRP6IntegerVariable S;

    public ServerAuthenticationHandshake(String username, AuthenticationDatabase authenticationDatabase) {
        this.I = new PlainText(username);

        this.authenticationDatabase = authenticationDatabase;
        this.authenticationTable = authenticationDatabase.getAuthenticationTable();

        this.secureRandom = new SecureRandom();
    }

    /**
     * Puts the SRP6-Variable into the JSONObject at the provided path as a String of Base64 encoded bytes
     * @param jsonObject the JSONObject to insert the SRP6 variable
     * @param path the path at which to insert the SRP6 variable
     * @param srp6IntegerVariable the variable to be inserted
     */
    private void putSRP6Variable(JSONObject jsonObject, String path, SRP6IntegerVariable srp6IntegerVariable) {
        putSRP6Bytes(jsonObject, path, srp6IntegerVariable.bytes(BYTE_ORDER));
    }

    /**
     * Puts SRP6-Variable Bytes into the JSONObject at the provided path as a String of Base64 encoded bytes
     * @param jsonObject the JSONObject to insert the SRP6 variable
     * @param path the path at which to insert the SRP6 variable
     * @param bytes the variable to be inserted
     */
    private void putSRP6Bytes(JSONObject jsonObject, String path, Bytes bytes) {
        jsonObject.put(path, Base64.getEncoder().encodeToString(bytes.asArray()));
    }

    /**
     * Gets a JSONObject response to the player hello, first stage of communication. The server responds with the
     * constant N and g variables, as well as the salt (s) for the player and calculated B.
     * @param playerId the player id for the player hello
     * @return the JSONObject response based on the fetched player id
     */
    public JSONObject respondToHello(int playerId) {
        JSONObject responseToHello = new JSONObject();

        ConnectionHandler connectionHandler = authenticationDatabase.getNewConnectionHandler(true);
        PlayerAuthenticationData playerAuthenticationData = authenticationTable
                .getPlayerAuthenticationRecord(connectionHandler, playerId);

        // If the player's authentication record does not exist, we should respond with an error
        if (playerAuthenticationData == null) {
            responseToHello.put("error", "bad_record_mac");

            return responseToHello;
        }

        // Most of this code taken directly from Glusk SRP6-Variables repository, in the example layout
        // This was a big portion of why I chose this library, it did most of the implementation already
        try {
            // lookup and fetch the record by I -> <I, s, v>

            s = playerAuthenticationData.getSalt();
            v = playerAuthenticationData.getVerifier();

            b = new SRP6RandomEphemeral(secureRandom, -1, N);
            k = new SRP6Multiplier(IMD, N, g, BYTE_ORDER);
            B = new SRP6ServerPublicKey(N, g, k, v, b);

            // Server should respond with N, g, s, and B in the JSONObject
            putSRP6Variable(responseToHello, "N", N);
            putSRP6Variable(responseToHello, "g", g);
            putSRP6Bytes(responseToHello, "s", s);
            putSRP6Variable(responseToHello, "B", B);
        } catch (SRP6Exception ex) {
            // If any error was generated when calculating any of the SRP6 variables, then there is an invalid record here
            responseToHello.put("error", "bad_record_mac");
        }

        return responseToHello;
    }

    /**
     * Gets the JSONObject response for the client key exchange. This uses the A and M1 variables sent by the client
     * to verify that the server can reproduce the client's M1 message proof. On a successful handshake, this returns M2,
     * which the client verifies to complete the handshake process.
     *
     * @param A the SRP6 variable A, used to generate the M1 proof on the client side
     * @param M1 the SRP6 message proof from the client that needs to be verified by the server, using A
     *
     * @return the JSONObject response for the client key exchange
     */
    public JSONObject respondToKeyExchange(SRP6IntegerVariable A, Bytes M1) {
        JSONObject jsonObject = new JSONObject();

        try {
            // This code is again taken from the SRP6-Variables GitHub to make for easier implementation, using a library resource
            SRP6IntegerVariable u = new SRP6ScramblingParameter(IMD, A, B, N, BYTE_ORDER);
            S = new SRP6ServerSharedSecret(N, A, v, u, b);
            Bytes K = new SRP6SessionKey(IMD, S, BYTE_ORDER);
            Bytes sM1 = new SRP6ClientSessionProof(IMD, N, g, I, s, A, B, K, BYTE_ORDER);

            if (!(sM1.equals(M1))) {
                throw new SRP6Exception("Client proof mismatch!");
            }

            M2 = new SRP6ServerSessionProof(IMD, N, A, sM1, K, BYTE_ORDER);

            // Server should send M2 as the response
            putSRP6Bytes(jsonObject, "M2", M2);
        } catch (SRP6Exception ex) {
            // If there were any exceptions when generating the proof, the client has invalid credentials
            jsonObject.put("error", "Client proof mismatch!");
        }

        return jsonObject;
    }

    /**
     * If the message proof of the client was verified, then the server can use S as the shared premaster secret
     * that can be used to encrypt and decrypt data sent for this session.
     *
     * @return the byte array version of the premaster secret
     */
    public byte[] getPremasterSecret() {
        return S.bytes(BYTE_ORDER).asArray();
    }

}
