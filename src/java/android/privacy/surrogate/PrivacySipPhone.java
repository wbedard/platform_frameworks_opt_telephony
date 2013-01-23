/**
* Copyright (C) 2012 Stefan Thiele
* This program is free software; you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the Free Software
* Foundation; either version 3 of the License, or (at your option) any later version.
* This program is distributed in the hope that it will be useful, but WITHOUT ANY
* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
* PARTICULAR PURPOSE. See the GNU General Public License for more details.
* You should have received a copy of the GNU General Public License along with
* this program; if not, see <http://www.gnu.org/licenses>.
*/

package android.privacy.surrogate;

import android.content.Context;
import android.net.sip.SipProfile;
import android.os.Binder;
import android.os.Process;
import android.os.ServiceManager;
import android.privacy.IPrivacySettingsManager;
import android.privacy.PrivacyServiceException;
import android.privacy.PrivacySettings;
import android.privacy.PrivacySettingsManager;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.PhoneNotifier;
import com.android.internal.telephony.PhoneSubInfo;
import com.android.internal.telephony.UUSInfo;
import com.android.internal.telephony.sip.SipPhone;
/**
 * Provides privacy handling for phone
 * @author CollegeDev
 * {@hide}
 */
public class PrivacySipPhone extends SipPhone{

	private static final String P_TAG = "PrivacySipPhone";
	private PrivacySettingsManager mPrvSvc;
	private Context context;
	
	public PrivacySipPhone(Context context, PhoneNotifier pN, SipProfile sP) {
		super(context, pN, sP); //I've changed the constructor to public!
		this.context = context;
        mPrvSvc = PrivacySettingsManager.getPrivacyService();
		Log.i(P_TAG,"Constructor ready for package: " + context.getPackageName());
	}
	
	
	@Override
	public String getDeviceSvn() {
		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getDeviceSvn()");
		String packageName = context.getPackageName();
		String output;
		try {
		    if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
		        PrivacySettings settings = mPrvSvc.getSettings(packageName);
		        if (settings == null || settings.getDeviceIdSetting() == PrivacySettings.REAL) {
		            output = super.getDeviceId();
		            mPrvSvc.notification(packageName, PrivacySettings.REAL, PrivacySettings.DATA_DEVICE_ID, output);
		        } else {
		            output = settings.getDeviceId(); // can be empty, custom or random
		            mPrvSvc.notification(packageName, settings.getDeviceIdSetting(), PrivacySettings.DATA_DEVICE_ID, output);
		        }
		} catch (PrivacyServiceException e) {
		    Log.e(P_TAG,"PrivacySipPhone:getDeviceSvn: PrivacyServiceException occurred: " + context.getPackageName(), e);
            output = "";
            mPrvSvc.notification(packageName, PrivacySettings.ERROR, PrivacySettings.DATA_DEVICE_ID, output);
		}
        return output;
	}
	
	@Override
	public String getImei() {
		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getImei");
		String packageName = context.getPackageName();
		String output;		
		try {
		    if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
		        PrivacySettings settings = mPrvSvc.getSettings(packageName);
		        if (settings == null || settings.getDeviceIdSetting() == PrivacySettings.REAL) {
		            output = super.getImei();
		            mPrvSvc.notification(packageName, PrivacySettings.REAL, PrivacySettings.DATA_DEVICE_ID, output);
		        } else {
		            output = settings.getDeviceId(); // can be empty, custom or random
		            mPrvSvc.notification(packageName, settings.getDeviceIdSetting(), PrivacySettings.DATA_DEVICE_ID, output);
		        }
		} catch (PrivacyServiceException e) {
		    Log.e(P_TAG,"PrivacySipPhone:getImei: PrivacyServiceException occurred: " + context.getPackageName(), e);
            output = "";
            mPrvSvc.notification(packageName, PrivacySettings.ERROR, PrivacySettings.DATA_DEVICE_ID, output);
		}
        return output;
	}
	
	@Override
	public String getSubscriberId() {
		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getSubscriberId()");
		String packageName = context.getPackageName();
		String output;
		try {
		    if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
		        PrivacySettings settings = mPrvSvc.getSettings(packageName);
		        if (settings == null || settings.getSubscriberIdSetting() == PrivacySettings.REAL) {
		            output = super.getSubscriberId();
		            mPrvSvc.notification(packageName, PrivacySettings.REAL, PrivacySettings.DATA_SUBSCRIBER_ID, output);
		        } else {
		            output = settings.getSubscriberId(); // can be empty, custom or random
		            mPrvSvc.notification(packageName, settings.getSubscriberIdSetting(), PrivacySettings.DATA_SUBSCRIBER_ID, output);
		        }
		} catch (PrivacyServiceException e) {
		    Log.e(P_TAG,"PrivacySipPhone:getSubscriberId: PrivacyServiceException occurred: " + context.getPackageName(), e);
            output = "";
            mPrvSvc.notification(packageName, PrivacySettings.ERROR, PrivacySettings.DATA_SUBSCRIBER_ID, output);
		}
        return output;
	}
	
//	@Override
//	public void notifyLocationChanged() {
//		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for notifyLocationChanged()");
//		PrivacySettings settings = mPrvSvc.getSettings(context.getPackageName(), Process.myUid());
//		if(mPrvSvc != null && settings != null && settings.getNetworkInfoSetting() != PrivacySettings.REAL){
//			//do nothing here
//		}
//		else
//			mNotifier.notifyCellLocation(this);
//	}


