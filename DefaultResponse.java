package com.companyname.models;
/*****************************************************************************************
 * Copyright (c) PhaseAlpha, LLC.  All rights reserved.
 * File: DefaultResponse.java
 * Created By: Auther name
 * Created On: 25/02/2019
 * Description: Default response for apis like cancel order, Reset Password.
 * Modification By: NA
 * Modified On: NA
 * Modification Details: NA
 *****************************************************************************************/

import com.google.gson.annotations.SerializedName;

public class DefaultResponse {

    @SerializedName("result")
    private Object result;

    @SerializedName("message")
    private String message;

    public void setUser(Object user) {
        this.result = user;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @SerializedName("status")
    private String status;

    public Object getResult(){
        return result;
    }

    public String getMessage(){
        return message;
    }

    public String getStatus(){
        return status;
    }

}
