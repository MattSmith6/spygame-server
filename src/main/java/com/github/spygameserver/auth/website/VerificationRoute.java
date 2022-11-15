package com.github.spygameserver.auth.website;

import org.json.JSONObject;
import org.json.JSONTokener;
import spark.Request;
import spark.Response;
import spark.Route;

public abstract class VerificationRoute implements Route {

    private final String STATUS_FIELD = "status";

    @Override
    public Object handle(Request request, Response response) throws Exception {
        JSONTokener requestBodyTokener = new JSONTokener(request.body());
        JSONObject requestBody = new JSONObject(requestBodyTokener);

        if (!GoogleEmailVerifier.isCSUNEmailVerified(requestBody)) {
            return getErrorObject("Unable to verify your CSUN email.");
        }

        return handleAdditional(requestBody, response).toString();
    }

    public abstract JSONObject handleAdditional(JSONObject requestBody, Response response);

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