	@Override
	public String getLine1AlphaTag() {
		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getLine1AlphaTag()");
		String output;
		
		try {
		    if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
		        PrivacySettings settings = mPrvSvc.getSettings(context.getPackageName());
		        if (settings == null || settings.getLine1NumberSetting() == PrivacySettings.REAL) {
		            output = super.getLine1AlphaTag();
    		        mPrvSvc.notification(context.getPackageName(), PrivacySettings.REAL, PrivacySettings.DATA_LINE_1_NUMBER, output);
		        } else {
		            output = settings.getLine1Number();
		            mPrvSvc.notification(context.getPackageName(), settings.getLine1NumberSetting(), PrivacySettings.DATA_LINE_1_NUMBER, output);
		        }
		} catch (PrivacyServiceException e) {
		    Log.e(P_TAG,"PrivacySipPhone:getLine1AlphaTag: PrivacyServiceException occurred: " + context.getPackageName(), e);
            output = "";
            mPrvSvc.notification(context.getPackageName(), PrivacySettings.ERROR, PrivacySettings.DATA_LINE_1_NUMBER, output);
		}
		return output;
	}


	@Override
	public String getVoiceMailAlphaTag() {
		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getVoiceMailAlphaTag()");
		String packageName = context.getPackageName();
        String output;
        try {
            if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
                PrivacySettings settings = mPrvSvc.getSettings(packageName);
                if (settings == null || settings.getLine1NumberSetting() == PrivacySettings.REAL) {
                    output = super.getVoiceMailAlphaTag();
                    mPrvSvc.notification(packageName, PrivacySettings.REAL, PrivacySettings.DATA_LINE_1_NUMBER, output);
                } else {
                    output = settings.getLine1Number(); // can be empty, custom or random
                    mPrvSvc.notification(packageName, settings.getLine1NumberSetting(), PrivacySettings.DATA_LINE_1_NUMBER, output);
                }
        } catch (PrivacyServiceException e) {
            Log.e(P_TAG,"PrivacySipPhone:getVoiceMailAlphaTag: PrivacyServiceException occurred: " + context.getPackageName(), e);
            output = "";
            mPrvSvc.notification(packageName, PrivacySettings.ERROR, PrivacySettings.DATA_LINE_1_NUMBER, output);
        }
        return output;
	}
	
	@Override
	public String getVoiceMailNumber(){
		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getVoiceMailNumber()");
		String packageName = context.getPackageName();
		String output;
		try {
		    if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
		        PrivacySettings settings = mPrvSvc.getSettings(packageName);
		        if (settings == null || settings.getLine1NumberSetting() == PrivacySettings.REAL) {
    		        output = super.getVoiceMailNumber();
    		        mPrvSvc.notification(packageName, PrivacySettings.REAL, PrivacySettings.DATA_LINE_1_NUMBER, output);
		        } else {
		            output = settings.getLine1Number(); // can be empty, custom or random
		            mPrvSvc.notification(packageName, settings.getLine1NumberSetting(), PrivacySettings.DATA_LINE_1_NUMBER, output);		            
		        }
		} catch (PrivacyServiceException e) {
		    Log.e(P_TAG,"PrivacySipPhone:getVoiceMailNumber: PrivacyServiceException occurred: " + context.getPackageName(), e);
            output = "";
            mPrvSvc.notification(packageName, PrivacySettings.ERROR, PrivacySettings.DATA_LINE_1_NUMBER, output);
		}
        return output;
	}

	@Override
	public String getDeviceId() {
		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getDeviceId()");
		String packageName = context.getPackageName();
        String output;
        try {
            if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
                PrivacySettings settings = mPrvSvc.getSettings(packageName);
                if (settings == null || settings.getDeviceIdSetting() == PrivacySettings.REAL) {
                    output = super.getDeviceId();
                    mPrvSvc.notification(packageName, PrivacySettings.REAL, PrivacySettings.DATA_DEVICE_ID, output);
                } else {
                    output = settings.getDeviceId(); // can be empty, custom or random
                    mPrvSvc.notification(packageName, settings.getDeviceIdSetting(), PrivacySettings.DATA_DEVICE_ID, output);
                }
        } catch (PrivacyServiceException e) {
            Log.e(P_TAG,"PrivacySipPhone:getDeviceId: PrivacyServiceException occurred: " + context.getPackageName(), e);
            output = "";
            mPrvSvc.notification(packageName, PrivacySettings.ERROR, PrivacySettings.DATA_DEVICE_ID, output);
        }
        return output;
	}
	
