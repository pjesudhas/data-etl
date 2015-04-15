package com.serendio.social;

public class BaseScoreCalculator {

	private final int A = 1;
	private final int C = 1;
	private final int D = 100;

	public BaseScoreCalculator() {
		super();
	}

	public double compute(long reach, float engagement, double b) {

		double x = Math.log(reach + 2 * engagement);

		double score = Math.abs((C + (x - A) * (C - D)) / (b - A));
		return score;
	}

}