package com.serendio.social;

import com.serendio.socialinterface.*;

public class TwitterScoreImpl extends BaseScoreCalculator implements TwitterScore {

	public double compute(long tweets, long netFollowers, float avgPostRetweet,
			float avgPostFav, double maxLogVal) {
		
		long reach = tweets * netFollowers;
		float engagement =  avgPostFav + avgPostRetweet;
		
		return compute(reach, engagement, maxLogVal);
		
	}
}
