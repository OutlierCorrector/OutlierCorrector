package org.OutlierCorrector.JMeterLogCorrector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import org.OutlierCorrector.Request;
import org.OutlierCorrector.RequestHandler;
import org.OutlierCorrector.RequestStreamSorter;
import org.OutlierCorrector.ThreadHandler;

/**
 * Look to the main function for changing the "style" of the way to read in a log file.
 * Current: JMeter can output to a .csv/.jtl file that gives each request a line, with comma separated values.
 * Per line: item #1 timestamp, item #2 response time, item #4 request type label, item #6 thread ID
 * @author chmiel
 *
 */
public class JMeterLogFix {
    
    static class JMeterLogFixConfiguration {
	String inputFileName = null;
	String outputFileName = null;
	boolean changeLabel = false;
	boolean duplicateStream = false;
	
	public JMeterLogFixConfiguration(final String[] args) {
	    try {
		for (int i = 0; i < args.length; i++) {
		    if (args[i].equals("-i")) {
			inputFileName = args[++i];	
		    } else if (args[i].equals("-o")) {
			outputFileName = args[++i];	
		    } else if (args[i].equals("-c")) {
			changeLabel = true;			
		    } else if (args[i].equals("-d")) {
			duplicateStream = true;			
		    } else {
			throw new Exception("Invalid arg :" + args[i]);
		    }
		}
		if (inputFileName == null) {
		    throw new Exception("Missing input file");
		}
		if (outputFileName == null) {
		    throw new Exception("Missing output file");
		}
	    } catch (Exception e) {
		System.err.println("usage: java JMeterLogFix -i inputFile -o outputFile [-c] [-d]");
		System.err.println(" [-c]    change Labels to indicate correction");
		System.err.println(" [-d]    duplicate original stream under corrected labels");
		System.exit(1);
	    }
	}	
    }

    public static void main(String[] args) throws IOException {
	JMeterLogFixConfiguration config = new JMeterLogFixConfiguration(args);
	
	Scanner s = new Scanner(new BufferedReader(new FileReader(config.inputFileName)));
	FileOutputtingHandler fileOutputter = new FileOutputtingHandler(config.outputFileName, config.changeLabel, config.duplicateStream);

	
	RequestStreamSorter sortingOutputter = new RequestStreamSorter(fileOutputter); 

	RequestHandler threadHandler = new ThreadHandler(sortingOutputter);


	try {
	    s.useDelimiter(",|\\n");
	    int count = 0;
	    double responseTime = 0.0;
	    long timeStamp = 0;
	    String requestLabel = "";
	    String threadName = "";
	    
	    s.nextLine(); // USE WHEN FIRST LINE IS HEADERS
	    
	    // The first string per line is the time stamp (count == 0)
	    // The second string is the response time (count == 1)
	    // The sixth string per line is the thread name (count == 5)
	    while (s.hasNext()) {
		if (count == 1) {
		    responseTime = Double.parseDouble(s.next());
		} else if (count == 0) {
		    timeStamp = Long.parseLong(s.next());
		} else if (count == 2) {
		    requestLabel = s.next();
		} else if (count == 5) {
		    threadName = s.next();
		    threadHandler.handleRequest(new Request(timeStamp, responseTime, threadName, requestLabel, true /* original request */));
		} else {
		    if (count > 5) {
			s.nextLine();
			count = -1;
		    } else
			s.next();
		}

		count++;
	    }
	} finally {
	    if (s != null) {
		s.close();
	    }
	    threadHandler.flush();
	}
    }
}