	@Override
	public String getMeid() {
		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getMeid()");
		String packageName = context.getPackageName();
        String output;
        try {
            if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
                PrivacySettings settings = mPrvSvc.getSettings(packageName);
                if (settings == null || settings.getDeviceIdSetting() == PrivacySettings.REAL) {
                    output = super.getMeid();
                    mPrvSvc.notification(packageName, PrivacySettings.REAL, PrivacySettings.DATA_DEVICE_ID, output);
                } else {
                    output = settings.getDeviceId(); // can be empty, custom or random
                    mPrvSvc.notification(packageName, settings.getDeviceIdSetting(), PrivacySettings.DATA_DEVICE_ID, output);                    
                }
        } catch (PrivacyServiceException e) {
            Log.e(P_TAG,"PrivacySipPhone:getMeid: PrivacyServiceException occurred: " + context.getPackageName(), e);
            output = "";
            mPrvSvc.notification(packageName, PrivacySettings.ERROR, PrivacySettings.DATA_DEVICE_ID, output);
        }
        return output;
	}
	
	@Override
	public String getEsn() {
		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getEsn()");
		String packageName = context.getPackageName();
		String output;
		try {
		    if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
		        PrivacySettings settings = mPrvSvc.getSettings(packageName);
		        if (settings == null || settings.getDeviceIdSetting() == PrivacySettings.REAL) {
		            output = super.getEsn();
		            mPrvSvc.notification(packageName, PrivacySettings.REAL, PrivacySettings.DATA_DEVICE_ID, output);		            
		        } else {
		            output = settings.getDeviceId(); // can be empty, custom or random
		            mPrvSvc.notification(packageName, settings.getDeviceIdSetting(), PrivacySettings.DATA_DEVICE_ID, output);
		        }
		} catch (PrivacyServiceException e) {
		    Log.e(P_TAG,"PrivacySipPhone:getEsn: PrivacyServiceException occurred: " + context.getPackageName(), e);
            output = "";
            mPrvSvc.notification(packageName, PrivacySettings.ERROR, PrivacySettings.DATA_DEVICE_ID, output);
		}
        return output;
	}
	
	@Override
	public String getLine1Number() {
		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getLine1Number()");
		String output;
		try {
		    if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
		        PrivacySettings settings = mPrvSvc.getSettings(context.getPackageName());
		        if (settings == null || settings.getLine1NumberSetting() == PrivacySettings.REAL) {
		            output = super.getLine1Number();
		            mPrvSvc.notification(context.getPackageName(), PrivacySettings.REAL, PrivacySettings.DATA_LINE_1_NUMBER, output);
		        } else {
		            output = settings.getLine1Number();
		            mPrvSvc.notification(context.getPackageName(), settings.getLine1NumberSetting(), PrivacySettings.DATA_LINE_1_NUMBER, output);
		        }
		} catch (PrivacyServiceException e) {
		    Log.e(P_TAG,"PrivacySipPhone:getLine1Number: PrivacyServiceException occurred: " + context.getPackageName(), e);
            output = "";
            mPrvSvc.notification(context.getPackageName(), PrivacySettings.ERROR, PrivacySettings.DATA_LINE_1_NUMBER, output);
		}
		return output;
	}
	
	@Override
	public CellLocation getCellLocation() {
		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getCellLocation()");
		
		try {
		    if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
		        PrivacySettings settings = mPrvSvc.getSettings(context.getPackageName());
		        if (settings == null || (settings.getLocationNetworkSetting() == PrivacySettings.REAL && settings.getLocationGpsSetting() == PrivacySettings.REAL)) {
		            mPrvSvc.notification(context.getPackageName(), PrivacySettings.REAL, PrivacySettings.DATA_LOCATION_NETWORK, null);
		            return super.getCellLocation();
		        } else {
		            mPrvSvc.notification(context.getPackageName(), settings.getLocationNetworkSetting(), PrivacySettings.DATA_LOCATION_NETWORK, null);
	            return new GsmCellLocation();
		    }
		} catch (PrivacyServiceException e) {
		    Log.e(P_TAG,"PrivacySipPhone:getCellLocation: PrivacyServiceException occurred: " + context.getPackageName(), e);
            mPrvSvc.notification(context.getPackageName(), PrivacySettings.ERROR, PrivacySettings.DATA_LOCATION_NETWORK, null);
            return new GsmCellLocation();
		}
	}
	
