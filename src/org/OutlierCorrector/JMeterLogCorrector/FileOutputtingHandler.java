package org.OutlierCorrector.JMeterLogCorrector;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.OutlierCorrector.Request;
import org.OutlierCorrector.RequestHandler;

public class FileOutputtingHandler implements RequestHandler {
    final String fileName;
    final PrintStream fileStream;
    final boolean changeLabel;
    final boolean duplicateStream;

    public FileOutputtingHandler(final String fileName, final boolean changeLabel, final boolean duplicateStream) throws IOException {
	this.fileName = fileName;
	this.changeLabel = changeLabel;
	this.duplicateStream = duplicateStream;
	fileStream = new PrintStream(new FileOutputStream(fileName));
    }

    private synchronized void writeToFile(Request toWrite, String labelToUse) throws IOException {
	fileStream.print(toWrite.getTimeStamp() + ",");
	fileStream.print((int) toWrite.getResponseTime() + ",");
	fileStream.print(labelToUse + ",200,OK," + toWrite.getThreadName() + ",");
	fileStream.print("text,true,0,0\n");
    }

    public void close() throws IOException {
	fileStream.close();
    }

    @Override
    public void handleRequest(Request request) {
	try {
	    if (duplicateStream && request.isOriginalRequest()) {
		    writeToFile(request, request.getRequestTypeLabel());		
	    }
	    
	    String label;
	    if (changeLabel) {
		if (!duplicateStream) {
		    label = request.isOriginalRequest() ? "" : "-Compensation";
		}
		label = request.getRequestTypeLabel() + "-Compensated";		
	    } else {
		label = request.getRequestTypeLabel();
	    } 
	    
	    writeToFile(request, label);
	} catch (IOException ex) {
	    System.err.println(ex);
	}
    }

    @Override
    public void flush() {
	fileStream.flush();
    }
    
    
    
}
