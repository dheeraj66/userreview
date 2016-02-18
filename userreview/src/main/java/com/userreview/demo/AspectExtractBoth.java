package com.userreview.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.canova.api.util.ClassPathResource;

import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;

public class AspectExtractBoth {
	public static void main(String[] args) {
		Set<String> stopWords = getStopWords();
		OutputStreamWriter fileWriter = null;
		String text = "";
		try {
			fileWriter = new OutputStreamWriter(
					new FileOutputStream(UserReviewConstants.ASPECTS_LIST),
					"UTF-8");

			File file = new File(UserReviewConstants.REVIEW_DATA);

			text = Files.toString(file, Charset.forName("utf-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation document = new Annotation(text);

		pipeline.annotate(document);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		Map<String, Integer> wordMap = new TreeMap<String, Integer>();
		for (CoreMap sentence : sentences) {
			Tree tree = sentence.get(TreeAnnotation.class);
			Set<String> result = getNounPhrases(tree);
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String pos = token.get(PartOfSpeechAnnotation.class);
				if (pos.equals("NNS") | pos.equals("NN") | pos.equals("NNP") | pos.equals("NNPS")) {
					try {
						String word = token.get(TextAnnotation.class);
						String neToken = token.get(NamedEntityTagAnnotation.class);
						if (!result.contains(word) & neToken.equals("O")) {
							result.add(word);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			for (String word : result) {
				Integer count = wordMap.get(word);
				count = (count == null ? 1 : count + 1);
				wordMap.put(word, count);
			}
		}
		List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(wordMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
		for (Map.Entry<String, Integer> entry : list) {
			try {
				if (entry.getValue() >= 3 & entry.getKey().length() > 3
						& !stopWords.contains(entry.getKey().toLowerCase())) {
					fileWriter.write(entry.getKey().toLowerCase());
					fileWriter.write("\n");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Completed");
		try {
			fileWriter.flush();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Set<String> getNounPhrases(Tree parse) {
		Set<String> result = new TreeSet<>();
		TregexPattern pattern = TregexPattern.compile("@NP");
		TregexMatcher matcher = pattern.matcher(parse);
		while (matcher.find()) {
			Tree match = matcher.getMatch();
			List<Tree> leaves = match.getLeaves();
			String nounPhrase = Joiner.on(' ').join(Lists.transform(leaves, Functions.toStringFunction()));
			int count = nounPhrase.split(" ").length;
			if (count <= 3) {
				result.add(nounPhrase);
			}
		}
		return result;
	}

	public static Set<String> getStopWords() {
		BufferedReader br;
		String line;
		Set<String> result = new TreeSet<String>();
		try {

			br = new BufferedReader(new FileReader(new ClassPathResource(UserReviewConstants.STOPWORDS_LIST).getFile().getAbsolutePath()));
			while ((line = br.readLine()) != null) {
				result.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
}
