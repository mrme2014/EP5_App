package com.ntk.util;

import com.ntk.nvtkit.NVTKitModel;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SocketHBModel implements onHBListener {

	private static String TAG = "SocketHBModel";
	private static Handler eventHandler;
	private static int count_HB = 0;
	private static boolean isWorking = false;

	public SocketHBModel(final Handler eventHandler) {
		NVTKitModel.setHBCallback(SocketHBModel.this);
		this.eventHandler = eventHandler;
	}

	public static void startSocketHB() {

		if (isWorking == false) {
			isWorking = true;
			new Thread(new Runnable() {
				@Override
				public void run() {

					try {
						NVTKitModel.closeNotifySocket();
						Thread.sleep(100);
						NVTKitModel.initNotifySocket();
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					while (isWorking) {
						while (count_HB < 5) {
							NVTKitModel.sendSockectHB();
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						Message msg = eventHandler.obtainMessage(1, "SocketHBModel on");
						eventHandler.sendMessage(msg);

						String ack = NVTKitModel.devHeartBeat();
						int count_devHeartBeat = 0;
						while (ack == null) {

							try {
								Thread.sleep(1000);
								Log.e(TAG, "devHeartBeat no response");
								ack = NVTKitModel.devHeartBeat();
								
								count_devHeartBeat = count_devHeartBeat + 1;
								if(count_devHeartBeat >= 5) {
									WifiAPUtil.setWifiEnabled(false);
									Log.e(TAG, "setWifiApEnabled false");
									Thread.sleep(3000);
									WifiAPUtil.setWifiEnabled(true);
									Log.e(TAG, "setWifiApEnabled true");
									count_devHeartBeat = 0;
								}
								
								
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}

						//Log.e(TAG, ack);
						if (ack.equals("-22")) {
							NVTKitModel.devAPPSessionOpen();
						}

						count_HB = 0;
						NVTKitModel.closeNotifySocket();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						NVTKitModel.initNotifySocket();

						NVTKitModel.videoStopPlay();
						NVTKitModel.videoResumePlay();

						Message msg2 = eventHandler.obtainMessage(1, "SocketHBModel off" );
						eventHandler.sendMessage(msg2);
					}
					isWorking = false;
				}
			}).start();
		}
	}	

	public void SocketHBStart() {

	}

	public void SocketHBStop() {

	}

	@Override
	public void onHBReturn() {
		Log.e("onHBReturn", "onHBReturn");
		count_HB = count_HB + 1;
	}
}