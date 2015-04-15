package com.serendio.socialinterface;

public interface FourSquareScore {

	public double compute(long tips, long netFollowers, float avgPostLikes,
			float avgPostSave, double maxLogVal);
}
