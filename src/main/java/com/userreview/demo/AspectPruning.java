package com.userreview.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

import org.canova.api.util.ClassPathResource;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

public class AspectPruning {
	private static WordVectors wordVectors;

	public static void main(String[] args) {
		try {
			File aspectFile = new File(UserReviewConstants.PRUNED_ASPECTS_LIST);
			aspectFile.getParentFile().mkdirs();
			OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(aspectFile), "UTF-8");
			setWordVectors(UserReviewConstants.WORD2VEC_MODEL);
			INDArray aspectCentroid = getCentroid(UserReviewConstants.ASPECTS_LIST, 10);
			INDArray brownCentroid = getCentroid(new ClassPathResource(UserReviewConstants.BROWN_ASPECTS_LIST).getFile().getAbsolutePath(), 25);
			BufferedReader br = new BufferedReader(new FileReader(UserReviewConstants.ASPECTS_LIST));
			String word;
			while (((word = br.readLine()) != null)) 
			{
				INDArray wordVector = wordVectors.getWordVectorMatrix(word);
				double simDomain=Transforms.cosineSim(wordVector,aspectCentroid);
				double simCommon=Transforms.cosineSim(wordVector,brownCentroid);
				if(simCommon<simDomain)
				{
					fileWriter.write(word);
					fileWriter.write("\n");
				}
			}
			fileWriter.flush();
			fileWriter.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setWordVectors(String model) {
		try {
			File gModel = new File(model);
			wordVectors = WordVectorSerializer.loadGoogleModel(gModel, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static INDArray getCentroid(String aspects, int value) {
		INDArray sumVector = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(aspects));
			String word;
			int count = 0;
			while (((word = br.readLine()) != null) & (count < value)) {
				INDArray wordVector = wordVectors.getWordVectorMatrix(word);
				if (count == 0) {
					sumVector = wordVector;
				} else {
					sumVector.add(wordVector);
				}
			
				count++;
			}
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sumVector.div(value);
	}
}
