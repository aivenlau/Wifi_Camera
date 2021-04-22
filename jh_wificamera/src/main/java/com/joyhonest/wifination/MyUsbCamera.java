package com.joyhonest.wifination;

import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.util.Log;

//import com.joyhonest.jh_ui.R;




import org.simple.eventbus.EventBus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyUsbCamera {

    private boolean DEBUG = true;
    private final String TAG ="MyUsbCamera";

    private static final String ACTION_USB_PERMISSION_BASE = "com.serenegiant.USB_PERMISSION.";
    private final String ACTION_USB_PERMISSION = ACTION_USB_PERMISSION_BASE + hashCode();
    public static final String ACTION_USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";

    public static MyUsbCamera myUsbCamera;

    private UsbManager usbManager;


    private boolean  bRegisterUsb = false;

    private final WeakReference<Context> mWeakContext;

    private PendingIntent mPermissionIntent = null;
    private List<DeviceFilter> mDeviceFilters = new ArrayList<DeviceFilter>();

    private MyUsbCamera(Context context)
    {
        mWeakContext = new WeakReference<Context>(context);
        usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
    }

    public static MyUsbCamera getInstance(Context context) {
        if (myUsbCamera == null) {
            myUsbCamera = new MyUsbCamera(context);
            myUsbCamera._regisgerUsb();
        }
        return myUsbCamera;
    }

    public static void Release()
    {
        if(myUsbCamera!=null)
        {
            myUsbCamera._release();
        }
    }


    private  void _regisgerUsb()
    {
        if(bRegisterUsb)
            return;

        if (mPermissionIntent == null) {
            if (DEBUG) Log.i(TAG, "register:");
            final Context context = mWeakContext.get();
            if (context != null)
            {
                mPermissionIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
                final IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                // ACTION_USB_DEVICE_ATTACHED never comes on some devices so it should not be added here
                filter.addAction(ACTION_USB_DEVICE_ATTACHED);
                filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
                context.getApplicationContext().registerReceiver(mUsbReceiver, filter);
                bRegisterUsb = true;
            }
        }

    }

    private  void _unregisgerUsb()
    {
        if(!bRegisterUsb)
            return;
        final Context context = mWeakContext.get();
        try {
            if (context != null) {
                context.unregisterReceiver(mUsbReceiver);
            }
        } catch (final Exception e) {
             Log.w(TAG, e);
        }
        bRegisterUsb = false;
    }

    private void _release()
    {
        _unregisgerUsb();

    }

    public List<UsbDevice> getDeviceList(final List<DeviceFilter> filters) throws IllegalStateException {
        // get detected devices
        final HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/MyUsbCamera/failed_devices.txt";

        File logFile = new File(fileName);
        if(!logFile.getParentFile().exists()) {
            logFile.getParentFile().mkdirs();
        }

        if(! logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter(logFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(fw != null) {
            pw = new PrintWriter(fw);
        }
        final List<UsbDevice> result = new ArrayList<UsbDevice>();
        if (deviceList != null) {
            if ((filters == null) || filters.isEmpty()) {
                result.addAll(deviceList.values());
            } else {
                for (final UsbDevice device: deviceList.values() ) {
                    // match devices
                    for (final DeviceFilter filter: filters) {
                        if ((filter != null) && filter.matches(device) || (filter != null && filter.mSubclass == device.getDeviceSubclass())) {
                            // when filter matches
                            if (!filter.isExclude) {
                                result.add(device);
                            }
                            break;
                        } else {
                            // collection failed dev's class and subclass
                            String devModel = android.os.Build.MODEL;
                            String devSystemVersion = android.os.Build.VERSION.RELEASE;
                            String devClass = String.valueOf(device.getDeviceClass());
                            String subClass = String.valueOf(device.getDeviceSubclass());
                            try{
                                if(pw != null) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(devModel);
                                    sb.append("/");
                                    sb.append(devSystemVersion);
                                    sb.append(":");
                                    sb.append("class="+devClass+", subclass="+subClass);
                                    pw.println(sb.toString());
                                    pw.flush();
                                    fw.flush();
                                }
                            }catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        if (pw != null) {
            pw.close();
        }
        if (fw != null) {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


    public List<UsbDevice> getUsbDeviceList() {
//        final Context context = mWeakContext.get();
//        if(context==null)
//            return  null;
//        List<DeviceFilter> deviceFilters = DeviceFilter
//                .getDeviceFilters(context.getApplicationContext(), com.joyhonest.wifination.R.xml.joyhonest_device_filter);
//
//        return getDeviceList(deviceFilters);
        return null;
    }

    public synchronized boolean requestPermission(final UsbDevice device) {
//		if (DEBUG) Log.v(TAG, "requestPermission:device=" + device);
        boolean result = false;
        if(usbManager == null)
        {
            EventBus.getDefault().post(device,"Connected_UsbCamera_Cancel");
        }
        else
        {
            if (device != null) {
                if (usbManager.hasPermission(device)) {
                    // call onConnect if app already has permission
                    //processConnect(device);
                    EventBus.getDefault().post(device,"Connected_UsbCamera");
                } else {
                    try {
                        // パーミッションがなければ要求する
                        usbManager.requestPermission(device, mPermissionIntent);
                    } catch (final Exception e) {
                        // Android5.1.xのGALAXY系でandroid.permission.sec.MDM_APP_MGMTという意味不明の例外生成するみたい
                        Log.w(TAG, e);
                        EventBus.getDefault().post(device,"Connected_UsbCamera_Cancel");
                        result = true;
                    }
                }
            } else {
                EventBus.getDefault().post(device,"Connected_UsbCamera_Cancel");
                result = true;
            }
        }
        return result;
    }


    public void requestPermission(int index) {
        List<UsbDevice> devList = getUsbDeviceList();
        if (devList == null || devList.size() == 0) {
            return;
        }
        int count = devList.size();
        if (index >= count)
            return;
        requestPermission(getUsbDeviceList().get(index));
    }


    /**
     * BroadcastReceiver for USB permission
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                {
                    final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            EventBus.getDefault().post(device,"Connected_UsbCamera");
                        }
                    } else {
                        // failed to get permission
                        EventBus.getDefault().post(device,"Connected_UsbCamera_Fail");
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                //updatePermission(device, hasPermission(device));
                //processAttach(device);
                EventBus.getDefault().post(device,"Attached_UsbCamera");
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                // when device removed
                final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    EventBus.getDefault().post(device,"Detached_UsbCamera");
                }
            }
        }
    };

}
