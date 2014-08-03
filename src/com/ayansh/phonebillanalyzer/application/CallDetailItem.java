package com.ayansh.phonebillanalyzer.application;

import org.json.JSONException;
import org.json.JSONObject;

public class CallDetailItem {

	private String callDate, callTime, phoneNumber, duration, comments;
	private float cost;
	
	public CallDetailItem(){
		callDate = callTime = phoneNumber = duration = comments = "";
		cost = 0;
	}
	
	public CallDetailItem(JSONObject cd) {

		try {
			
			callDate = cd.getString("callDate");
			callTime = cd.getString("callTime");
			phoneNumber = cd.getString("phoneNumber");
			duration = cd.getString("duration");
			cost = (float) cd.getDouble("cost");
			comments = cd.getString("comments");
			
		} catch (JSONException e) {
			// Can't do anything :(
		}
		
	}

	public String getCallDate() {
		return callDate;
	}
	public void setCallDate(String callDate) {
		this.callDate = callDate;
	}
	
	public String getCallTime() {
		return callTime;
	}
	public void setCallTime(String callTime) {
		this.callTime = callTime;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	public float getCost() {
		return cost;
	}
	public void setCost(float cost) {
		this.cost = cost;
	}
	
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	
}