package org.example;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentClass;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static edu.stanford.nlp.neural.rnn.RNNCoreAnnotations.getPredictedClass;

public class AnalyzeSentiment_Old extends RichMapFunction<
        TwitchMessage,
        Tuple2<TwitchMessage, Tuple2<Integer, String>>
        > {

    private StanfordCoreNLP pipeline;


    @Override
    public void open(Configuration configuration) {
        Properties properties = new Properties();
        properties.setProperty(
                "annotators",
                "tokenize, ssplit, parse, sentiment"
        );

        pipeline = new StanfordCoreNLP(properties);
    }

    @Override
    public Tuple2<
            TwitchMessage,
            Tuple2<Integer, String>
            > map(TwitchMessage twitchMessage) {
        return new Tuple2<>(
                twitchMessage,
                getSentiment(twitchMessage.getMessage())
        );
    }


    private Tuple2<Integer, String> getSentiment(String message) {
        int totalScore = 0;
        int sentenceCount = 0;
        String sentimentClass = null;

        int averageScore = 0;
        if (message != null && !message.isEmpty()) {
            Annotation annotation = pipeline.process(message);
            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

            if (sentences != null && !sentences.isEmpty()) {
                for (CoreMap sentence : sentences) {
                    Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
                    int predictedClass = RNNCoreAnnotations.getPredictedClass(tree);
                    totalScore += predictedClass;
                    sentenceCount++;
                }

                // Calculate average sentiment rating
                averageScore = sentenceCount > 0 ? totalScore / sentenceCount : 0;

                // Get the sentiment class based on the average score
                switch (averageScore) {
                    case 0:
                        sentimentClass = "Very negative";
                        break;
                    case 1:
                        sentimentClass = "Negative";
                        break;
                    case 2:
                        sentimentClass = "Neutral";
                        break;
                    case 3:
                        sentimentClass = "Positive";
                        break;
                    case 4:
                        sentimentClass = "Very positive";
                        break;
                    default:
                        sentimentClass = "Unknown";
                }
            }
        }

        return new Tuple2<>(averageScore, sentimentClass);
    }

//    private Tuple2<List<Integer>, List<String>> getSentiment(String message) {
//        List<Integer> scores = new ArrayList<>();
//        List<String> classes = new ArrayList<>();
//
//        if (message != null && !message.isEmpty()) {
//            Annotation annotation = pipeline.process(message);
//
//            annotation.get(SentencesAnnotation.class).forEach(sentence -> {
//                // sentiment score
//                Tree tree = sentence
//                        .get(SentimentAnnotatedTree.class);
//                scores.add(getPredictedClass(tree));
//
//                // sentiment class
//                classes.add(sentence.get(SentimentClass.class));
//            });
//        }
//
//        return new Tuple2<>(scores, classes);
//    }

}


