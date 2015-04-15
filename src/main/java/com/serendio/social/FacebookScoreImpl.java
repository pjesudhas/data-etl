package com.serendio.social;

import com.serendio.socialinterface.*;

public class FacebookScoreImpl extends BaseScoreCalculator implements FacebookScore {
	
	public double compute(long pageLikes, float avgPostLikes, float avgPostComment,
			float avgPostShare, double maxLogVal) {
		
		long reach = pageLikes;
		float engagement =  avgPostLikes + avgPostComment + avgPostShare;
		
		return compute(reach, engagement, maxLogVal);
		
	}
}
