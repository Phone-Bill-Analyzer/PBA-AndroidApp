package com.ayansh.phonebillanalyzer;

import org.varunverma.CommandExecuter.Invoker;
import org.varunverma.CommandExecuter.ProgressInfo;
import org.varunverma.CommandExecuter.ResultObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ayansh.phonebillanalyzer.application.DeleteRegIdCommand;
import com.ayansh.phonebillanalyzer.application.PBAApplication;
import com.ayansh.phonebillanalyzer.application.SaveRegIdCommand;
import com.ayansh.phonebillanalyzer.ui.DisplayFile;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	public GCMIntentService(){
		super(PBAApplication.SenderId);
	}
	
	@Override
	protected void onMessage(Context context, Intent intent) {

		String message = intent.getExtras().getString("message");
		
		if (message.contentEquals("InfoMessage")) {
			// Show Info Message to the User
			showInfoMessage(intent);
		} else {
			// Process Message
		}		
	}

	private void showInfoMessage(Intent intent) {
		// Show Info Message
		String subject = intent.getExtras().getString("subject");
		String content = intent.getExtras().getString("content");
		String mid = intent.getExtras().getString("message_id");
		if(mid == null || mid.contentEquals("")){
			mid = "0";
		}
		int id = Integer.valueOf(mid);

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Create Intent and Set Extras
		Intent notificationIntent = new Intent(this, DisplayFile.class);

		notificationIntent.putExtra("Title", "Info:");
		notificationIntent.putExtra("Subject", subject);
		notificationIntent.putExtra("Content", content);
		notificationIntent.addCategory(subject);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		Notification notification = new NotificationCompat.Builder(this)
										.setContentTitle(subject)
										.setContentText(content)
										.setSmallIcon(R.drawable.ic_launcher)
										.setContentIntent(pendingIntent).build();

		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = subject;
		notification.when = System.currentTimeMillis();

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;

		nm.notify(id, notification);

	}

	@Override
	protected void onError(Context context, String errorId) {
		// Error while registration / un-registration

	}

	@Override
	protected void onRegistered(Context context, String regId) {
		/*
		 * Device is Registered !
		 * 
		 * 1. Save this Reg Id.
		 * 2. Update this to my server.
		 * 
		 */
		
		// Initialize the application
		PBAApplication.getInstance().setContext(getApplicationContext());
		
		if(regId == null || regId.contentEquals("")){
			return;
		}
		
		Log.v(PBAApplication.TAG, "Registration with GCM success");
		PBAApplication.getInstance().addParameter("RegistrationId", regId);
		
		SaveRegIdCommand command = new SaveRegIdCommand(new Invoker(){

			@Override
			public void NotifyCommandExecuted(ResultObject result) {				
			}

			@Override
			public void ProgressUpdate(ProgressInfo pi) {				
			}}, regId);
		
		command.execute();
		
	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		// Device is Un-registered
		
		// Initialize the application
		PBAApplication.getInstance().setContext(getApplicationContext());
				
		Log.v(PBAApplication.TAG, "Un-Registration with GCM success");
		PBAApplication.getInstance().addParameter("RegistrationId", "");
		
		DeleteRegIdCommand command = new DeleteRegIdCommand(new Invoker(){

			@Override
			public void NotifyCommandExecuted(ResultObject result) {			
			}

			@Override
			public void ProgressUpdate(ProgressInfo pi) {			
			}}, regId);
		
		command.execute();

	}

}