package com.serendio.socialinterface;

public interface TwitterScore {

	public double compute(long tweets, long netFollowers, float avgPostRetweet,
			float avgPostFav, double maxLogVal);
}
