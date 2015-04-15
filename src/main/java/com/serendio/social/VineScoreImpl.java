package com.serendio.social;

import com.serendio.socialinterface.*;

public class VineScoreImpl extends BaseScoreCalculator implements VineScore {

	public double compute(long totalLoops, long netFollowers,
			float avgPostLikes, float avgPostRevine, float avgPostLoops,
			float avgPostComments, double maxLogVal) {

		long reach = totalLoops * netFollowers;
		float engagement = avgPostLikes + avgPostRevine + avgPostLoops + avgPostComments;

		return compute(reach, engagement, maxLogVal);

	}

}
