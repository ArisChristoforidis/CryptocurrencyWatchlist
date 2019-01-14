package com.arisc.cryptocurrencywatchlist;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import org.greenrobot.greendao.query.QueryBuilder;

public class GoogleSignInManager {

    private static final String TAG = "GoogleSignInManager";

    private static GoogleSignInClient mGoogleSignInClient;
    private static UserAccount mSignedAccount;

    public static GoogleSignInClient createGoogleSignInClient(Context context){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        mGoogleSignInClient = GoogleSignIn.getClient(context,gso);

        return mGoogleSignInClient;
    }

    public static GoogleSignInClient getGoogleSignInClient(){
        return mGoogleSignInClient;
    }

    public static UserAccount getSignedAccount() {
        if(mSignedAccount == null) Log.d(TAG,"getSignedAccount attempted to return null");
        return GoogleSignInManager.mSignedAccount;
    }

    public static void setSignedAccount(UserAccount signedAccount) {
        GoogleSignInManager.mSignedAccount = signedAccount;
    }



}
