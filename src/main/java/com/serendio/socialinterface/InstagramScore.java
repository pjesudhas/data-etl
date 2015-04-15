package com.serendio.socialinterface;

public interface InstagramScore {
	
	public double compute(long posts, long netFollowers, float avgPostLikes,
			float avgComments, double maxLogVal);
	
}
