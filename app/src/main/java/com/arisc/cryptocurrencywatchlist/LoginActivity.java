package com.arisc.cryptocurrencywatchlist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.greenrobot.greendao.query.QueryBuilder;

public class LoginActivity extends AppCompatActivity {


    private static final String TAG = "LoginActivity";

    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton mSignInButton;

    private static final int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mGoogleSignInClient = GoogleSignInManager.createGoogleSignInClient(this);

        mSignInButton = findViewById(R.id.btnSignIn);
        mSignInButton.setSize(SignInButton.SIZE_WIDE);

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Check if the user is already signed in.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            onSuccessfulUserLogin(account.getId());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            onSuccessfulUserLogin(account.getId());
        } catch (ApiException e) {
            Log.d(TAG, "ApiException(handleSignInResult). Code: " + e.getStatusCode());
            e.printStackTrace();
        }

    }

    private void onSuccessfulUserLogin(String id){

        Log.d(TAG,"onSuccessfulUserLogin:User id is " + id);

        //Put them in the database
        DaoSession daoSession = ((App)getApplication()).getDaoSession();
        UserAccountDao userAccountDao = daoSession.getUserAccountDao();

        QueryBuilder<UserAccount> queryBuilder = userAccountDao.queryBuilder();

        queryBuilder.where(UserAccountDao.Properties.UserId.eq(id));

        UserAccount account = queryBuilder.unique();

        if(account == null){
            account = new UserAccount();
            account.setUserId(id);
            userAccountDao.insert(account);
        }

        if(account != null){
            Log.d(TAG,"onSuccessfulUserLogin:Account is not null");
        }else{
            Log.d(TAG,"onSuccessfulUserLogin:Account is null");
        }
        GoogleSignInManager.setSignedAccount(account);
        //Start the activity.
        startMainActivity();
    }

    private void startMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        //We don't want to let the user return to this activity by pressing the back button,so we kill it.
        finish();

    }



}
