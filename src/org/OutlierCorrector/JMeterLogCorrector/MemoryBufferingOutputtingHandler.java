package org.OutlierCorrector.JMeterLogCorrector;

import java.io.IOException;
import java.util.*;

import org.OutlierCorrector.Request;
import org.OutlierCorrector.RequestHandler;

public class MemoryBufferingOutputtingHandler implements RequestHandler {
    private final List<Request> log = (List<Request>) Collections.synchronizedCollection(new ArrayList<Request>());

    public MemoryBufferingOutputtingHandler() throws IOException {
    }

    Request getRequestAtIndex(int index) {
	return log.get(index);
    }

    public void handleRequest(final Request request) {
	log.add(request);
    }

    @Override
    public void flush() {
		
    }
}
