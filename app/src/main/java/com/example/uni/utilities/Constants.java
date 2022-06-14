package com.example.uni.utilities;

import java.util.HashMap;

public class Constants {
    public static final String STATUS = "status";
    public static final String AVAILABLE = "available";
    public static final String REMOTE_AUTHORIZATION = "Authorization";
    public static final String REMOTE_CONTENT_TYPE = "Content-Type";
    public static HashMap<String, String> remoteHeaders = null;
    public static final String REMOTE_DATA = "data";
    public static final String REMOTE_REGISTRATION_IDS = "registration_ids";
    public static HashMap<String, String> getRemoteHeaders(){
        if (remoteHeaders == null){
            remoteHeaders = new HashMap<>();
            remoteHeaders.put(
                    REMOTE_AUTHORIZATION,
                    "key=AAAA9nf4I_U:APA91bGqJNnx8raJleu-FokVTYSAZv784TZbn-Q1bbtyxYF0zEj5auYPGJ19Tv49q5HKygITsG2rWWPa6uO55qMgyj8hcJlAiCZKt7ktuTgza_Bu0yAHJ2AfK22D52MI9VFLdLGiS-0Z"
            );
            remoteHeaders.put(
                    REMOTE_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteHeaders;
    }
    public static final String USERNAME_SIGN = "@";
    public static final String ARGUMENT_PASSWORD = "argumentPassword";
    public static final String ARGUMENT_IMAGE_PROFILE = "argumentImageProfile";
    public static final String ARGUMENT_NAME = "argumentName";
    public static final String RECOVERY_CODE = "recoveryCode";
    public static final String PASSWORD = "password";
    public static final String PREFERENCE_NAME = "appPreference";
    public static final String USERS = "users";
    public static final String USERNAME = "userName";
    public static final String NAME = "name";
    public static final String STORAGE_PACKAGE = "imageProfiles/";
    public static final String IMAGE_PROFILE = "imageProfile";
    public static final String IS_SIGNED_IN = "isSignedIn";
    public static final String BIO = "bio";
    public static final String USER_ID = "userId";
    public static final String APP_VERSION = "Beta 1.0.0";
    public static final Integer DELAY_MILLS = 1000;
    public static final String DEFAULT_IMAGE_PROFILE = "https://firebasestorage.googleapis.com/v0/b/uni1-c1b01.appspot.com/o/imageProfiles%2Fimage_profile.png?alt=media&token=42efb4d1-5ea9-4b13-9110-81083500782c";
    public static final String DEFAULT_BIO = "Hi, I am using Uni";
    public static final String FCM_TOKEN = "fcmToken";
    public static final String USER = "user";
    public static final Integer VIEW_TYPE_SENT = 1;
    public static final Integer VIEW_TYPE_RECEIVED = 2;
    public static final String COLLECTION_CHAT = "chat";
    public static final String SENDER_ID = "senderId";
    public static final String RECEIVER_ID = "receiverId";
    public static final String MESSAGE = "message";
    public static final String TIMESTAMP = "timeStamp";
    public static final String CONVERSATIONS = "conversations";
    public static final String SENDER_NAME = "senderName";
    public static final String RECEIVER_NAME = "receiverName";
    public static final String SENDER_IMAGE = "senderImage";
    public static final String RECEIVER_IMAGE = "receiverImage";
    public static final String LAST_MESSAGE = "lastMessage";
    /*Length*/
    public static final String RECOVERY_CODE_MAX_LENGTH = "8";
    public static final String PASSWORD_MAX_LENGTH = "35";
    public static final String NAME_MAX_LENGTH = "25";
    public static final String USERNAME_MAX_LENGTH = "25";
    public static final String BIO_MAX_LENGTH = "30";
    public static final Integer MINIMUM_PASSWORD_LENGTH = 6;
}
