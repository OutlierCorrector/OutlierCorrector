package org.OutlierCorrector;

public interface RequestHandler {
    
    public void flush();
    
    public void handleRequest(Request request);
}
