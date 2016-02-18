package com.userreview.demo;

public class UserReviewConstants {
	public static final String REVIEW_DATA ="C:\\Data\\vivantareviews.txt";
	public static final String ASPECTS_LIST ="C:\\Data\\Test\\Aspects.txt";
	public static final String WORD2VEC_MODEL ="C:\\Compressed Softwares\\GoogleNews-vectors-negative300.bin.gz";
	public static final String PRUNED_ASPECTS_LIST ="C:\\Data\\Test\\PrunedAspects.txt";
	public static final String ASPECTS_CLUSTER ="C:\\Data\\Test\\AspectCluster.txt";
	public static final String SENTIMENT_RESULT="C:\\Data\\Test\\Result.txt";
	
	//Word2Vec cosine similarity factor to limit the cluster sizes
	public static final double COSINE_SIM_FACTOR=0.50;
	
	//Not needed to Change
	public static final String STOPWORDS_LIST ="stopwords_en.txt";
	public static final String BROWN_ASPECTS_LIST ="BrownAspects.txt";
}
