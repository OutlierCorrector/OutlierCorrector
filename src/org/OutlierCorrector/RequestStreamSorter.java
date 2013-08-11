package org.OutlierCorrector;

import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class RequestStreamSorter implements RequestHandler {
    
    private final RequestHandler outputter;
    
    private HashMap<String, Request> latestRequestPerThread = new HashMap<String, Request>();

    PriorityBlockingQueue<Request> incomingRequests = new PriorityBlockingQueue<Request>(10000, compareRequestByTimestamp);
    PriorityBlockingQueue<Request> sortedLatestRequests = new PriorityBlockingQueue<Request>(1000, compareRequestByTimestamp);   
    
    public RequestStreamSorter(final RequestHandler outputter) {
	this.outputter = outputter;
    }
    
    public synchronized void handleRequest(Request request) {
	
	incomingRequests.add(request);
	
	// Update the latestRequestPerThread map:
	Request previousLatestRequest = latestRequestPerThread.get(request.getThreadName());
	latestRequestPerThread.put(request.getThreadName(), request);
	
	// Update the sortedLatestRequests queue:
	sortedLatestRequests.add(request);
	if (previousLatestRequest != null) {
	    sortedLatestRequests.remove(previousLatestRequest);
	}
	
	long timeToWhichWeCanProcess = sortedLatestRequests.peek().getTimeStamp(); // The earliest of per-thread latest
	processRequestsUpToTime(timeToWhichWeCanProcess);	
    }
    
    public synchronized void flush() {
	processRequestsUpToTime(Long.MAX_VALUE);
	outputter.flush();
    }
	
    private void processRequestsUpToTime(long timeToWhichToProcess) {
	Request incomingRequest; 
	while (((incomingRequest = incomingRequests.peek()) != null) &&
		(incomingRequest.getTimeStamp() <= timeToWhichToProcess)) {
	    incomingRequest = incomingRequests.poll();
	    assert(incomingRequest != null);
	    
	    outputter.handleRequest(incomingRequest);    
	}	
    }   
    
    private static CompareRequestByTimestamp compareRequestByTimestamp = new CompareRequestByTimestamp();
    
    static class CompareRequestByTimestamp implements Comparator<Request> {
	public int compare(Request r1, Request r2) {
	    long t1 = r1.getTimeStamp();
	    long t2 = r2.getTimeStamp();
	    return (t1 > t2) ? 1 : ((t1 < t2) ? -1 : 0);
	}
    }
}
