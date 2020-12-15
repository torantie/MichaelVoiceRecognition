package com.example.michi.michaelvoicerecognition;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Random;

import static android.content.ContentValues.TAG;

public class BluetoothService extends Service {


    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    private BluetoothSocket bluetoothSocket;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        BluetoothService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** method for clients */
    public int getRandomNumber() {
        return mGenerator.nextInt(100);
    }

    public BluetoothSocket getBluetoothSocket() {
        Log.i(TAG,"getBluetoothSocket in BluetoothService returns: "+bluetoothSocket);
        return bluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        Log.i(TAG,"setBluetoothSocket in BluetoothService sets bluetoothSocket to: "+bluetoothSocket);
        this.bluetoothSocket = bluetoothSocket;
    }

}
