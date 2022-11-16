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
import com.github.spygameserver.database.table.AuthenticationTable;
import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

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

    private final AuthenticationTable authenticationTable;
    private final ConnectionHandler connectionHandler;

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
        this.authenticationTable = authenticationDatabase.getAuthenticationTable();
        this.connectionHandler = authenticationDatabase.getNewConnectionHandler(true);

        this.secureRandom = new SecureRandom();
    }

    private void putSRP6Variable(JSONObject jsonObject, String path, SRP6IntegerVariable srp6IntegerVariable) {
        jsonObject.put(path, srp6IntegerVariable.bytes(BYTE_ORDER).asArray());
    }

    public JSONObject respondToHello(int playerId) {
        JSONObject responseToHello = new JSONObject();

        PlayerAuthenticationData playerAuthenticationData = authenticationTable
                .getPlayerAuthenticationRecord(connectionHandler, playerId);

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
            responseToHello.put("N", Base64.getEncoder().encodeToString(N.bytes(BYTE_ORDER).asArray()));
            responseToHello.put("g", Base64.getEncoder().encodeToString(g.bytes(BYTE_ORDER).asArray()));
            responseToHello.put("s", Base64.getEncoder().encodeToString(s.asArray()));
            responseToHello.put("B", Base64.getEncoder().encodeToString(B.bytes(BYTE_ORDER).asArray()));
        } catch (SRP6Exception ex) {
            responseToHello.put("error", "bad_record_mac");
        }

        return responseToHello;
    }

    public JSONObject respondToKeyExchange(SRP6IntegerVariable A, Bytes M1) {
        JSONObject jsonObject = new JSONObject();

        try {
            SRP6IntegerVariable u = new SRP6ScramblingParameter(IMD, A, B, N, BYTE_ORDER);
            S = new SRP6ServerSharedSecret(N, A, v, u, b);
            Bytes K = new SRP6SessionKey(IMD, S, BYTE_ORDER);
            Bytes sM1 = new SRP6ClientSessionProof(IMD, N, g, I, s, A, B, K, BYTE_ORDER);

            if (!(sM1.equals(M1))) {
                throw new SRP6Exception("Client proof mismatch!");
            }

            M2 = new SRP6ServerSessionProof(IMD, N, A, sM1, K, BYTE_ORDER);

            // Server should send M2
            jsonObject.put("M2", Base64.getEncoder().encodeToString(M2.asArray()));
        } catch (SRP6Exception ex) {
            jsonObject.put("error", "Client proof mismatch!");
        }

        return jsonObject;
    }

    public byte[] getPremasterSecret() {
        return S.bytes(BYTE_ORDER).asArray();
    }

}
