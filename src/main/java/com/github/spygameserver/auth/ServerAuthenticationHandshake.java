package com.github.spygameserver.auth;

import com.github.spygameserver.util.ExceptionHandling;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServerAuthenticationHandshake {

    /*
    private static final SRP6IntegerVariable N = new SRP6CustomIntegerVariable(
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

    private static final SRP6IntegerVariable g = new SRP6CustomIntegerVariable(
            BigInteger.valueOf(2)
    );

    private static final ImmutableMessageDigest imd = getSHA256Hash();

    private static ImmutableMessageDigest getSHA256Hash() {
        try {
            return new ImmutableMessageDigest(
                    MessageDigest.getInstance("SHA-256")
            );
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN; */

    private final Socket playerConnection;
    private final Connection databaseConnection;
    private final SecureRandom secureRandom;

    public ServerAuthenticationHandshake(Socket playerConnection, Connection databaseConnection) {
        this.playerConnection = playerConnection;
        this.databaseConnection = databaseConnection;
        this.secureRandom = new SecureRandom();
    }

    public String receiveHello(int playerId) {
        String query = "SELECT salt, verifier FROM test_player_authentication WHERE player_id=?";

        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(query)) {
            preparedStatement.setInt(1, playerId);

            ResultSet authenticationDataResultSet = preparedStatement.executeQuery();
            boolean hasAuthenticationRecord = authenticationDataResultSet.next();
            ExceptionHandling.closeQuietly(authenticationDataResultSet);

            if (!hasAuthenticationRecord) {
                return "bad_record_mac";
            }

            return "Success!";
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return "Could not connect to database.";
    }

}
