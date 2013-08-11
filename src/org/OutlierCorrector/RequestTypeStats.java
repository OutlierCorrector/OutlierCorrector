package org.OutlierCorrector;

public class RequestTypeStats {

    private final StatsWindow responseTimes; // including outliers
    private final StatsWindow responseTimesWithoutOutliers;
    private final StatsWindow delaysWithoutOutliers;

    private String requestTypeLabel;

    long requestCount;

    public RequestTypeStats(final String requestTypeLabel, final int windowSize) {
	this.setRequestTypeLabel(requestTypeLabel);
	this.responseTimes = new StatsWindow(windowSize);
	this.responseTimesWithoutOutliers = new StatsWindow(windowSize);
	this.delaysWithoutOutliers = new StatsWindow(windowSize);
    }

    public void addResponseTime(final double responseTime) {
	getResponseTimes().addValue(responseTime);
    }

    public void addNonOutlierResponseTime(final double responseTime) {
	getResponseTimes().addValue(responseTime);
	getResponseTimesWithoutOutliers().addValue(responseTime);
    }

    public void addDelay(final double delay) {
	getDelaysWithoutOutliers().addValue(delay);
    }

    public StatsWindow getResponseTimes() {
	return responseTimes;
    }

    public StatsWindow getResponseTimesWithoutOutliers() {
	return responseTimesWithoutOutliers;
    }

    public StatsWindow getDelaysWithoutOutliers() {
	return delaysWithoutOutliers;
    }

    public String getRequestTypeLabel() {
	return requestTypeLabel;
    }

    public void setRequestTypeLabel(String requestTypeLabel) {
	this.requestTypeLabel = requestTypeLabel;
    }

}
