package com.example.michi.michaelvoicerecognition;

import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


import static android.content.ContentValues.TAG;

/**
 * Created by Mischael on 24.08.2017.
 */

public class ServerClientThreads {
    final static UUID MY_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    final static String APP_NAME = "MichaelVoiceRecognition";

    private BluetoothAdapter mBluetoothAdapter;

    private Context context;


    public ServerClientThreads(BluetoothAdapter mBluetoothAdapter,Context context)
    {
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.context = context;
    }

    /**
     * Sets the BluetoothSocket of the Service binded to the current Activity
     * @param socket
     */
    public void manageMyConnectedSocket(BluetoothSocket socket)
    {
        Log.i(TAG, "manageMyConnectedSocket socket: "+socket);
        //Chat chat = new Chat(socket);
        BluetoothActivity mainActivity = (BluetoothActivity) context;
        mainActivity.getBoundBluetoothService().getService().setBluetoothSocket(socket);
    }

    public void startAcceptThread()
    {
        AcceptThread acceptThread = new AcceptThread();
        Log.i(TAG, "startAcceptThread");
        acceptThread.start();
    }
    public void startConnectThread(BluetoothDevice bluetoothDevice)
    {
        ConnectThread connectThread = new ConnectThread(bluetoothDevice);
        Log.i(TAG, "startConnectThread");
        connectThread.start();
    }






    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    manageMyConnectedSocket(socket);
                    Intent i = new Intent("Connection");
                    context.sendBroadcast(i);
                    cancel();
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
            Intent i = new Intent("Connection");
            context.sendBroadcast(i);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }



}
