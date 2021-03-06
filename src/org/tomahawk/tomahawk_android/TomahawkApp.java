/* == This file is part of Tomahawk Player - <http://tomahawk-player.org> ===
 *
 *   Copyright 2012, Christopher Reichert <creichert07@gmail.com>
 *
 *   Tomahawk is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Tomahawk is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Tomahawk. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tomahawk.tomahawk_android;

import java.io.IOException;

import org.acra.annotation.ReportsCrashes;
import org.tomahawk.libtomahawk.LocalCollection;
import org.tomahawk.libtomahawk.Source;
import org.tomahawk.libtomahawk.SourceList;
import org.tomahawk.libtomahawk.network.TomahawkService;
import org.tomahawk.libtomahawk.network.TomahawkService.TomahawkServiceConnection;
import org.tomahawk.libtomahawk.network.TomahawkService.TomahawkServiceConnection.TomahawkServiceConnectionListener;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * This class contains represents the Application core.
 */
@ReportsCrashes(formKey = "")
public class TomahawkApp extends Application implements AccountManagerCallback<Bundle>,
        TomahawkServiceConnectionListener {

    private static final String TAG = TomahawkApp.class.getName();

    private static Context sApplicationContext;

    private AccountManager mAccountManager = null;
    private SourceList mSourceList;

    private TomahawkServiceConnection mTomahawkServiceConnection = new TomahawkServiceConnection(this);
    private TomahawkService mTomahawkService;

    @Override
    public void onCreate() {
        TomahawkExceptionReporter.init(this);
        super.onCreate();
        sApplicationContext = getApplicationContext();

        mSourceList = new SourceList();

        initialize();
    }

    /**
     * Initialize the Tomahawk app.
     */
    public void initialize() {
        initLocalCollection();
    }

    /**
     * Initializes a new Collection of all local tracks.
     */
    public void initLocalCollection() {
        Log.d(TAG, "Initializing Local Collection.");
        Source src = new Source(new LocalCollection(), 0, "My Collection");
        mSourceList.setLocalSource(src);
    }

    /**
     * Returns the Tomahawk AccountManager;
     */
    public AccountManager getAccountManager() {
        return mAccountManager;
    }

    /**
     * Return the list of Sources for this TomahawkApp.
     * 
     * @return SourceList
     */
    public SourceList getSourceList() {
        return mSourceList;
    }

    /**
     * Returns the context for the application
     * 
     * @return
     */
    public static Context getContext() {
        return sApplicationContext;
    }

    /**
     * This method is called when the Authenticator has finished.
     * why d
     * Ideally, we start the Tomahawk web service here.
     */
    @Override
    public void run(AccountManagerFuture<Bundle> result) {

        try {

            String token = result.getResult().getString(AccountManager.KEY_AUTHTOKEN);
            String username = result.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);
            if (token == null) {
                Intent i = new Intent(getApplicationContext(), TomahawkAccountAuthenticatorActivity.class);
                startActivity(i);
            } else {
                Log.d(TAG, "Starting Tomahawk Service: " + token);
                Intent intent = new Intent(this, TomahawkService.class);
                intent.putExtra(TomahawkService.ACCOUNT_NAME, username);
                intent.putExtra(TomahawkService.AUTH_TOKEN_TYPE, token);
                startService(intent);
                bindService(intent, mTomahawkServiceConnection, Context.BIND_AUTO_CREATE);
            }

        } catch (OperationCanceledException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setTomahawkService(TomahawkService ps) {
        mTomahawkService = ps;
    }

    @Override
    public void onTomahawkServiceReady() {

    }
}
