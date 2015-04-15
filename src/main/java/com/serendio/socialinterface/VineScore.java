package com.serendio.socialinterface;

public interface VineScore {

	public double compute(long totalLoops, long netFollowers,
			float avgPostLikes, float avgPostRevine, float avgPostLoops,
			float avgPostComments, double maxLogVal);
}
