package com.github.spygameserver.auth.website;

import org.json.JSONObject;

import java.util.Map;

public class GoogleEmailVerifier {

    public static boolean isCSUNEmailVerified(JSONObject jsonObject) {
        // TODO: Grab email and google token from post body
        String email = jsonObject.getString("email");
        // String token = jsonObject.getString("token");

        if (!isEmailFromCSUN(email)) {
            return false;
        }

        // TODO: Process the Google token

        return true;
    }

    public static boolean isEmailFromCSUN(String email) {
        if (email == null) {
            return false;
        }

        String domainName = email.split("@")[1];
        return domainName.endsWith("csun.edu");
    }

}
