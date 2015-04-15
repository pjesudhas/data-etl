package com.serendio.social;

import com.serendio.socialinterface.*;

public class YoutubeScoreimpl extends BaseScoreCalculator implements YoutubeScore {

	public double compute(long subscribers, long totalViews, long totalVideos,
			float avgPostView, float avgPostDislike, float favourites,
			float avgPostComment, double maxLogVal) {

		long reach = subscribers * totalViews * totalVideos;
		float engagement = avgPostView + favourites + avgPostComment;

		return compute(reach, engagement, maxLogVal);

	}

}
