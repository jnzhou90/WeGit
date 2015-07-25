package com.quinn.githubknife.interactor;

import android.accounts.Account;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.quinn.githubknife.account.GitHubAccount;
import com.quinn.githubknife.utils.L;
import com.quinn.githubknife.utils.PreferenceUtils;
import com.quinn.httpknife.github.Github;
import com.quinn.httpknife.github.GithubError;
import com.quinn.httpknife.github.GithubImpl;
import com.quinn.httpknife.github.User;

/**
 * Created by Quinn on 7/22/15.
 */
public class UserInfoInteractorImpl implements UserInfoInteractor {

    private final static String TAG = UserInfoInteractorImpl.class.getSimpleName();
    private final static int USER_MSG = 1;
    private final static int BOOL_MSG = 2;

    private Context context;
    private GitHubAccount gitHubAccount;
    private Github github;
    private OnLoadUserInfoListener listener;
    private Handler handler;

    public UserInfoInteractorImpl(Context context, final OnLoadUserInfoListener listener){
        String name = PreferenceUtils.getString(context, PreferenceUtils.Key.ACCOUNT);
        if(name.isEmpty())
            name = "NO_ACCOUNT";
        Account account = new Account(name, GitHubAccount.ACCOUNT_TYPE);
        this.gitHubAccount = new GitHubAccount(account,context);
        this.github = new GithubImpl(context);
        this.listener = listener;

        this.handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case USER_MSG:
                        User user = (User)msg.obj;
                        listener.onFinish(user);
                        break;
                    case BOOL_MSG:

                        break;
                }

            }
        };
    }

    @Override
    public void auth() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String token = gitHubAccount.getAuthToken();
                L.i(TAG,"token = " + token);
                User user = null;
                try {
                     user = github.authUser(token);
                    L.i(TAG,user.toString());
                    L.i("Get new avatar = " + user.getAvatar_url());
                }catch (GithubError e){
                    L.i("update avatar url fail");
                }
                Message msg = new Message();
                msg.what = 1;
                msg.obj = user;
                handler.sendMessage(msg);
            }
        }).start();


    }

    @Override
    public void userInfo(final String username) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String token = gitHubAccount.getAuthToken();
                github.makeAuthRequest(token);
                User user = null;
                try {
                    user = github.user(username);
                    L.i(TAG,user.toString());
                }catch (GithubError e){
                    L.i(TAG,"userinfo fail");
                }
                Message msg = new Message();
                msg.what = USER_MSG;
                msg.obj = user;
                handler.sendMessage(msg);
            }
        }).start();
    }

    @Override
    public void hasFollow(String user) {

    }

    @Override
    public void follow(String user) {

    }

    @Override
    public void unFollow(String user) {

    }
}
