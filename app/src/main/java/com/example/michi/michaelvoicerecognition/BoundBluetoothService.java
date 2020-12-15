package com.example.michi.michaelvoicerecognition;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by Mischael on 30.08.2017.
 */

public class BoundBluetoothService {
    /**
     * current context
     */
    private Context context;
    /**
     * BluetoothService
     */
    private BluetoothService mService;
    /**
     * bool for checking if the BluetoothService is bound to the activity
     */
    private boolean mBound;


    /**
     * ServiceConnection required to bind service
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Because we have bound to an explicit
            // service that is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mService = binder.getService();
            context.sendBroadcast(new Intent("ConnectionToService"));
            mBound = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {

            mBound = false;
        }
    };


    public BoundBluetoothService(Context context)
    {
        this.context = context;
        bindToBluetoothService();

    }

    /**
     * bind BluetoothService to current context
     */
    private void bindToBluetoothService()
    {
        Intent intent = new Intent(context, BluetoothService.class);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        System.out.println("blablabla ");
    }

    public BluetoothService getService() {
        if(!isBound())
        {
            System.out.println("Service is not bound ");
        }
        return mService;
    }

    public boolean isBound() {
        return mBound;
    }

    public ServiceConnection getConnection() {
        return mConnection;
    }
}
