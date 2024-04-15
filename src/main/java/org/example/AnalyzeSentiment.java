package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;

import java.util.List;
import java.util.Properties;

import static edu.stanford.nlp.neural.rnn.RNNCoreAnnotations.getPredictedClass;

public class AnalyzeSentiment extends RichMapFunction<TwitchMessage, String>
{

    private StanfordCoreNLP pipeline;
    private Gson gson;

    @Override
    public void open(Configuration configuration) {
        Properties properties = new Properties();
        properties.setProperty(
                "annotators",
                "tokenize, ssplit, parse, sentiment"
        );

        pipeline = new StanfordCoreNLP(properties);
        gson = new Gson();
    }

    @Override
    public String map(TwitchMessage twitchMessage) {
        Tuple2<Integer, String> sentimentResult = getSentiment(twitchMessage.getMessage());
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("channel", twitchMessage.getChannel());
        jsonObject.addProperty("user", twitchMessage.getUser());
        jsonObject.addProperty("message", twitchMessage.getMessage());
        jsonObject.addProperty("sentiment_score", sentimentResult.f0);
        jsonObject.addProperty("sentiment", sentimentResult.f1);
        return gson.toJson(jsonObject);
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

}


