package com.github.spygameserver.auth.website;

import org.json.JSONObject;

public class GoogleEmailVerifier {

    public static boolean isCSUNEmailVerified(JSONObject postBody) {
        // TODO: Grab email and google token from post body
        String email = postBody.getString("email");
        String token = postBody.getString("token");

        if (!isEmailFromCSUN(email)) {
            return false;
        }

        // TODO: Process the Google token

        return true;
    }

    private static boolean isEmailFromCSUN(String email) {
        String domainName = email.split("@")[1];
        return domainName.endsWith("csun.edu");
    }

}
