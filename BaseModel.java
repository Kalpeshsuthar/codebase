package com.psenergy.models;
/*****************************************************************************************
 * Copyright (c) PhaseAlpha, LLC.  All rights reserved.
 * File: BaseModel.java
 * Created By: Ravindra
 * Created On: 13/02/2019
 * Description: Parent class of all models, Some commonly used methods defined here
 * Modification By: NA
 * Modified On: NA
 * Modification Details: NA
 *****************************************************************************************/
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.Gson;
import com.psenergy.R;
import com.psenergy.api.RequestListener;
import com.psenergy.enums.ResponseStatus;
import com.psenergy.utils.Utils;

import java.util.ArrayList;
import java.util.List;


public class BaseModel {
    private ProgressDialog pDialog;
    private Context mContext;
    protected String unknownError;

    public BaseModel(Context mContext) {
        this.mContext = mContext;
        this.unknownError = mContext.getResources().getString(R.string.error);
    }
    public BaseModel(Context mContext,RequestListener requestListener) {
        this.mContext = mContext;
        this.unknownError = mContext.getResources().getString(R.string.error);
    }
    /**
     * Display progressbar while api calling
     * */
    protected  void showProgress() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(mContext);
            pDialog.setCancelable(false);
            pDialog.setMessage(Utils.getAppKeyValue(mContext,R.string.msgApiLoader));
        }
        if (!pDialog.isShowing())
            pDialog.show();
    }

    protected void hideProgress() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.cancel();
    }

    /**
     * Checking for all possible internet providers
     **/
    protected boolean isConnectingToInternet() {
        final ConnectivityManager connectivity = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo activeNetwork = connectivity.getActiveNetworkInfo();
            if (activeNetwork != null) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    return true;
                } else return activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return false;
    }

    public Object getObject(Object object, final Class type) {
        if (object == null)
            return null;
        Gson gson = new Gson();
        String gsonString = gson.toJson(object);
        Object objectToSend = null;
        final Class clazz = type.getClass();
        if (object instanceof ArrayList) {
            List<Object> objects = new ArrayList<>();
            for (Object objectA : (ArrayList) object) {
                gsonString = gson.toJson(objectA);
                objectToSend = gson.fromJson(gsonString, type);
                objects.add(objectToSend);
            }
            return objects;
        } else {
            objectToSend = gson.fromJson(gsonString, type);
        }
        return objectToSend;
    }

     public boolean isSuccessResponse(String responseStatus) {
        return responseStatus.equalsIgnoreCase(ResponseStatus.SUCCESS.getType());

    }


}
