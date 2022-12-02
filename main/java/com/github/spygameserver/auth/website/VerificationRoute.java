package com.github.spygameserver.auth.website;

import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public abstract class VerificationRoute implements Route {

    private final String STATUS_FIELD = "status";

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        JSONObject jsonObject = parseRequestIntoJSON(request.body());

        if (!GoogleEmailVerifier.isCSUNEmailVerified(jsonObject)) {
            return getErrorObject("Unable to verify your CSUN email.");
        }

        return handleAdditional(jsonObject, response).toString();
    }

    private JSONObject parseRequestIntoJSON(String body) {
        JSONObject jsonObject = new JSONObject();

        for (String keyValuePair : body.split("&")) {

            String[] keyValueSplit = keyValuePair.split("=");
            String key = keyValueSplit[0];
            String value = URLDecoder.decode(keyValueSplit[1], StandardCharsets.UTF_8);

            jsonObject.put(key, value);
        }

        return jsonObject;
    }

    public abstract JSONObject handleAdditional(JSONObject jsonObject, Response response);

    protected String getEmail(JSONObject jsonObject) {
        return jsonObject.getString("email");
    }

    protected JSONObject getErrorObject(String errorMessage) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(STATUS_FIELD, "ERROR");
        jsonObject.put("error", errorMessage);

        return jsonObject;
    }

    protected JSONObject getSuccessObject() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(STATUS_FIELD, "SUCCESS");

        return jsonObject;
    }

}
