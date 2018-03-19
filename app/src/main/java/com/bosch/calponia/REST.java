package com.bosch.calponia;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class REST extends Application  {

    private static final String LOGTAG = "REST";

    // URLS
    public static final String LOGIN = "https://api-internal.calponia-bcx.de/api/users/login";
    public static final String PROJECTS = "https://api-internal.calponia-bcx.de/api/users/%s/projects";
    public static final String EQUIPMENTS = "https://api-internal.calponia-bcx.de/api/projects/%s/equipment";
    public static final String VEHICLE = "https://api-internal.calponia-bcx.de/api/vehicles/%s";
    public static final String EQUIPMENT = "https://api-internal.calponia-bcx.de/api/equipment/%s";
    public static final String RELAY = "https://iot.calponia-bcx.de/%s";

    // Authentication
    private static JSONObject accessToken = null;

    public static JSONObject getAccessToken() {
        return accessToken;
    }
    public static void setAccessToken(JSONObject token) {
        accessToken = token;
    }

    public static String vehicle = null;
    public static void setVehicle (String id) {
        vehicle = id;
//        Call(new Request.Params() {
//            @Override
//            void onPostExecute (JSONObject data) {
//
//            }
//        });
    }
    public static void setEquipment (String id) {
        Call(new Request.Params(String.format(EQUIPMENT, id)) {
            @Override
            void onPostExecute (JSONObject data) {
                try {
                    final JSONObject equipmentQR = data.getJSONObject("response");
                    REST.Call(new Request.Params(String.format(REST.EQUIPMENTS, equipmentQR.getString("projectId"))) {
                        @Override
                        void onPostExecute(JSONObject data) {
                            try {
                                if (!IsError(data)) {
                                    JSONArray result = data.getJSONArray("response");
                                    for (int i=0; i < result.length(); i++) {
                                        JSONObject equipment = result.getJSONObject(i);
                                        if (equipment.getString("id").equals(equipmentQR.getString("id"))) {
                                            setAccessToken(equipment.getJSONObject("accessToken"));
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.i(LOGTAG, e.getMessage());
                            }
                        }
                    });
                } catch (Exception e) {}
            }
        });
    }

    // Helpers
    private static long LastRequest = 0;

    public static boolean IsError (JSONObject data) {
        if (data.has("error")) {
            return true;
        }
        return false;
    }

    public static void Call (Request.Params params) {
        // limit request to every second
        if (params.URL.startsWith(RELAY) && LastRequest > System.currentTimeMillis() - 1000) return;

        if (accessToken != null) {
            if (params.headers == null) {
                params.headers = new HashMap<>();
            }
            try {
                Log.i(LOGTAG, "Authorization: " + accessToken.getString("id"));
                params.headers.put("Authorization", accessToken.getString("id"));
            } catch (Exception e) {}
        }
        Request req = new Request();
        req.execute(params);
        LastRequest = System.currentTimeMillis();
    }

    public static void Vehicle (Location location) {
        JSONObject data = new JSONObject();
        try {
            data.put("lat", location.getLatitude());
            data.put("long", location.getLongitude());
            data.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {}
        Log.i(LOGTAG, data.toString());
        Call(new Request.Params(String.format(RELAY, "gps"), "POST", data));
    }
}
