/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.nrftoolbox.dfu;

import android.app.Activity;

import no.nordicsemi.android.dfu.DfuBaseService;

public class DfuService extends DfuBaseService {

	@Override
	protected Class<? extends Activity> getNotificationTarget() {
		/*
		 * As a target activity the NotificationActivity is returned, not the MainActivity. This is because the notification must create a new task:
		 * 
		 * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 * 
		 * when user press it. Using NotificationActivity we can check whether the new activity is a root activity (that means no other activity was open before)
		 * or that there is other activity already open. In the later case the notificationActivity will just be closed. System will restore the previous activity.
		 * However if the application has been closed during upload and user click the notification a NotificationActivity will be launched as a root activity.
		 * It will create and start the main activity and terminate itself.
		 * 
		 * This method may be used to restore the target activity in case the application was closed or is open. It may also be used to recreate an activity
		 * history (see NotificationActivity).
		 */
		return NotificationActivity.class;
	}

}
