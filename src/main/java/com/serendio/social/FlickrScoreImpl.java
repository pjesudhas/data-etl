package com.serendio.social;

import com.serendio.socialinterface.*;

public class FlickrScoreImpl extends BaseScoreCalculator implements FlickrScore {

	public double compute(long totalPhotos, long netFollowers,
			long testimonial, float avgPostViews, float avgPostFav,
			float avgPostComments, double maxLogVal) {

		long reach = totalPhotos * netFollowers * testimonial;
		float engagement = avgPostViews + avgPostFav + avgPostComments;

		return compute(reach, engagement, maxLogVal);

	}

}
