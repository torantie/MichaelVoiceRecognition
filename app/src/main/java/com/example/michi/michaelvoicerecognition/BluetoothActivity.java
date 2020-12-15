package com.example.michi.michaelvoicerecognition;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

        /*
            Button for searching other devices.
         */
        private Button searchButton;
        /*
            Button for setting up a ServerBluetoothSocket and waiting for someone to connect.
        */
        private Button waitButton;
        /*
            Button for turning on bluetooth.
        */
        private Button bluetoothButton;
        /*

         */
        private final int REQUEST_ENABLE_BLUETOOTH = 1;
        /*
            BluetoothAdapter.
         */
        private BluetoothAdapter bluetoothAdapter;
        /*
            Saves mac-adress and name of discovered devices.
         */
        private ArrayAdapter<String> discoveredDevicesAdapter;
        /*
            Saves mac-adress and name of paired devices.
         */
        private ArrayAdapter<String> pairedDevicesAdapter;
        /*
            TODO Blank Fragment for device search
         */
        private BlankFragment blankFragment;
        /*
            An Arraylist to save the found bluetooth devices.
         */
        private ArrayList<BluetoothDevice> bluetoothDevices;
        /*
            Context of the main activity.
         */
        private Context context;
        /*
            Variable that binds context/activity to the BluetoothService
        */
        private BoundBluetoothService boundBluetoothService;
        /*
            Receives intents with all discovered and already paired devices.
        */
        private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                System.out.println("on Receive received: "+action);
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    System.out.println("found "+device+" with bond state " +device.getBondState());
                    String deviceInformation = device.getName() + "\n" + device.getAddress();
                    //If device isnt already bonded or if it is not already in the arrayadapter, add it to the arrayadapter and the list of devices
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED ) {
                        discoveredDevicesAdapter.add(deviceInformation);
                        //
                        bluetoothDevices.add(device);
                        /** TODO **/
                        /**Bundle bundle = new Bundle();
                         bundle.putString("Device Name", device.getName());
                         bundle.putString("Device Address", device.getAddress());
                         // set Fragmentclass Arguments
                         blankFragment.setArguments(bundle);**/
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    if (discoveredDevicesAdapter.getCount() == 0) {
                        discoveredDevicesAdapter.add("no device found");
                    }


                }
            }
        };
        /*
            Receives an intent if aconnection was established and starts the new ChatActivty.
        */
        private final BroadcastReceiver connectionEstablishedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String connection = intent.getAction();

                if (connection.equals("Connection")) {
                    Intent i = new Intent(context,ChatActivity.class);
                    i.putExtra("BluetoothConnection",true);
                    context.startActivity(i);
                }
            }
        };
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_bluetooth);

            context = this;
            bluetoothDevices = new ArrayList<BluetoothDevice>();
            discoveredDevicesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
            pairedDevicesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);

            //Sets up the intent filters with setupIntentFilters().
            setupIntentFilters();
            setupGUI();

        }

        /*
            Starts to discover bluetooth devices if the bluetoothadapter isn't already discovering.
            Searches already paired devices and adds them to  the pairedDevicesAdapter.
        */
        private void discoverBluetoothDevices()
        {

            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();



            //Find the bondedDevices
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            // If there are paired devices, add each one to the ArrayAdapter
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                    bluetoothDevices.add(device);
                }
            } else {
                pairedDevicesAdapter.add("No paired devices");
            }
        }

        /**
         * sends an intent to make the device discoverable.
         * TODO Button
         */
        private void makeDiscoverable()
        {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }

        /**
         * Sets up three intentfilters BluetoothDevice.ACTION_FOUND, BluetoothAdapter.ACTION_DISCOVERY_FINISHED , "Connection".
         */
        private void setupIntentFilters()
        {
            // Register for broadcasts when a device is discovered
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(discoveryFinishReceiver, filter);

            // Register for broadcasts when discovery has finished
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(discoveryFinishReceiver, filter);

            // Register for broadcasts when a device is discovered
            filter = new IntentFilter("Connection");
            registerReceiver(connectionEstablishedReceiver, filter);
        }

        /**
         * Sets the funktion of the buttons in this activity and the onclicklistener of the list
         */
        private void setupGUI()
        {
            ListView discoveredDevices = (ListView) findViewById(R.id.discoveredDevices);
            discoveredDevices.setAdapter(discoveredDevicesAdapter);
            discoveredDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    String item = ((TextView)view).getText().toString();
                    System.out.println("Clicked on item: "+item);
                    ServerClientThreads serverClientThreads = new ServerClientThreads(bluetoothAdapter,context);
                    for (BluetoothDevice device : bluetoothDevices)
                    {
                        System.out.println("In for "+(device.getName() + "\n" + device.getAddress()));
                        if(item.equals(device.getName() + "\n" + device.getAddress()))
                        {
                            serverClientThreads.startConnectThread(device);
                        }
                    }
                }
            });

            ListView pairedDevices = (ListView) findViewById(R.id.pairedDevices);
            pairedDevices.setAdapter(pairedDevicesAdapter);
            pairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {
                    String item = ((TextView)view).getText().toString();
                    System.out.println("Clicked on item: "+item);
                    ServerClientThreads serverClientThreads = new ServerClientThreads(bluetoothAdapter,context);
                    for (BluetoothDevice device : bluetoothDevices)
                    {
                        System.out.println("In for "+(device.getName() + "\n" + device.getAddress()));
                        if(item.equals(device.getName() + "\n" + device.getAddress()))
                        {
                            serverClientThreads.startConnectThread(device);
                        }
                    }
                }
            });

            searchButton = (Button) findViewById(R.id.search);
            searchButton.setVisibility(View.INVISIBLE);
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("Pressed search button ");
                    //setContentView(R.layout.fragment_blank);
                    discoverBluetoothDevices();


                    //FragmentManager fragmentManager = getFragmentManager();
                    //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


                    //fragmentTransaction.add(R.id.constraintLayout,blankFragment);
                    //fragmentTransaction.commit();
                    /**TODO
                     blankFragment = new BlankFragment();
                     blankFragment.show(getSupportFragmentManager(),"");**/
                }
            });
            //Button to wait for incoming connection requests
            waitButton = (Button) findViewById(R.id.wait);
            waitButton.setVisibility(View.INVISIBLE);
            waitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ServerClientThreads serverClientThreads = new ServerClientThreads(bluetoothAdapter,context);
                    serverClientThreads.startAcceptThread();
                }
            });

            bluetoothButton = (Button) findViewById(R.id.bluetooth);
            bluetoothButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("Pressed bluetooth button");
                    //check if bluetooth is available
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (bluetoothAdapter == null) {
                        // Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
                        // finish(); //automatic close app if Bluetooth service is not available!
                    }
                    //turn on bluetooth if it is not turned on
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
                    }

                    //bind to the bluetooth service
                    boundBluetoothService = new BoundBluetoothService(context);

                    view.setVisibility(View.INVISIBLE);
                    findViewById(R.id.search).setVisibility(View.VISIBLE);
                    findViewById(R.id.wait).setVisibility(View.VISIBLE);
                }
            });
        }
        @Override
        public void onPause() {
            super.onPause();
            //unbindService(boundBluetoothService.getConnection());
        }
        @Override
        protected void onDestroy() {
            super.onDestroy();
            // Don't forget to unregister the ACTION_FOUND receiver.
            unregisterReceiver(discoveryFinishReceiver);
            unregisterReceiver(connectionEstablishedReceiver);
            //unbind service
            if(getBoundBluetoothService()!= null && getBoundBluetoothService().isBound()==true)
            {
                unbindService(getBoundBluetoothService().getConnection());
            }

        }
        public BoundBluetoothService getBoundBluetoothService() {
            return boundBluetoothService;
        }



}
