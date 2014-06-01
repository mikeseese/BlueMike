package com.mikeseese.android.bluemike;

import java.io.IOException;
import java.util.UUID;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class AcceptThread extends Thread
{
	private Network mNetwork;
	public Handler mHandler;
	private final BluetoothServerSocket mmServerSocket;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean is_main_node;
	
	public AcceptThread(Network n, boolean main_node)
	{
		is_main_node = main_node;
		
		mNetwork = n;
    	
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothServerSocket tmp = null;
        try
        {
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("ballslappers", mNetwork.mUUID);
        }
        catch (IOException e) {}
        mmServerSocket = tmp;
	}
	 
    public AcceptThread(String name, Network n)
    {
    	is_main_node = true;
    	
    	mNetwork = n;
    	
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothServerSocket tmp = null;
        try
        {
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(name, mNetwork.mUUID);
        }
        catch (IOException e) {}
        mmServerSocket = tmp;
    }
 
    public void run()
    {
        BluetoothSocket socket = null;

        while (true)
        {
            try
            {
                socket = mmServerSocket.accept();
            } 
            catch (IOException e)
            {
                break;
            }
            
            // If a connection was accepted
            if (socket != null) 
            {
            	if(is_main_node)
            	{
	            	mNetwork.broadcast("$NEWDEV,"+socket.getRemoteDevice().getAddress()+";"); //broadcast to everyone else to add the new device
	        		Message msg = mNetwork.mHandler.obtainMessage();
	        		msg.what = BMConstants.NEW_DEVICE;
	        		msg.sendToTarget();
            	}
            	
        		mNetwork.addDevice(socket); //now add the device to the current network
                socket = null;
            }
        }
	}
	
	/** Will cancel the listening socket, and cause the thread to finish */
	public void cancel()
	{
	    try
	    {
	        mmServerSocket.close();
	    }
	    catch (IOException e) {}
	}
}
