package com.serendio.social;

import com.serendio.socialinterface.*;

public class PinterestScoreImpl extends BaseScoreCalculator implements PinterestScore {

	public double compute(long totalPins, long boards, long totalLikes,
			long netFollowers, float avgPostPins,float avgPostLikes, double maxLogVal) {
		
		long reach = totalPins * boards * totalLikes * netFollowers;
		float engagement =  avgPostPins + avgPostLikes;
		
		return compute(reach, engagement, maxLogVal);
		
	}
}
