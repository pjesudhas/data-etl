package com.serendio.socialinterface;

public interface GooglePlusScore {

	public double compute(long totalFollowers, long totalViews,
			float avgPostOnePlus, float avgPostShare, float avgPostComments,
			double maxLogVal);

}
