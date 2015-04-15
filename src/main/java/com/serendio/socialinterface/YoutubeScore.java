package com.serendio.socialinterface;

public interface YoutubeScore {

	public double compute(long subscribers, long totalViews, long totalVideos,
			float avgPostView, float avgPostDislike, float favourites,
			float avgPostComment, double maxLogVal);
}
