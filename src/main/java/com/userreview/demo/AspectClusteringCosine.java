package com.userreview.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class AspectClusteringCosine {
public static Multimap main(String[] args)
{
	Multimap<String, String> multimap = ArrayListMultimap.create();
	try {
		OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(UserReviewConstants.ASPECTS_CLUSTER), "UTF-8");
		String word;
		String keyWords[]={"location","service","food","room","vibe","amenities","comfort","value","WiFi","pool"};
		List<String> aspectList=getAspectWords();

		int count=0;
		File gModel = new File(UserReviewConstants.WORD2VEC_MODEL);
		WordVectors wordVectors = WordVectorSerializer.loadGoogleModel(gModel, true);
		WeightLookupTable weightLookupTable = wordVectors.lookupTable();
		Iterator<INDArray> vectors = weightLookupTable.vectors();
		for(int i=0;i<keyWords.length;i++)
		{
			String keyWord=keyWords[i];
			INDArray keyVector=wordVectors.getWordVectorMatrix(keyWord);
		for(int j=0;j<aspectList.size();j++)
		{
			String wordAspect=aspectList.get(j);
			INDArray wordVector = wordVectors.getWordVectorMatrix(wordAspect);
			double sim=Transforms.cosineSim(wordVector,keyVector);
				if(sim>UserReviewConstants.COSINE_SIM_FACTOR)
				{
					multimap.put(keyWord,wordAspect);
				}
			}
		}
		Set<String> keys=multimap.keySet();
		for(String key:keys)
		{
			fileWriter.write(key+" ");
			List<String> words=new ArrayList<String>();
			words.addAll(multimap.get(key));
			for(String simWord:words)
			{
				fileWriter.write(simWord+" ");
			}
			fileWriter.write("\n");
		}
		fileWriter.flush();
		fileWriter.close();
	} catch (Exception e) {
		e.printStackTrace();
	}

return multimap;
}
private static List<String> getAspectWords()
{
	List<String> aspectWords=new ArrayList<String>();
	try
	{
	BufferedReader br = new BufferedReader(new FileReader(UserReviewConstants.PRUNED_ASPECTS_LIST));
	String word;

	while((word=br.readLine())!=null)
	{
		aspectWords.add(word);
	}
	}
	catch(Exception e)
	{
		e.printStackTrace();
	}
	return aspectWords;
	}
}
