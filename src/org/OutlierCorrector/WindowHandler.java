package org.OutlierCorrector;

//TODO : 2 windows, one for keeping outliers one for not
//		  the kept outliers will be for detecting new outliers

public class WindowHandler implements RequestHandler {

    Request previousRequest;
    long requestCount = 0;
    long patternCount = 0;

    private final int windowSize;
    private final int patternCountBeforeCompensation;

    private int requestPatternLength;
    private int requestPatternOffset;
    private RequestTypeStats[] requestPatternStats;
    private int positionInRequestPattern = 0;

    RequestHandler outputter;

    private final int maxPatternFindingRequestCount;
    private final int maxOffsetBeforePattern;
    private final int minNumberOfPaternRepeatsInFinder;
    final PatternFinder<String> patternFinder;
    private boolean patternIdentified = false;
    private boolean patternIsReadyToGo = false;
    private int numberOfPatternFindingRequestsSoFar = 0;

    public WindowHandler(final RequestHandler outputter, final int windowSize, final int patternCountBeforeCompensation,
	    final int maxPatternFindingRequestCount, final int maxOffsetBeforePattern, final int minNumberOfPaternRepeatsInFinder) {
	this.windowSize = windowSize;
	this.patternCountBeforeCompensation = patternCountBeforeCompensation;
	this.outputter = outputter;
	this.maxPatternFindingRequestCount = maxPatternFindingRequestCount;
	this.maxOffsetBeforePattern = maxOffsetBeforePattern;
	this.minNumberOfPaternRepeatsInFinder = minNumberOfPaternRepeatsInFinder;

	patternFinder = new PatternFinder<String>(this.maxOffsetBeforePattern);
    }

    private void updateStats(Request newRequest) {
	int previousPositionInRequestPattern = (positionInRequestPattern + requestPatternLength - 1) % requestPatternLength;
	StatsWindow responseTimes = requestPatternStats[positionInRequestPattern].getResponseTimes();
	StatsWindow responseTimesWithoutOutliers = requestPatternStats[positionInRequestPattern]
		.getResponseTimesWithoutOutliers();
	StatsWindow delaysWithoutOutliers = requestPatternStats[previousPositionInRequestPattern].getDelaysWithoutOutliers();

	if (requestPatternStats[positionInRequestPattern].getRequestTypeLabel() == null) {
	    requestPatternStats[positionInRequestPattern].setRequestTypeLabel(newRequest.getRequestTypeLabel());
	} else {
	    if (!requestPatternStats[positionInRequestPattern].getRequestTypeLabel().equals(newRequest.getRequestTypeLabel())) {
		// Scream and shout
		System.out.println("******");
		System.out.println("Pattern mismatch at pattern position " + positionInRequestPattern + ":");
		System.out.println("Expected label : " + requestPatternStats[positionInRequestPattern].getRequestTypeLabel()
			+ " , but got label : " + newRequest.getRequestTypeLabel() + " .");
		System.out.println("******");
		patternIsReadyToGo = false;
		patternIdentified = false; // pattern error, stop compensating
	    }
	}

	double threshold = responseTimes.getAverage() + (3.5 * responseTimes.getStandardDeviation());

	responseTimes.addValue(newRequest.getResponseTime());

	if (newRequest.getResponseTime() > threshold && patternCount > patternCountBeforeCompensation) {
	    // Outlier detected
	    addOutlier(newRequest);
	} else {
	    responseTimesWithoutOutliers.addValue(newRequest.getResponseTime());

	    if (previousRequest != null) {
		delaysWithoutOutliers.addValue(newRequest.getTimeStamp()
			- (previousRequest.getTimeStamp() + previousRequest.getResponseTime()));
	    }
	}

	previousRequest = newRequest;

	requestCount++;
	patternCount = requestCount / requestPatternLength;
	positionInRequestPattern = (positionInRequestPattern + 1) % requestPatternLength;
    }

