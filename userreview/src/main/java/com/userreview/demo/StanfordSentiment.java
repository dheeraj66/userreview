package com.userreview.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

/**
 * Hello world!
 *
 */
public class StanfordSentiment {
	public static void main(String[] args) {
		try {
			Set<String> values = new TreeSet<String>();
			Multimap<String, String> map = AspectClusteringCosine.main(null);
			values.addAll(map.values());
			Multimap<String, String> valuePos = ArrayListMultimap.create();
			Multimap<String, String> valueNeg = ArrayListMultimap.create();
			OutputStreamWriter fileWriter = new OutputStreamWriter(
					new FileOutputStream(UserReviewConstants.SENTIMENT_RESULT), "UTF-8");
			Properties props = new Properties();
			props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			File file = new File(UserReviewConstants.REVIEW_DATA);
			String text = "";
			text = Files.toString(file, Charset.forName("utf-8"));

			// create an empty Annotation just with the given text
			Annotation document = new Annotation(text);

			// run all Annotators on this text
			pipeline.annotate(document);

			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			for (CoreMap sentence : sentences) {
				String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
				for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
					String tokenText = token.get(TextAnnotation.class);
					if (values.contains(tokenText.toLowerCase())) {
						if (sentiment.toLowerCase().contains("positive")) {
							valuePos.put(tokenText.toLowerCase(), sentence.toString());
						} else if (sentiment.toLowerCase().contains("negative")) {
							valueNeg.put(tokenText.toLowerCase(), sentence.toString());
						}
					}
				}
			}
			Set<String> keys = map.keySet();
			for (String key : keys) {
				List<String> words = new ArrayList<String>();
				words.addAll(map.get(key));
				Set<String> posSentences = new TreeSet<String>();
				Set<String> negSentences = new TreeSet<String>();
				for (String simWord : words) {
					posSentences.addAll(valuePos.get(simWord));
					negSentences.addAll(valueNeg.get(simWord));
				}
				int posCount = posSentences.size();
				int negCount = negSentences.size();
				int totalCount = posCount + negCount;
				double percentage = 0.0;
				if (totalCount != 0) {
					percentage =(double)((posCount*100) / totalCount);
				}
				fileWriter.write(key + " ");
				fileWriter.write(percentage + "%");
				if (posCount != 0) {
					for (String sent : posSentences) {
						fileWriter.write("++++"+sent);
					}
				}
				if (negCount != 0) {
					for (String sent : negSentences) {
						fileWriter.write("----"+sent);
					}
				}
				fileWriter.write("\n");
			}
		fileWriter.flush();
		fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
