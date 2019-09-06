package com.companyname.models;
/*****************************************************************************************
 * Copyright (c) company name.  All rights reserved.
 * File: ApiCallingForAccounts.java
 * Created By: Auther name
 * Created On: 13/02/2019
 *
 * Description: Login and splash api calling is done from here.
 * Modification By: Auther name
 * Modified On: 14/02/2019
 * Modification Details: NA
 *
 * Description: Added code for Google PlaceApi calling.
 * Modification By: Auther name
 * Modified On: 15/02/2019
 * Modification Details: NA
 * Description: Added code for Google PlaceApi calling.
 *
 * Modification By: Auther name
 * Modified On: 15/02/2019
 * Modification Details: NA
 *
 *  Description: Order Entry Form related api calling
 *  * Modification By: Auther name
 *  * Modified On: 25/02/2019
 *  * Modification Details: NA
 *
 *  Description: Change in Login Api for New Structure
 *  Modification By: Auther name
 *  Modified On: 10/04/2019
 *  Modification Details: NA
 *****************************************************************************************/

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.BuildConfig;
import com.PSEApplication;
import com.R;
import com.api.HttpGetWithEntity;
import com.api.RequestCode;
import com.api.RequestListener;
import com.enums.ResponseStatus;
import com.models.fueling.DefaultLocationResponse;
import com.models.fueling.TankLocationList;
import com.models.login.LoginResponse;
import com.models.login.User;
import com.utils.BaseConstant;
import com.utils.Debug;
import com.utils.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiCallingForAccounts extends BaseModel {
    private Activity mContext;
    private RequestListener requestListener;
    private ProgressDialog pDialog;
    protected String unknownError;

    public ApiCallingForAccounts(Activity mContext, RequestListener requestListener) {
        super(mContext, requestListener);
        this.mContext = mContext;
        this.requestListener = requestListener;
        this.unknownError = mContext.getResources().getString(R.string.error);
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

    /**
     * Forgott Password api calling
     *
     * @param params Hashmap of paramters
     */
    public void callForgottPasswordApi(Map<String, String> params) {
        final RequestCode requestCode = RequestCode.FORGOTT_PASSWORD;
        if (!isConnectingToInternet()) {
            requestListener.onRetryRequest(requestCode);
            return;
        }
        showProgress();
        Call<DefaultResponse> forgottPassword = PSEApplication.getInstance().apiServiceAccounts.forgottPassword(params);
        forgottPassword.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(@NonNull Call<DefaultResponse> call, @NonNull Response<DefaultResponse> response) {
                hideProgress();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        DefaultResponse defaultResponse = response.body();
                        if (isSuccessResponse(defaultResponse.getStatus())) {
                            requestListener.onComplete(requestCode, defaultResponse, "");
                        } else {
                            requestListener.onRequestError(defaultResponse.getMessage(), response.code(), requestCode);
                        }
                    } else {
                        requestListener.onRequestError(unknownError, response.code(), requestCode);
                    }
                } else {
                    requestListener.onRequestError(unknownError, response.code(), requestCode);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DefaultResponse> call, @NonNull Throwable t) {
                hideProgress();
                requestListener.onException(unknownError, requestCode);
            }
        });
    }


    /**
     * Reset Password api calling
     *
     * @param currentPassword the current password
     * @param newPassword     the new password
     * @param email           the email registered
     */
    public void callResetPasswordApi(final String currentPassword, final String newPassword, final String email) {
        final RequestCode requestCode = RequestCode.FORGOTT_PASSWORD;
        if (!isConnectingToInternet()) {
            requestListener.onRetryRequest(requestCode);
            return;
        }
        showProgress();
        Call<DefaultResponse> forgottPassword = PSEApplication.getInstance().apiServiceAccounts.resetPassWord(email, currentPassword, newPassword);
        forgottPassword.enqueue(new Callback<DefaultResponse>() {
            @Override
            public void onResponse(@NonNull Call<DefaultResponse> call, @NonNull Response<DefaultResponse> response) {
                hideProgress();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        DefaultResponse defaultResponse = response.body();
                        if (isSuccessResponse(defaultResponse.getStatus())) {
                            requestListener.onComplete(requestCode, defaultResponse, "");
                        } else {
                            requestListener.onRequestError(defaultResponse.getMessage(), response.code(), requestCode);
                        }
                    } else {
                        requestListener.onRequestError(unknownError, response.code(), requestCode);
                    }
                } else {
                    requestListener.onRequestError(unknownError, response.code(), requestCode);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DefaultResponse> call, @NonNull Throwable t) {
                hideProgress();
                requestListener.onException(unknownError, requestCode);
            }
        });
    }


    /**
     * Reset password api.
     * Api for Changing user password.
     * we have used httpclient as we have to pass body in GET request and we can't do that in retrofit
     *
     * @param paramJson json containing email,old password, new password
     */
    public void callResetPasswordApiOld(String paramJson) {
        String url = BuildConfig.BASE_URL + BaseConstant.BASE_URL_PREFIX + "Account/ChangePassword";
        if (!isConnectingToInternet()) {
            requestListener.onRetryRequest(RequestCode.RESET_PASSWORD);
            return;
        }
        new GetMethodAsyncyTask(paramJson, RequestCode.RESET_PASSWORD).execute(url);
    }


    /**
     * Class is used to call get methods passing body in entity
     */
    public class GetMethodAsyncyTask extends AsyncTask<String, Void, String> {
        String server_response;
        String jsonParam;
        RequestCode requestCode;

        /**
         * Instantiates a new Get method asyncy task.
         *
         * @param paramJson   request body
         * @param requestCode request code
         */
        public GetMethodAsyncyTask(String paramJson, RequestCode requestCode) {
            jsonParam = paramJson;
            this.requestCode = requestCode;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                com.utils.Debug.trace("TAG:", strings[0]);
                Debug.trace("TAG:", jsonParam.toString());

//                Lists.newArrayList(header);
                HttpClient client = new DefaultHttpClient();

                HttpGetWithEntity myGet = new HttpGetWithEntity(strings[0]);
                if (requestCode == RequestCode.GET_ORDERS) {
                    myGet.setHeader("Content-Type", "application/json");
                    myGet.setHeader("UserId", "userId");
                    myGet.setHeader("Password", "password");
                }

                StringEntity se = new StringEntity(jsonParam);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                myGet.setEntity(se);
                HttpResponse response = client.execute(myGet);
                server_response = EntityUtils.toString(response.getEntity());
                server_response = server_response.substring(1, server_response.length() - 1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            hideProgress();
            server_response = server_response.replace("\\", "");
            requestListener.onComplete(requestCode, server_response, "");
            Log.e("CatalogClient", "" + server_response);
        }
    }


    /**
     * Display progressbar while api calling
     */
    protected void showProgress() {
        if (pDialog == null) {
            pDialog = new ProgressDialog(mContext);
            pDialog.setCancelable(false);
            pDialog.setMessage(Utils.getAppKeyValue(mContext, R.string.msgApiLoader));
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

    /**
     * Login API
     * 'Login' api used for login, After getting response from api, data will be sent back to
     * LoginActivity using appropriate Requestlistener method.
     */
    public void callLoginApi(Map<String, String> param, final RequestListener requestListener) {
        final RequestCode requestCode = RequestCode.LOGIN;
        if (!isConnectingToInternet()) {
            requestListener.onRetryRequest(requestCode);
            return;
        }
        showProgress();
        Call<LoginResponse> callLogin = PSEApplication.getInstance().apiServiceAccounts.login(param);
        callLogin.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                hideProgress();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().getStatus().equalsIgnoreCase(ResponseStatus.SUCCESS.getType())) {
                            User loggedInUser = (User) getObject(response.body().getResult(), User.class);
                            requestListener.onComplete(requestCode, loggedInUser, response.body().getMessage());
                        } else {
                            requestListener.onRequestError(response.body().getMessage(), 0, requestCode);
                        }
                    } else {
                        requestListener.onRequestError(unknownError, response.code(), requestCode);
                    }
                } else {
                    requestListener.onRequestError(unknownError, response.code(), requestCode);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                hideProgress();
                requestListener.onException(unknownError, requestCode);
            }
        });
    }

    /*GetTankLocationDetails Api*/
    public void getTankLocationDetails(final boolean showProgressDialog, Map<String, String> param, final RequestListener requestListener) {
        final RequestCode requestCode = RequestCode.TANK_LOCATOIN_DETAILS;
        if (!isConnectingToInternet()) {
            requestListener.onRetryRequest(requestCode);
            return;
        }
        if (showProgressDialog) {
            showProgress();
        }
        Call<DefaultLocationResponse> getTankList = PSEApplication.getInstance().apiServiceBasicNew.getLocationList(param);
        getTankList.enqueue(new Callback<DefaultLocationResponse>() {
            @Override
            public void onResponse(@NonNull Call<DefaultLocationResponse> call, @NonNull Response<DefaultLocationResponse> response) {
                // hideProgress();
                if (showProgressDialog) {
                    hideProgress();
                }
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (isSuccessResponse(response.body().getStatus())) {
                            List<TankLocationList> locationList = (List<TankLocationList>) getObject(response.body().getResult(), TankLocationList.class);
                            requestListener.onComplete(requestCode, locationList, response.body().getMessage());
                        } else {
                            requestListener.onRequestError(response.body().getMessage(), 0, requestCode);
                        }
                    } else {
                        requestListener.onRequestError(unknownError, 0, requestCode);
                    }
                } else {
                    requestListener.onRequestError(unknownError, response.code(), requestCode);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DefaultLocationResponse> call, @NonNull Throwable t) {
                // hideProgress();
                if (showProgressDialog) {
                    hideProgress();
                }
                requestListener.onException(unknownError, requestCode);
            }
        });
    }

}