    private void addOutlier(Request newRequest) {
	StatsWindow responseTimesWithoutOutliers = requestPatternStats[positionInRequestPattern]
		.getResponseTimesWithoutOutliers();
	StatsWindow delaysWithoutOutliers = requestPatternStats[positionInRequestPattern].getDelaysWithoutOutliers();
	long numberMissingPatterns;
	double leftoverPatternFractionTime = 0;

	double timeInThisOutlierRequest = newRequest.getResponseTime() + delaysWithoutOutliers.getAverage();
	double timeThisRequestWouldNormallyTake = responseTimesWithoutOutliers.getAverage() + delaysWithoutOutliers.getAverage();

	double totalPatternTime = 0;
	for (RequestTypeStats requestStats : requestPatternStats) {
	    totalPatternTime += requestStats.getResponseTimesWithoutOutliers().getAverage();
	    totalPatternTime += requestStats.getDelaysWithoutOutliers().getAverage();
	}
	System.out.println("*** Compensating: totalPatternTime = " + totalPatternTime + "( patternLength = "
		+ requestPatternLength + ", " + requestPatternStats.length);

	numberMissingPatterns = (long) ((timeInThisOutlierRequest - timeThisRequestWouldNormallyTake) / totalPatternTime);

	leftoverPatternFractionTime = (timeInThisOutlierRequest - timeThisRequestWouldNormallyTake) % totalPatternTime;

	double effectiveOutlierTime = newRequest.getResponseTime() - timeThisRequestWouldNormallyTake
		- leftoverPatternFractionTime; // centers data over absence
					       // period

	String threadName = newRequest.getThreadName();

	// A "Phantom" request is a request missing from the original stream
	// which we add to the stream here:

	long timeStampFirstPhantom = newRequest.getTimeStamp() + (long) leftoverPatternFractionTime;

	for (long i = 0; i < numberMissingPatterns; i++) {
	    double phantomTimeShift = i * totalPatternTime;

	    double responseTimeAtPhantomPatternStart = effectiveOutlierTime - phantomTimeShift;// -
											       // averageDelay;
	    long timeStampAtPatternStart = timeStampFirstPhantom + (long) phantomTimeShift;

	    double timeSincePhantomPatternStart = 0;

	    for (int j = 0; j < requestPatternLength; j++) {
		int positionInPhantomPattern = (positionInRequestPattern + 1 + j) % requestPatternLength;

		long timeStampAtPhantomRequestStart = timeStampAtPatternStart + (long) timeSincePhantomPatternStart;
		double responseTimeOfPhantomRequest = responseTimeAtPhantomPatternStart - timeSincePhantomPatternStart;

		Request phantomRequest = new Request(timeStampAtPhantomRequestStart, responseTimeOfPhantomRequest, threadName,
			requestPatternStats[positionInPhantomPattern].getRequestTypeLabel(), false /* not an original request */);
		outputter.handleRequest(phantomRequest);

		timeSincePhantomPatternStart += requestPatternStats[positionInPhantomPattern].getResponseTimesWithoutOutliers()
			.getAverage() + requestPatternStats[positionInPhantomPattern].getDelaysWithoutOutliers().getAverage();
	    }
	}
    }

    private void figureOutPattern(Request request) {
	numberOfPatternFindingRequestsSoFar++;
	if (numberOfPatternFindingRequestsSoFar <= maxPatternFindingRequestCount) {
//	    System.out.println("Adding token: " + request.getRequestTypeLabel());
	    patternFinder.addToken(request.getRequestTypeLabel());

	    if (numberOfPatternFindingRequestsSoFar == maxPatternFindingRequestCount) {
//		System.out.println("Computing pattern.");
		int patternLength = patternFinder.getBestPatternLength();
		requestPatternOffset = patternFinder.getBestPatternOffset();
//		System.out.println("patternLength = " + patternLength);
//		System.out.println("requestPatternOffset = " + requestPatternOffset);
		if (patternLength <= ((maxPatternFindingRequestCount - requestPatternOffset) / minNumberOfPaternRepeatsInFinder)) {
		    // Pattern repeats at least 3 times. We are confident in it.
		    requestPatternLength = patternLength;
		    requestPatternStats = new RequestTypeStats[requestPatternLength];
		    for (int i = 0; i < requestPatternLength; i++) {
			requestPatternStats[i] = new RequestTypeStats(null, windowSize);
		    }
		    patternIdentified = true;
//		    System.out.println("Identified pattern.");
		}
	    }
	} else {
	    if (patternIdentified) {
		if ((numberOfPatternFindingRequestsSoFar - requestPatternOffset) % requestPatternLength == 0) {
		    patternIsReadyToGo = true;
//		    System.out.println("Pattern is ready to go.");
		}
	    }
	}
    }

    public void handleRequest(Request request) {
	outputter.handleRequest(request);
	if (patternIsReadyToGo) {
	    updateStats(request);
	} else {
	    figureOutPattern(request);
	}
    }

    @Override
    public void flush() {
	outputter.flush();
    }
}