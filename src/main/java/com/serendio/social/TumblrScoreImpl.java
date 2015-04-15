package com.serendio.social;

import com.serendio.socialinterface.*;

public class TumblrScoreImpl extends BaseScoreCalculator implements TumblrScore {

	public double compute(long posts, long blogLikes, float avgPostNotes,
			double maxLogVal) {

		long reach = posts * blogLikes;
		float engagement = avgPostNotes;

		return compute(reach, engagement, maxLogVal);

	}

}
