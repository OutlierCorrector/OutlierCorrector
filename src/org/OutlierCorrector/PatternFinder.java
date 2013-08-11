package org.OutlierCorrector;

import java.util.ArrayList;
import java.util.List;

/**
 * Identify a pattern in a stream of tokens, starting at offset 0 in the stream
 * 
 * @author chmiel
 * 
 */

public class PatternFinder<T> {
    final List<T> stream = new ArrayList<T>();
    final ArrayList<OffsetPatternFinder<T>> offsetPatternFinders;
    final int maxPotentialOffset;

    private class OffsetPatternFinder<E> {
	final int patternOffset;
	private int patternLength = 1;
	int positionInPattern = 0;

	OffsetPatternFinder(final int patternOffset) {
	    this.patternOffset = patternOffset;
	}

	void addToken(final E token) {
	    if (patternOffset >= stream.size())
		return;

	    if (!stream.get(positionInPattern + patternOffset).equals(token)) {
		System.out.println("Resetting pattern offset " + patternOffset + " becasue " + token + " != "
			+ stream.get(positionInPattern + patternOffset));
		resetPattern();
	    } else {
		positionInPattern = (positionInPattern + 1) % getPatternLength();
	    }
	}

	void resetPattern() {
	    patternLength = stream.size() - patternOffset;
	    positionInPattern = 0;
	}

	int getPatternLength() {
	    return patternLength;
	}

	int getPatternOffset() {
	    return patternOffset;
	}
    }

    public PatternFinder(final int maxPotentialOffset) {
	this.maxPotentialOffset = maxPotentialOffset;
	offsetPatternFinders = new ArrayList<OffsetPatternFinder<T>>(maxPotentialOffset);
	for (int i = 0; i < maxPotentialOffset; i++) {
	    offsetPatternFinders.add(new OffsetPatternFinder<T>(i));
	}
    }

    public void addToken(T token) {
	stream.add(token);
	for (OffsetPatternFinder<T> offsetPatternFinder : offsetPatternFinders) {
	    offsetPatternFinder.addToken(token);
	}
    }

    public int getBestPatternLength() {
	int bestPatternLength = stream.size();
	for (OffsetPatternFinder<T> offsetPatternFinder : offsetPatternFinders) {
	    System.out.println("offsetPatternFinder, offset = " + offsetPatternFinder.getPatternOffset() + " , length = "
		    + offsetPatternFinder.getPatternLength());
	    if (bestPatternLength > offsetPatternFinder.getPatternLength()) {
		bestPatternLength = offsetPatternFinder.getPatternLength();
	    }
	}
	return bestPatternLength;
    }

    public int getBestPatternOffset() {
	int bestPatternLength = stream.size();
	int bestPatternOffset = 0;
	for (OffsetPatternFinder<T> offsetPatternFinder : offsetPatternFinders) {
	    if (bestPatternLength > offsetPatternFinder.getPatternLength()) {
		bestPatternLength = offsetPatternFinder.getPatternLength();
		bestPatternOffset = offsetPatternFinder.getPatternOffset();
	    }
	}
	return bestPatternOffset;
    }

    public int getStreamLength() {
	return stream.size();
    }

}
