* Copyright (c) company name.  All rights reserved.
* File: ApiCallingForAccounts.java
* Created By: Auther name
* Created On: 13/02/2019
*
* Description: SignalR base chat hub class.

using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using Microsoft.AspNet.SignalR;
using companyname.Common;
using companyname.Entity;
using companyname.BusinessLogic;
using System.Data;
using companyname.Lib;
using System.Configuration;
using System.IO;

namespace companyname
{
    /**
     * Chathub class to setup connection, chat related functions for sending and getting messages.
     */
    public class ChatHub : Hub
    {
        public static string emailIDLoaded = "";

        /**
        * Function for connecting the user with chathub using credential and notifing to receiver with status.
        *
        * @param userName   user name 
        * @param email      user email id
        * @param userId     signalr client id 
        */
        #region Connect
        public void Connect(string userName, string email, int userId)
        {
            emailIDLoaded = email;
            var id = Context.ConnectionId;
            int IDINT = 0;
            using (companydbEntities dc = new companydbEntities())
            {
                var item = dc.ChatUserDetails.FirstOrDefault(x => x.userId == userId);
                if (item != null)
                {
                   
                    item.ConnectionId = id;
                    item.status = 1;
                    dc.SaveChanges();

                    IDINT = item.ID;
                    var connectedUsers = chatUserList(userId);
                    var CurrentMessage = unReadMsgList(userId);
                    var userdetails = new ChatUserDetail
                    {
                        userId = item.userId,
                        ID = item.ID,
                        ConnectionId = id,
                        UserName = userName,
                        EmailID = email,
                        status = 1,
                        deviceToken = ""
                    };
                    Clients.Caller.onConnected(userdetails, connectedUsers, CurrentMessage);

                }
                else
                {
                    var Users = dc.ChatUserDetails.Where(x => x.userId == userId).ToList();
                    if (!Users.Any())
                    {
                        var userdetails = new ChatUserDetail
                        {
                            userId = userId,
                            ConnectionId = id,
                            UserName = userName,
                            status = 1,
                            EmailID = email,
                            deviceToken = ""
                        };
                        dc.ChatUserDetails.Add(userdetails);
                        dc.SaveChanges();
                        IDINT = userdetails.ID;

                        userdetails = new ChatUserDetail
                        {
                            ID = IDINT,
                            userId = userId,
                            ConnectionId = id,
                            UserName = userName,
                            status = 1,
                            EmailID = email,
                            deviceToken = ""
                        };
                        var connectedUsers = chatUserList(userId);
                        var CurrentMessage = unReadMsgList(userId);
                        Clients.Caller.onConnected(userdetails, connectedUsers, CurrentMessage);
                    }
                }

                var userDTL = new ChatUserDetail
                {
                    ID = IDINT,
                    ConnectionId = id,
                    UserName = userName,
                    EmailID = email,
                    status = 1,
                    userId = userId,
                    deviceToken = ""

                };

                var connectedUsers1 = chatUserList(userId);
                var CurrentMessage1 = unReadMsgList(userId);

                List<string> conn_list = new List<string>();
                string[] value = chatUserConnectionIdList(userId).Trim().Split(',');
                for (int i = 0; i < value.Count(); i++)
                {
                    conn_list.Add(value[i]);

                }
                Clients.Clients(conn_list).onNewUserConnected(userDTL, connectedUsers1, CurrentMessage1);
            }
        }
        #endregion


        /**
        * Function for getting latest message list based on user id.
        *
        * @param userId     user id  
        */
        public List<messageDetail> getUnReadMsgList(int userId)
        {
            using (companydbEntities dc = new companydbEntities())
            {
                return (from C in dc.ChatPrivateMessageDetails
                        where C.receiverId == userId && C.status == 1
                        select new messageDetail
                        {

                            ID = C.ID,
                            MasterEmailID = C.MasterEmailID,
                            ChatToEmailID = C.ChatToEmailID,
                            senderId = C.senderId,
                            receiverId = C.receiverId,
                            Message = C.Message,
                            status = C.status,
                            Type = C.Type,
                            createDate = C.createDate,
                            createDateTimeStamp = 0
                        }).ToList();
            }
        }

        /**
        * Function for getting receiver list.
        *
        * @param userId     user id  
        */
        public List<object> chatUserList(int userId)
        {
            List<object> objList = new List<object>();
            UsersBL objUsersBL = new UsersBL();
            DataSet ds = new DataSet();
            ds = objUsersBL.userChatList(userId);
            if (ds.Tables[0].Rows.Count > 0)
            {
                objList = ds.Tables[0].AsEnumerable().Select(p => new
                {
                    ID = p.Field<Int32>("ID"),
                    ConnectionId = p.Field<string>("ConnectionId"),
                    UserName = p.Field<string>("UserName"),
                    EmailID = p.Field<string>("EmailID"),
                    status = p.Field<Int32>("status"),
                    deviceToken = p.Field<string>("deviceToken"),
                    userId = p.Field<Int32>("userId"),
                    unreadCnt = p.Field<Int32>("UnreadCnt"),
                    avatar = CheckImageExists(p.Field<String>("avatar"), "PROVIDERIMAGE_THUMB", Convert.ToInt32(ConfigurationManager.AppSettings["DefaultLanguageId"].ToString()))
                }).ToList<object>();
            }
            return objList;
        }

        public string chatUserConnectionIdList(int userId)
        {
            List<object> objList = new List<object>();
            UsersBL objUsersBL = new UsersBL();
            DataSet ds = new DataSet();
            ds = objUsersBL.userChatList(userId);
            string conList = "";
            if (ds.Tables[0].Rows.Count > 0)
            {
                for (int i = 0; i < ds.Tables[0].Rows.Count; i++)
                {
                    if (conList != "")
                    {
                        conList = conList + "," + ds.Tables[0].Rows[i]["ConnectionId"].ToString();
                    }
                    else
                    {
                        conList = ds.Tables[0].Rows[i]["ConnectionId"].ToString();
                    }
                    
                }
            }
            return conList;
        }

        /**
        * Function for getting user object.
        *
        * @param userId     user id  
        */
        public object getChatUserDetails(int userId)
        {
            object objList = new object();
            UsersBL objUsersBL = new UsersBL();
            DataSet ds = new DataSet();
            ds = objUsersBL.getChatUserDetails(userId);
            if (ds.Tables[0].Rows.Count > 0)
            {
                objList = ds.Tables[0].AsEnumerable().Select(p => new
                {
                    ID = p.Field<Int32>("ID"),
                    ConnectionId = p.Field<string>("ConnectionId"),
                    UserName = p.Field<string>("UserName"),
                    EmailID = p.Field<string>("EmailID"),
                    status = p.Field<Int32>("status"),
                    deviceToken = p.Field<string>("deviceToken"),
                    userId = p.Field<Int32>("userId"),
                    avatar = CheckImageExists(p.Field<String>("avatar"), "PROVIDERIMAGE_THUMB", Convert.ToInt32(ConfigurationManager.AppSettings["DefaultLanguageId"].ToString()))
                }).FirstOrDefault();
            }
            return objList;
        }
        #endregion


    }
}