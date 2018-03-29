package com.forzo.uberriderappclone.common;


import com.forzo.uberriderappclone.remote.IGoogleAPI;
import com.forzo.uberriderappclone.remote.RetrofitClient;

/**
 * Created by Leon on 22-03-18.
 */

public class Common {
    public static final String baseURL = "https://maps.googleapis.com";

    public static IGoogleAPI getIGoogleAPI() {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
}
