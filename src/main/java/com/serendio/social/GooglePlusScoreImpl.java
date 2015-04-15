package com.serendio.social;

import com.serendio.socialinterface.*;

public class GooglePlusScoreImpl extends BaseScoreCalculator implements GooglePlusScore {

	public double compute(long totalFollowers, long totalViews,
			float avgPostOnePlus, float avgPostShare, float avgPostComments,
			double maxLogVal) {

		long reach = totalFollowers * totalViews;
		float engagement = avgPostOnePlus + avgPostShare + avgPostComments;

		return compute(reach, engagement, maxLogVal);

	}

}
