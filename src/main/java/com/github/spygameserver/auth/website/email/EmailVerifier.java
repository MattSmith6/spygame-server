package com.github.spygameserver.auth.website.email;

import org.json.JSONObject;

public class EmailVerifier {

    public static boolean isEmailFromCSUN(JSONObject jsonObject) {
        if (!jsonObject.has("email")) {
            return false;
        }

        return isEmailFromCSUN(jsonObject.getString("email"));
    }

    public static boolean isEmailFromCSUN(String email) {
        if (email == null) {
            return false;
        }

        String domainName = email.split("@")[1];
        return domainName.endsWith("csun.edu");
    }

}
