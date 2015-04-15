package com.serendio.socialinterface;

public interface FlickrScore {

	public double compute(long totalPhotos, long netFollowers,
			long testimonial, float avgPostViews, float avgPostFav,
			float avgPostComments, double maxLogVal);
}
