package org.OutlierCorrector;

public class Request {
    final private long timeStamp;
    final private double responseTime;
    final private String threadName;
    final private String requestTypeLabel;
    final private boolean isOriginalRequest;

    public Request(final long timeStamp, final double responseTime, final String threadName,
	    final String requestTypeLabel, final boolean isOriginalRequest) {
	this.timeStamp = timeStamp;
	this.responseTime = responseTime;
	this.threadName = threadName;
	this.requestTypeLabel = requestTypeLabel;
	this.isOriginalRequest = isOriginalRequest;
    }

    public long getTimeStamp() {
	return timeStamp;
    }

    public double getResponseTime() {
	return responseTime;
    }

    public String getThreadName() {
	return threadName;
    }

    public String getRequestTypeLabel() {
	return requestTypeLabel;
    }

    public boolean isOriginalRequest() {
	return isOriginalRequest;
    }

}
