package org.OutlierCorrector;

import java.util.HashMap;
import java.util.Iterator;

public class ThreadHandler implements RequestHandler {

    private static final int DEFAULT_WINDOW_SIZE = 100;
    private static final int DEFAULT_PATTERN_COUNT_BEFORE_COMPENSATION = 3;
    private static final int DEFAULT_MAX_PATTERN_FINDING_REQUEST_COUNT = 15;
    private static final int DEFAULT_MAX_OFFSET_BEFORE_PATTERN = 5;
    private static final int DEFAULT_MIN_NUMBER_OF_PATTERN_REPEATS_IN_FINDER = 5;

    private HashMap<String, RequestHandler> handlers = new HashMap<String, RequestHandler>();

    private final RequestHandler outputter;
    private final int windowSize;
    private final int patternCountBeforeCompensation;
    private final int maxPatternFindingRequestCount;
    private final int maxOffsetBeforePattern;
    private final int minNumberOfPatternRepeatsInFinder;

    public ThreadHandler(final RequestHandler outputter) {
	this(outputter, DEFAULT_WINDOW_SIZE, DEFAULT_PATTERN_COUNT_BEFORE_COMPENSATION,
		DEFAULT_MAX_PATTERN_FINDING_REQUEST_COUNT, DEFAULT_MAX_OFFSET_BEFORE_PATTERN,
		DEFAULT_MIN_NUMBER_OF_PATTERN_REPEATS_IN_FINDER);
    }

    public ThreadHandler(RequestHandler outputter, final int windowSize, final int patternCountBeforeCompensation,
	    final int maxPatternFindingRequestCount, final int maxOffsetBeforePattern, final int minNumberOfPatternRepeatsInFinder) {
	this.outputter = outputter;
	this.windowSize = windowSize;
	this.patternCountBeforeCompensation = patternCountBeforeCompensation;
	this.maxPatternFindingRequestCount = maxPatternFindingRequestCount;
	this.maxOffsetBeforePattern = maxOffsetBeforePattern;
	this.minNumberOfPatternRepeatsInFinder = minNumberOfPatternRepeatsInFinder;
    }

    public synchronized void handleRequest(Request request) {
	if (!handlers.containsKey(request.getThreadName())) {
	    handlers.put(request.getThreadName(), new WindowHandler(outputter, windowSize, patternCountBeforeCompensation,
		    maxPatternFindingRequestCount, maxOffsetBeforePattern, minNumberOfPatternRepeatsInFinder));
	}

	handlers.get(request.getThreadName()).handleRequest(request);
    }

    public void flush() {
	Iterator<String> it = handlers.keySet().iterator();
	while (it.hasNext()) {
	    String key = it.next();
	    RequestHandler handler = handlers.get(key);
	    handler.flush();
	}
    }

}
