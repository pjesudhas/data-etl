package com.serendio.socialinterface;

public interface TumblrScore {

	public double compute(long posts, long blogLikes, float avgPostNotes,
			double maxLogVal);
}
