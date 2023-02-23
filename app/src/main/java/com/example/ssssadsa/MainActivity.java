package com.example.ssssadsa;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;

import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private Button ledoff;
    private Button connect_b;
    private Button search_b;
    private Button ledon;
    private TextView BT_devices_txt;

    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Defining Permission codes.
    // We can give any value
    // but unique for each permission.
    private static final int BLUETOOTH_CODE = 102;
    private static final int BLUETOOTH_ADMIN_CODE = 103;
    private static final int BLUETOOTH_CONNECT_CODE = 104;
    private static final String TAG = "Logs";


    ConnectedThread connectedThread;

    /* declare device object */
    public BluetoothDevice arduinoBTModule = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* GUI components defined */
        /* GUI components */
        ledon = findViewById(R.id.ledon);
        ledoff = findViewById(R.id.ledoff);
        connect_b = findViewById(R.id.connect_b);
        search_b = findViewById(R.id.searchDevices);
        BT_devices_txt = findViewById(R.id.BT_devices_txt);

        /* Set up Bluetooth*/
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        // Set Buttons on Click Listeners
        ledon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                connectedThread.write("1");
            }
        });

        ledoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                connectedThread.write("0");
            }
        });

        search_b.setOnClickListener(new View.OnClickListener() {
            //Display all the linked BT Devices
            @Override
            public void onClick(View view) {
                checkPermission(Manifest.permission.BLUETOOTH, BLUETOOTH_CODE);
                checkPermission(Manifest.permission.BLUETOOTH_ADMIN, BLUETOOTH_ADMIN_CODE);
                /* not required for my version */
                //checkPermission(Manifest.permission.BLUETOOTH_CONNECT, BLUETOOTH_CONNECT_CODE);

                    String btDevicesString="";
                    Set< BluetoothDevice > pairedDevices = bluetoothAdapter.getBondedDevices();

                    if (pairedDevices.size() > 0) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice device: pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress(); // MAC address
                            Log.d(TAG, "deviceName:" + deviceName);
                            Log.d(TAG, "deviceHardwareAddress:" + deviceHardwareAddress);
                            //We append all devices to a String that we will display in the UI
                            btDevicesString=btDevicesString+deviceName+" || "+deviceHardwareAddress+"\n";
                            //If we find the HC 05 device (the Arduino BT module)
                            //We assign the device value to the Global variable BluetoothDevice
                            //We enable the button "Connect to HC 05 device"
                            if (deviceName.equals("HC-05")) {
                                Log.d(TAG, "HC-05 found");
                                MY_UUID = device.getUuids()[0].getUuid();
                                arduinoBTModule = device;
                            }
                            BT_devices_txt.setText(btDevicesString);
                        }
                    }
                }
        });

        connect_b.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onClick(View view) {

                checkPermission(Manifest.permission.BLUETOOTH, BLUETOOTH_CODE);
                checkPermission(Manifest.permission.BLUETOOTH_ADMIN, BLUETOOTH_ADMIN_CODE);
                /* not required for my version */
                //checkPermission(Manifest.permission.BLUETOOTH_CONNECT, BLUETOOTH_CONNECT_CODE);

                ConnectThread connectThread = new ConnectThread(arduinoBTModule, MY_UUID);
                connectThread.run();
                if (connectThread.getMmSocket().isConnected()) {
                    Toast.makeText(MainActivity.this, "Socket Opened", Toast.LENGTH_SHORT).show();
                    //The pass the Open socket as arguments to call the constructor of ConnectedThread
                    connectedThread = new ConnectedThread(connectThread.getMmSocket());
                }
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();

            }
        });
    }

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
        else { // i chose to disable this toast
            //Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == BLUETOOTH_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "BLUETOOTH Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "BLUETOOTH Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }

        else if (requestCode == BLUETOOTH_ADMIN_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "BLUETOOTH_ADMIN Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "BLUETOOTH_ADMIN Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == BLUETOOTH_CONNECT_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "BLUETOOTH_CONNECT Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "BLUETOOTH_CONNECT Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}