/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony;

//BEGIN PRIVACY ADDED
//--------------------------------------------------------
//import com.android.internal.telephony.cdma.CDMAPhone;
//import com.android.internal.telephony.cdma.CDMALTEPhone;
//import com.android.internal.telephony.gsm.GSMPhone;
import android.privacy.surrogate.PrivacyCDMAPhone;
import android.privacy.surrogate.PrivacyCDMALTEPhone;
import android.privacy.surrogate.PrivacyGSMPhone;
import android.privacy.surrogate.PrivacySipPhone;
import android.privacy.surrogate.PrivacyPhoneProxy;
//--------------------------------------------------------
//END PRIVACY ADDED

import android.content.Context;
import android.net.LocalServerSocket;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.os.SystemProperties;

import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.sip.SipPhone;
import com.android.internal.telephony.sip.SipPhoneFactory;
import com.android.internal.telephony.uicc.UiccController;

/**
 * {@hide}
 */
public class PhoneFactory {
    static final String LOG_TAG = "PHONE";
    static final int SOCKET_OPEN_RETRY_MILLIS = 2 * 1000;
    static final int SOCKET_OPEN_MAX_RETRY = 3;

    //***** Class Variables

    static private Phone sProxyPhone = null;
    static private CommandsInterface sCommandsInterface = null;

    static private boolean sMadeDefaults = false;
    static private PhoneNotifier sPhoneNotifier;
    static private Looper sLooper;
    static private Context sContext;

    static final int preferredCdmaSubscription =
                         CdmaSubscriptionSourceManager.PREFERRED_CDMA_SUBSCRIPTION;

    //***** Class Methods

    public static void makeDefaultPhones(Context context) {
        makeDefaultPhone(context);
    }

