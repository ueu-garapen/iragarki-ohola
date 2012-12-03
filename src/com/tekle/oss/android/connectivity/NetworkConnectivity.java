/**
 * Copyright (c) 2012 Ephraim Tekle genzeb@gmail.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
 * associated documentation files (the "Software"), to deal in the Software without restriction, including 
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the 
 * following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial 
 * portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN 
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 *  @author Ephraim A. Tekle
 *
 */
package com.tekle.oss.android.connectivity;
 

import java.util.ArrayList;
import java.util.List; 

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

/**
 * This class provides a singleton object for monitoring network connectivity and to receive notification if connection state changes.
 * <p />
 * To use this class, acquire the singleton instance via the static method {@link #sharedNetworkConnectivity()} then register listeners using {@link #addNetworkMonitorListener(NetworkMonitorListener)}. Notification calls are done on the UI (i.e. main) thread and therefore are safe to update UI elements within the interface method implementation. 
 * <p />
 * <em>NOTE: the {@link NetworkConnectivity} singleton objects needs to be configured using the {@link #configure(Activity)} method prior to calling any other method on the object.</em>
 * @see NetworkMonitorListener
 * 
 * @author Ephraim A. Tekle
 *
 */
public class NetworkConnectivity { 
	
	private static NetworkConnectivity sharedNetworkConnectivity = null;
	
	private Activity activity = null;

	private final Handler handler = new Handler();
	private Runnable runnable = null;
	
	private boolean stopRequested = false;
	private boolean monitorStarted = false;
	
	private static final int NETWORK_CONNECTION_YES= 1;
	private static final int NETWORK_CONNECTION_NO = -1;
	private static final int NETWORK_CONNECTION_UKNOWN = 0;
	
	private int connected = NETWORK_CONNECTION_UKNOWN;
	 
	public static final int MONITOR_RATE_WHEN_CONNECTED_MS = 5000;
	public static final int MONITOR_RATE_WHEN_DISCONNECTED_MS = 1000;
	
	private final List<NetworkMonitorListener> networkMonitorListeners = new ArrayList<NetworkMonitorListener>();
	
	private NetworkConnectivity() { 
	}
	
	public synchronized static NetworkConnectivity sharedNetworkConnectivity() {
		if (sharedNetworkConnectivity == null) {
			sharedNetworkConnectivity = new NetworkConnectivity();
		}
		
		return sharedNetworkConnectivity;
	}
	
	/**
	 * The network monitor must be configured prior to use. The {@code Activity} is used to retrieve the connectivity service.
	 * @param activity the activity that will be used to retrieve connectivity service and make UI Thread calls on connection state changes
	 */
	public void configure(Activity activity) {
		this.activity = activity;
	}
	 
	/**
	 * Starts network monitor. This call is asynchronous and returns immediately. 
	 * 
	 * @return returns {@code true} if the {@code NetworkConnectivity} is configured and ready to start network monitor; {@code false} otherwise.
	 */
	public synchronized boolean startNetworkMonitor() {
		if (this.activity == null) {
			return false;
		}
		
		if (monitorStarted) {
			return true;
		}
		
		stopRequested = false;
		monitorStarted = true;
		
		(new Thread(new Runnable() { 
			@Override
			public void run() { 
				doCheckConnection() ;
			} 
		})).start(); 
		
		return true;
	}
	
	public synchronized void stopNetworkMonitor() {
		stopRequested = true;
		monitorStarted = false;
	}
	
	public void addNetworkMonitorListener(NetworkMonitorListener l) {
		this.networkMonitorListeners.add(l);
		this.notifyNetworkMonitorListener(l);
	}
	
	public boolean removeNetworkMonitorListener(NetworkMonitorListener l) {
		return this.networkMonitorListeners.remove(l);
	}
	
	private void doCheckConnection() {
		
		if (stopRequested) {
			runnable = null;
			return;
		}
		
		final boolean connectedBool = this.isConnected();
		final int _connected = (connectedBool?NETWORK_CONNECTION_YES:NETWORK_CONNECTION_NO);
		
		if (this.connected != _connected) { 
			
			this.connected = _connected;
			
			activity.runOnUiThread(new Runnable() { 
				@Override
				public void run() { 
					notifyNetworkMonitorListeners();
				}
			});
		}
		
		runnable = new Runnable() { 
			@Override
			public void run() {
				doCheckConnection();
			}
		};
		
		handler.postDelayed(runnable, (connectedBool?MONITOR_RATE_WHEN_CONNECTED_MS:MONITOR_RATE_WHEN_DISCONNECTED_MS));
	}
	
	/**
	 * A synchronous call to check if network connectivity exists.
	 * 
	 * @return {@true} if network is connected, {@false} otherwise.
	 */
	public boolean isConnected() {
		try {
			ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE); 
			NetworkInfo netInfo = cm.getActiveNetworkInfo();

			if (netInfo != null && netInfo.isConnected()) {
				return true;
			} else { 
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	} 
	
	private void notifyNetworkMonitorListener(NetworkMonitorListener l) {
		try {
			if (this.connected == NETWORK_CONNECTION_YES) {
				l.connectionEstablished();
			} else if (this.connected == NETWORK_CONNECTION_NO) {
				l.connectionLost();
			} else {
				l.connectionCheckInProgress();
			}
		} catch (Exception e) { 
		}
	}
	
	private void notifyNetworkMonitorListeners() {
		for (NetworkMonitorListener l : this.networkMonitorListeners) {
			this.notifyNetworkMonitorListener(l);
		}
	}
}
