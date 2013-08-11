package org.OutlierCorrector;

public class StatsWindow {
    private final int windowSize;
    private final double window[];
    private int totalCount = 0;

    private double sumOfValuesInWindow = 0.0;
    private double sumOfSquaresOfValuesInWindow = 0.0;

    private double average = 0.0;

    public StatsWindow(int size) {
	this.windowSize = size;
	window = new double[size];
    }

    public void addValue(double value) {
	int prevPosition = totalCount % windowSize;

	double prevValue = window[prevPosition];
	window[prevPosition] = value;

	totalCount++;
	int numberOfValuesInWindow = (totalCount > windowSize) ? windowSize : (prevPosition + 1);

	sumOfValuesInWindow -= prevValue;
	sumOfValuesInWindow += value;
	average = sumOfValuesInWindow / numberOfValuesInWindow;

	sumOfSquaresOfValuesInWindow -= prevValue * prevValue;
	sumOfSquaresOfValuesInWindow += value * value;
    }

    public double getAverage() {
	return average;
    }

    public double getStandardDeviation() {
	int numberOfValuesInWindow = (totalCount > windowSize) ? windowSize : (totalCount % windowSize);

	double numerator = numberOfValuesInWindow * sumOfSquaresOfValuesInWindow - Math.pow(sumOfValuesInWindow, 2);
	long denominator = numberOfValuesInWindow * numberOfValuesInWindow;

	return Math.sqrt(numerator / denominator);
    }
}
