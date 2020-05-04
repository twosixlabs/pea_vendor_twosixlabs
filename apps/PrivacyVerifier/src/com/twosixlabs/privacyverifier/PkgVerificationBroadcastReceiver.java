/*
 * This work was authored by Two Six Labs, LLC and is sponsored by a
 * subcontract agreement with Raytheon BBN Technologies Corp. under Prime
 * Contract No. FA8750-16-C-0006 with the Air Force Research Laboratory (AFRL).

 * The Government has unlimited rights to use, modify, reproduce, release,
 * perform, display, or disclose computer software or computer software
 * documentation marked with this legend. Any reproduction of technical data,
 * computer software, or portions thereof marked with this legend must also
 * reproduce this marking.

 * (C) 2020 Two Six Labs, LLC.  All rights reserved.
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

package com.twosixlabs.privacyverifier;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.policymanager.IPolicyManager;
import android.util.Log;
import android.view.WindowManager;
import com.android.internal.privacy.IPrivacyManager;

import java.io.File;
import java.util.List;
import java.util.Iterator;

import android.privatedata.MicroPALProviderService;

public class PkgVerificationBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = PkgVerificationBroadcastReceiver.class.getSimpleName();

    private static boolean allowInstall = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PACKAGE_NEEDS_VERIFICATION.equals(intent.getAction())) {
            this.handleVerification(context, intent);
        }
    }

    private void handleVerification(Context context, Intent intent) {
        Log.d(TAG, "handleVerification(): Privacy Matched intent, handling verification...");
        PackageManager pm = context.getPackageManager();
        int vId = intent.getIntExtra(PackageManager.EXTRA_VERIFICATION_ID, -1);

        IPrivacyManager privacyManagerService = IPrivacyManager.Stub.asInterface(ServiceManager.getService("privacy_manager"));
        IPolicyManager policyManager = null;

        try {
            policyManager = privacyManagerService.getCurrentManager();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        PackageParser.Package appPackage = getPackage(intent);
        if (policyManager != null && appPackage != null) {
            String packageName = appPackage.packageName;
            boolean isPAL = isListeningForAction(appPackage, MicroPALProviderService.PRIVACY_SERVICE_ACTION);
            Log.d(TAG, "handleVerification(): PackageParser.Package.mRealPackage=" + packageName);
            Log.d(TAG, String.format("handleVerification(): %s isListeningForAction(PRIVACY_SERVICE_ACTION)=%s", packageName, "" + isPAL));

            // NOTE: Make sure this matches the value in 
            //       frameworks/base/services/core/java/com/android/server/privacy/PrivacyManagerService.java
            final String POLICY_MANAGER_ACTION = "android.app.action.DEVICE_POLICY_MANAGER_START";
            boolean isPolicyManager = isListeningForAction(appPackage, POLICY_MANAGER_ACTION);
            Log.d(TAG, String.format("handleVerification(): %s isListeningForAction(POLICY_MANAGER_ACTION)=%s", packageName, "" + isPAL));

            if(isPAL) {
                Log.d(TAG, String.format("handleVerification(): handling %s as a PAL", packageName));
                handleKeyVerification(context, intent);
            // TODO: Are we not notifying PolicyManager on install of a PAL???
            } else if(isPolicyManager) {
                Log.d(TAG, String.format("handleVerification(): handling %s as a policy manager", packageName));
                handleKeyVerification(context, intent);
            
            } else {
                Log.d(TAG, String.format("handleVerification(): handling %s as a non-PAL", packageName));
                try {
                    allowInstall = policyManager.onAppInstall(appPackage.packageName, appPackage.offDevicePolicy);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    allowInstall = false;
                }
            }

        }

        if (allowInstall) {
            pm.verifyPendingInstall(vId, PackageManager.VERIFICATION_ALLOW);
        } else {
            pm.verifyPendingInstall(vId, PackageManager.VERIFICATION_REJECT);
        }
    }

    private PackageParser.Package getPackage(Intent intent) {
        File apkFile = new File(intent.getData().getPath());

        PackageParser pp = new PackageParser();
        int parseFlags = PackageParser.PARSE_CHATTY;

        try {
            PackageParser.Package parsedPackage = pp.parsePackage(apkFile, parseFlags);
            return parsedPackage;

        } catch (PackageParser.PackageParserException e) {
            Log.e(TAG, "Error parsing package: " + e.toString());
            return null;
        }
    }

    private void handleKeyVerification(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        int vId = intent.getIntExtra(PackageManager.EXTRA_VERIFICATION_ID, -1);

        try {
            PackageInfo systemInfo = pm.getPackageInfo("com.android.settings", PackageManager.GET_SIGNATURES);
            PackageParser.ApkLite appApkLite = getApkLite(intent);
            allowInstall = (appApkLite != null && appApkLite.signingDetails.signatures != null && systemInfo.signatures[0].equals(appApkLite.signingDetails.signatures[0]));
        } catch (PackageManager.NameNotFoundException e) {
            allowInstall = false;
            e.printStackTrace();
        }

        if(!allowInstall) {
            Log.d(TAG, "handleKeyVerification(): Installation failed package is not signed with correct key");

            // No need to make PackageManager wait any longer than needed
            pm.verifyPendingInstall(vId, PackageManager.VERIFICATION_REJECT);

            // Notify user via System Alert Dialog
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("Invalid Signature")
                .setMessage("Installation failed: Attempted to install policy manager or uPAL module without the correct key!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    })
                .create();

            alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            alertDialog.show();
        }
    }

    private static boolean isListeningForAction(PackageParser.Package appPackage, String action) {
        List<PackageParser.Service> services = appPackage.services;

        if(services != null) {
            for (PackageParser.Service s : services) {
                for (PackageParser.ServiceIntentInfo intentFilter : s.intents) {
                    for(Iterator<String> it = intentFilter.actionsIterator(); it.hasNext(); ) {
                        String serviceAction = it.next();
                        if(serviceAction.equals(action)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static PackageParser.ApkLite getApkLite(Intent intent) {
        String apkPath = intent.getData().getPath() + "/base.apk";
        File apkFile = new File(apkPath);

        PackageParser pp = new PackageParser();
        int parseFlags = PackageParser.PARSE_COLLECT_CERTIFICATES;

        try {
            PackageParser.ApkLite parsedApkLite = pp.parseApkLite(apkFile, parseFlags);
            return parsedApkLite;
        } catch (PackageParser.PackageParserException e) {
            Log.e(TAG, "Error parsing apk: " + e.toString());
            return null;
        }
    }
}