    /**
     * FIXME replace this with some other way of making these
     * instances
     */
    public static void makeDefaultPhone(Context context) {
        synchronized(Phone.class) {
            if (!sMadeDefaults) {
                sLooper = Looper.myLooper();
                sContext = context;

                if (sLooper == null) {
                    throw new RuntimeException(
                        "PhoneFactory.makeDefaultPhone must be called from Looper thread");
                }

                int retryCount = 0;
                for(;;) {
                    boolean hasException = false;
                    retryCount ++;

                    try {
                        // use UNIX domain socket to
                        // prevent subsequent initialization
                        new LocalServerSocket("com.android.internal.telephony");
                    } catch (java.io.IOException ex) {
                        hasException = true;
                    }

                    if ( !hasException ) {
                        break;
                    } else if (retryCount > SOCKET_OPEN_MAX_RETRY) {
                        throw new RuntimeException("PhoneFactory probably already running");
                    } else {
                        try {
                            Thread.sleep(SOCKET_OPEN_RETRY_MILLIS);
                        } catch (InterruptedException er) {
                        }
                    }
                }

                sPhoneNotifier = new DefaultPhoneNotifier();

                // Get preferred network mode
                int preferredNetworkMode = RILConstants.PREFERRED_NETWORK_MODE;
                if (TelephonyManager.getLteOnCdmaModeStatic() == PhoneConstants.LTE_ON_CDMA_TRUE) {
                    preferredNetworkMode = Phone.NT_MODE_GLOBAL;
                }
                int networkMode = Settings.Global.getInt(context.getContentResolver(),
                        Settings.Global.PREFERRED_NETWORK_MODE, preferredNetworkMode);
                Log.i(LOG_TAG, "Network Mode set to " + Integer.toString(networkMode));

                // Get cdmaSubscription
                // TODO: Change when the ril will provides a way to know at runtime
                //       the configuration, bug 4202572. And the ril issues the
                //       RIL_UNSOL_CDMA_SUBSCRIPTION_SOURCE_CHANGED, bug 4295439.
                int cdmaSubscription;
                int lteOnCdma = TelephonyManager.getLteOnCdmaModeStatic();
                switch (lteOnCdma) {
                    case PhoneConstants.LTE_ON_CDMA_FALSE:
                        cdmaSubscription = CdmaSubscriptionSourceManager.SUBSCRIPTION_FROM_NV;
                        Log.i(LOG_TAG, "lteOnCdma is 0 use SUBSCRIPTION_FROM_NV");
                        break;
                    case PhoneConstants.LTE_ON_CDMA_TRUE:
                        cdmaSubscription = CdmaSubscriptionSourceManager.SUBSCRIPTION_FROM_RUIM;
                        Log.i(LOG_TAG, "lteOnCdma is 1 use SUBSCRIPTION_FROM_RUIM");
                        break;
                    case PhoneConstants.LTE_ON_CDMA_UNKNOWN:
                    default:
                        //Get cdmaSubscription mode from Settings.System
                        cdmaSubscription = Settings.Global.getInt(context.getContentResolver(),
                                Settings.Global.PREFERRED_CDMA_SUBSCRIPTION,
                                preferredCdmaSubscription);
                        Log.i(LOG_TAG, "lteOnCdma not set, using PREFERRED_CDMA_SUBSCRIPTION");
                        break;
                }
                Log.i(LOG_TAG, "Cdma Subscription set to " + cdmaSubscription);

                //reads the system properties and makes commandsinterface
                sCommandsInterface = new RIL(context, networkMode, cdmaSubscription);

                // Instantiate UiccController so that all other classes can just call getInstance()
                UiccController.make(context, sCommandsInterface);

                int phoneType = TelephonyManager.getPhoneType(networkMode);
                if (phoneType == PhoneConstants.PHONE_TYPE_GSM) {
                    Log.i(LOG_TAG, "Creating GSMPhone");
                    //BEGIN PRIVACY ADDED
                    //sProxyPhone = new PhoneProxy(new GSMPhone(context,
                    //        sCommandsInterface, sPhoneNotifier));
                    sProxyPhone = new PrivacyPhoneProxy(new PrivacyGSMPhone(context,
                            sCommandsInterface, sPhoneNotifier),context);
                    //END PRIVACY ADDED
                } else if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                    switch (TelephonyManager.getLteOnCdmaModeStatic()) {
                        case PhoneConstants.LTE_ON_CDMA_TRUE:
                            Log.i(LOG_TAG, "Creating CDMALTEPhone");
                            //BEGIN PRIVACY ADDED
                            //sProxyPhone = new PhoneProxy(new CDMALTEPhone(context,
                            //    sCommandsInterface, sPhoneNotifier));
                            sProxyPhone = new PrivacyPhoneProxy(new PrivacyCDMALTEPhone(context,
                                sCommandsInterface, sPhoneNotifier),context);
                            //END PRIVACY ADDED
                            break;
                        case PhoneConstants.LTE_ON_CDMA_FALSE:
                        default:
                            Log.i(LOG_TAG, "Creating CDMAPhone");
                            //BEGIN PRIVACY ADDED
                            //sProxyPhone = new PhoneProxy(new CDMAPhone(context,
                            //        sCommandsInterface, sPhoneNotifier));
                            sProxyPhone = new PrivacyPhoneProxy(new PrivacyCDMAPhone(context,
                                    sCommandsInterface, sPhoneNotifier),context);
                            //END PRIVACY ADDED
                            break;
                    }
                }

                sMadeDefaults = true;
            }
        }
    }

    public static Phone getDefaultPhone() {
        if (sLooper != Looper.myLooper()) {
            throw new RuntimeException(
                "PhoneFactory.getDefaultPhone must be called from Looper thread");
        }

        if (!sMadeDefaults) {
            throw new IllegalStateException("Default phones haven't been made yet!");
        }
       return sProxyPhone;
    }

    public static Phone getCdmaPhone() {
        Phone phone;
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
            switch (TelephonyManager.getLteOnCdmaModeStatic()) {
                case PhoneConstants.LTE_ON_CDMA_TRUE: {
                    //BEGIN PRIVACY ADDED
                    //phone = new CDMALTEPhone(sContext, sCommandsInterface, sPhoneNotifier);
                    phone = new PrivacyCDMALTEPhone(sContext, sCommandsInterface, sPhoneNotifier);
                    //END PRIVACY ADDED
                    break;
                }
                case PhoneConstants.LTE_ON_CDMA_FALSE:
                case PhoneConstants.LTE_ON_CDMA_UNKNOWN:
                default: {
                    //BEGIN PRIVACY ADDED
                    //phone = new CDMAPhone(sContext, sCommandsInterface, sPhoneNotifier);
                    phone = new PrivacyCDMAPhone(sContext, sCommandsInterface, sPhoneNotifier);
                    //END PRIVACY ADDED
                    break;
                }
            }
        }
        return phone;
    }

    public static Phone getGsmPhone() {
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
            Phone phone = new PrivacyGSMPhone(sContext, sCommandsInterface, sPhoneNotifier);
            return phone;
        }
    }

    /**
     * Makes a {@link SipPhone} object.
     * @param sipUri the local SIP URI the phone runs on
     * @return the {@code SipPhone} object or null if the SIP URI is not valid
     */
    public static SipPhone makeSipPhone(String sipUri) {
        return SipPhoneFactory.makePhone(sipUri, sContext, sPhoneNotifier);
    }
}
