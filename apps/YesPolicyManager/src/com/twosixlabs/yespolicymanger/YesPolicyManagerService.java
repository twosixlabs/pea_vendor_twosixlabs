/*
 * This work was authored by Two Six Labs, LLC and is sponsored by a
 * subcontract agreement with Raytheon BBN Technologies Corp. under Prime
 * Contract No. FA8750-16-C-0006 with the Air Force Research Laboratory (AFRL).

 * The Government has unlimited rights to use, modify, reproduce, release,
 * perform, display, or disclose computer software or computer software
 * documentation marked with this legend. Any reproduction of technical data,
 * computer software, or portions thereof marked with this legend must also
 * reproduce this marking.
 *
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

package com.twosixlabs.yespolicymanager;

import android.app.policy.PolicyManagerService;
import android.content.pm.ActivityInfo;
import android.policymanager.ThreadDump;
import android.util.Plog;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.content.ComponentName;

import java.io.File;
import java.util.List;
import java.util.Set;

public class YesPolicyManagerService extends PolicyManagerService {
    private static final String TAG = YesPolicyManagerService.class.getSimpleName();

    @Override
    public boolean onAppInstall(String packageName, String odp) {
		if ( odp != null) {
			Plog.d(TAG, "ODP = " + odp);
		}

		return true;
    }

    @Override
    public void onPrivateDataRequest(String packageName, String permission, String purpose, String pal, String description, ResultReceiver recv) {
	if ( packageName != null && permission != null && purpose != null && pal != null && description != null) {
	    Plog.d(TAG, "Received onPrivateDataRequest for " + packageName +
		   " requesting a transformation of " + permission +
		   " for the purpose of " + purpose +
		   " . PAL = " + pal + ", transofrmation description = " + description);
	}

	if (recv != null){
	    Bundle b = new Bundle();
	    b.putBoolean("allowPerm", true);
	    recv.send(0, b);
	}
    }

    @Override
    public void onDangerousPermissionRequest(String packageName, String permission, String purpose, List<StackTraceElement[]> stackTrace, int flags, ComponentName callingComponent, ComponentName topActivity, ResultReceiver recv) {
		Bundle b = new Bundle();
		b.putBoolean("allowPerm", true);
		b.putString("ODP", "This is the policy...");
		recv.send(0, b);
    }
}
