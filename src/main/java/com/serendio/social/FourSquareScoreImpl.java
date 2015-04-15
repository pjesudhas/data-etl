package com.serendio.social;

import com.serendio.socialinterface.*;

public class FourSquareScoreImpl extends BaseScoreCalculator implements
		FourSquareScore {

	public double compute(long tips, long netFollowers, float avgPostLikes,
			float avgPostSave, double maxLogVal) {

		long reach = tips * netFollowers;
		float engagement = avgPostLikes + avgPostSave;

		return compute(reach, engagement, maxLogVal);

	}

}
