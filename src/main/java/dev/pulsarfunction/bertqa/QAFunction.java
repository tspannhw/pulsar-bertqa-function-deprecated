package dev.pulsarfunction.qafunction;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import ai.djl.Application;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ai.djl.pytorch.zoo.nlp.sentimentanalysis.*;
import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;


/**
 text function for chat

 * DistilBERT model trained by HuggingFace using PyTorch
 *
 https://github.com/awslabs/djl/blob/master/examples/src/main/java/ai/djl/examples/inference/SentimentAnalysis.java
 https://github.com/awslabs/djl/blob/master/examples/docs/sentiment_analysis.md
**/
public class QAFunction implements Function<String, String> {

    /**
     *
     * @param message
     * @return
     * @throws IOException
     * @throws TranslateException
     * @throws ModelException
     */
    private Result predict(String message) throws IOException, TranslateException, ModelException {
        Result result = new Result();

        if ( message == null || message.trim().length() <=0 ) {
            return result;
        }

        Criteria<String, Classifications> criteria =
                Criteria.builder()
                        .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
                        .optEngine( "PyTorch" )
                        .setTypes(String.class, Classifications.class)
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<String, Classifications> model = ModelZoo.loadModel(criteria)) {
            try (Predictor<String, Classifications> predictor = model.newPredictor()) {
                Classifications classifications = predictor.predict(message);
                if ( classifications == null) {
                    return result;
                }
                else {
                    if ( classifications.items() != null && classifications.items().size() > 0) {
                        if (  classifications.topK(5) != null ) {
                            result.setRawClassification( classifications.topK( 5 ).toString() );
                        }
                        for (Classifications.Classification classification : classifications.items()) {
                            try {
                                if (classification != null) {
                                    if ( classification.getClassName().equalsIgnoreCase( "positive" )) {
                                        result.setProbability( classification.getProbability() );
                                        result.setProbabilityPercentage( (classification.getProbability()*100) );
                                    }
                                    else if ( classification.getClassName().equalsIgnoreCase( "negative" )) {
                                        result.setProbabilityNegative( classification.getProbability() );
                                        result.setProbabilityNegativePercentage( (classification.getProbability()*100) );
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /** PROCESS */
    @Override
    public String process(String input, Context context) {
        if ( context != null && context.getLogger() != null) {
            context.getLogger().info("LOG:" + input);
        }

        Result result = null;

        try {
            result = predict(input);
        } catch (Throwable e) {
            if ( context != null && context.getLogger() != null) {
                context.getLogger().error("ERROR:" + e.getLocalizedMessage());
            }
        }

        if ( result != null && result.getRawClassification() != null) {
            String sentiment = "Neutral";
            if (result.getProbability() > result.getProbabilityNegative()) {
                sentiment = "Positive";
            }
            else if (result.getProbability() < result.getProbabilityNegative()) {
                sentiment = "Negative";
            }

            if ( context != null && context.getLogger() != null) {
                context.getLogger().info("sentiment-" + sentiment);
            }

            return String.format( "%s", sentiment);
        }
        else {
            return String.format("Neutral %s", input);
        }
    }
} 
