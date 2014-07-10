package org.jvalue.ods.notifications.sender;

import org.jvalue.ods.notifications.clients.GcmClient;
import org.jvalue.ods.notifications.clients.HttpClient;


public final class SenderFactory {

	private SenderFactory() { }


	private static GcmSender gcmSender;
	private static HttpSender restSender;


	public static NotificationSender<GcmClient> getGcmSender() throws NotificationException {
		if (gcmSender == null) gcmSender = new GcmSender();
		return gcmSender;
	}


	public static NotificationSender<HttpClient> getRestSender() {
		if (restSender == null) restSender = new HttpSender();
		return restSender;
	}

}
