package com.serendio.social;

import com.serendio.socialinterface.*;

public class InstagramScoreImpl extends BaseScoreCalculator implements
		InstagramScore {

	public double compute(long posts, long netFollowers, float avgPostLikes,
			float avgComments, double maxLogVal) {

		long reach = posts * netFollowers;
		float engagement = avgPostLikes + avgComments;

		return compute(reach, engagement, maxLogVal);

	}

}