	@Override
	public PhoneSubInfo getPhoneSubInfo() {
		Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getPhoneSubInfo()");
		
		try {
		    if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
		        PrivacySettings settings = mPrvSvc.getSettings(context.getPackageName());
		        if (settings == null || settings.getNetworkInfoSetting() == PrivacySettings.REAL) {
		            mPrvSvc.notification(context.getPackageName(), PrivacySettings.REAL, PrivacySettings.DATA_LOCATION_NETWORK, null);
		            return super.getPhoneSubInfo();
		        } else {
		            mPrvSvc.notification(context.getPackageName(), settings.getLocationNetworkSetting(), PrivacySettings.DATA_LOCATION_NETWORK, null);
		            return null;
		        }
		} catch (PrivacyServiceException e) {
		    Log.e(P_TAG,"PrivacySipPhone:getPhoneSubInfo: PrivacyServiceException occurred: " + context.getPackageName(), e);
            mPrvSvc.notification(context.getPackageName(), PrivacySettings.ERROR, PrivacySettings.DATA_LOCATION_NETWORK, null);
            return null;
		}
	}
	
	@Override
	public ServiceState getServiceState() {
		try{
			Log.i(P_TAG,"Package: " + context.getPackageName() + " asked for getServiceState()");
			if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
		        PrivacySettings settings = mPrvSvc.getSettings(context.getPackageName());
		        if (settings == null || settings.getNetworkInfoSetting() == PrivacySettings.REAL) {
		            mPrvSvc.notification(context.getPackageName(), PrivacySettings.REAL, PrivacySettings.DATA_LOCATION_NETWORK, null);
		            return super.getServiceState();
		        } else {
		            mPrvSvc.notification(context.getPackageName(), settings.getLocationNetworkSetting(), PrivacySettings.DATA_LOCATION_NETWORK, null);
		            ServiceState output = super.getServiceState();
		            output.setOperatorName("", "", "");
		            return output;
		        }
		} catch (PrivacyServiceException e) {
		    Log.e(P_TAG,"PrivacySipPhone:getServiceState: PrivacyServiceException occurred: " + context.getPackageName(), e);
		    mPrvSvc.notification(context.getPackageName(), PrivacySettings.ERROR, PrivacySettings.DATA_LOCATION_NETWORK, null);
            ServiceState output = super.getServiceState();
            output.setOperatorName("", "", "");
            return output;
		} catch(Exception e) {
			Log.e(P_TAG,"We got exception in getServiceState()-> give fake state", e);
			ServiceState output = super.getServiceState();
			output.setOperatorName("", "", "");
			return output;
		}
	}
	
	@Override
    public Connection dial(String dialNumber) throws CallStateException{
	    try {
	        if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
	            PrivacySettings settings = mPrvSvc.getSettings(context.getPackageName());
	            if (settings == null || settings.getPhoneCallSetting() == PrivacySettings.REAL) {
	                mPrvSvc.notification(context.getPackageName(), PrivacySettings.REAL, PrivacySettings.DATA_PHONE_CALL, null);
	                return super.dial(dialNumber);
	            } else {
	                mPrvSvc.notification(context.getPackageName(), PrivacySettings.EMPTY, PrivacySettings.DATA_PHONE_CALL, null);
	                throw new CallStateException();
	            }
	    } catch (PrivacyServiceException e) {
	        Log.e(P_TAG,"PrivacySipPhone:dial: PrivacyServiceException occurred: " + context.getPackageName(), e);
            mPrvSvc.notification(context.getPackageName(), PrivacySettings.ERROR, PrivacySettings.DATA_PHONE_CALL, null);
            throw new CallStateException();
	    }
    }
	
	@Override
    public Connection dial (String dialNumber, UUSInfo uusInfo) throws CallStateException{
	    try {
	        if (mPrvSvc == null) mPrvSvc = PrivacySettingsManager.getPrivacyService();
	            PrivacySettings settings = mPrvSvc.getSettings(context.getPackageName());
	            if (settings == null || settings.getPhoneCallSetting() == PrivacySettings.REAL) {
	                mPrvSvc.notification(context.getPackageName(), PrivacySettings.REAL, PrivacySettings.DATA_PHONE_CALL, null);
	                return super.dial(dialNumber, uusInfo);
	            } else {
	                mPrvSvc.notification(context.getPackageName(), PrivacySettings.EMPTY, PrivacySettings.DATA_PHONE_CALL, null);
	                throw new CallStateException();
	            }
	    } catch (PrivacyServiceException e) {
	        Log.e(P_TAG,"PrivacySipPhone:dial: PrivacyServiceException occurred: " + context.getPackageName(), e);
            mPrvSvc.notification(context.getPackageName(), PrivacySettings.ERROR, PrivacySettings.DATA_PHONE_CALL, null);
            throw new CallStateException();
	    }
	}
}
