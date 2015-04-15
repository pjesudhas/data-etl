package com.serendio.socialinterface;

public interface FacebookScore {

	public double compute(long pageLikes, float avgPostLikes, float avgPostComment,
			float avgPostShare, double maxLogVal);
}
