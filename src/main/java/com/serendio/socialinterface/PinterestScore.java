package com.serendio.socialinterface;

public interface PinterestScore {

	public double compute(long totalPins, long boards, long totalLikes,
			long netFollowers, float avgPostPins, float avgPostLikes,
			double maxLogVal);
}
