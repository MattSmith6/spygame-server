package com.github.spygameserver.auth.website.email;

import org.json.JSONObject;
import spark.Request;
import spark.Response;
import spark.Route;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * The abstract version of a
 */
public abstract class EmailRequiredRoute implements Route {

    private final String STATUS_FIELD = "status";

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("application/json");
        JSONObject jsonObject = parseRequestIntoJSON(request.body());

        if (!EmailVerifier.isEmailFromCSUN(jsonObject)) {
            return getErrorObject("Unable to verify your CSUN email.");
        }

        return handleAdditional(jsonObject, getEmail(jsonObject), response);
    }

    /**
     * Parse the request body into JSON, decoding any special characters escaped in the URL
     * @param body the body of the HTTP POST request
     * @return the representation of the POST parameters in a JSONObject
     */
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

    /**
     * The method to be implemented by subclasses to handle additional features outside of checking email validity.
     * @param jsonObject the HTTP POST request parameters in the form of a JSONObject
     * @param email the email that is already checked to match a valid CSUN domain
     * @param response the response object used by Spark to return errors or append results
     * @return the JSONObject to send to the response object after handling additional features
     */
    public abstract JSONObject handleAdditional(JSONObject jsonObject, String email, Response response);

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
