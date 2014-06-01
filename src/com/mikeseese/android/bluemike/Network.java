package com.mikeseese.android.bluemike;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class Network
{
	private static final int REQUEST_ENABLE_BT = 0;
	private ArrayList<ConnectedThread> mConnections;
	public UUID mUUID;
	private BluetoothAdapter mBluetoothAdapter;
	private AcceptThread acceptingThread;
	public Handler mHandler;
	
	public boolean main_node;
	
	public ArrayAdapter<String> btArrayAdapter;
	
	public Network(Handler h)
	{
		main_node = false;
		
		this.mHandler = h;
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mConnections = new ArrayList<ConnectedThread>();
		mUUID = UUID.fromString("65a6b459-b104-31fa-a212-207365657365"); //' SEESE'
		if (mBluetoothAdapter == null)
		{
		    // Device does not support Bluetooth
		}
	}
	
	public void addDevice(BluetoothSocket socket)
	{
		ConnectedThread newConnection = new ConnectedThread(mHandler, socket);
		newConnection.start();
		mConnections.add(newConnection);
		if(main_node)
		{
			Integer i = mConnections.size() - 1;
			newConnection.write("$NUMDEV," + i.toString());
		}
		
		Message msg = mHandler.obtainMessage();
		msg.what = BMConstants.DEVICE_ADDED;
		msg.sendToTarget();
	}
	
	public boolean BTIsEnabled()
	{
		return mBluetoothAdapter.isEnabled();
	}
	
	public ArrayList<BluetoothDevice> FindNetworks(/*service*/)
	{
		if (!mBluetoothAdapter.isEnabled())
		{
		    //return back
			return null;
		}

		mBluetoothAdapter.startDiscovery();
		
		ArrayList<BluetoothDevice> mDeviceArray = new ArrayList<BluetoothDevice>();
		
		return mDeviceArray;
	}
	
	public boolean FindNetwork(String name)
	{
		if (!mBluetoothAdapter.isEnabled())
		{
		    //return back
		}
		
		return true;
	}
	
	public boolean CreateNetwork(String name)
	{
		main_node = true;
		
		if (!mBluetoothAdapter.isEnabled())
		{
		    //return back
			return false;
		}
		
		/*if(FindNetwork(name))
		{
			return false;
		}*/
		
		//init accepting
		acceptingThread = new AcceptThread(name, this);
		acceptingThread.start();
		return true;
	}
	
	public boolean JoinNetwork(BluetoothDevice device)
	{
		main_node = false;
		
		ConnectThread connectingThread = new ConnectThread(this, device);
		connectingThread.start();
		AcceptThread tmpAcceptThread = new AcceptThread(this, false);
		tmpAcceptThread.start();
		return true;
	}
	
	public void broadcast(String message)
	{
		broadcast(message.getBytes());
	}
	
	public void broadcast(byte[] message)
	{
		//for all devices, send message
		for(int i = 0; i < mConnections.size(); i++)
		{
			mConnections.get(i).write(message);
		}
	}
	
	public void connectTo(String addr)
	{
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(addr);
		
		ConnectThread connectingThread = new ConnectThread(this, device);
		connectingThread.start();
	}
}
