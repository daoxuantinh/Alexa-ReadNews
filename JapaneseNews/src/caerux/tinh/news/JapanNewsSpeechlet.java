package caerux.tinh.news;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.speechlet.interfaces.audioplayer.AudioItem;
import com.amazon.speech.speechlet.interfaces.audioplayer.AudioPlayer;
import com.amazon.speech.speechlet.interfaces.audioplayer.PlayBehavior;
import com.amazon.speech.speechlet.interfaces.audioplayer.Stream;
import com.amazon.speech.speechlet.interfaces.audioplayer.directive.PlayDirective;
import com.amazon.speech.speechlet.interfaces.audioplayer.request.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This sample shows how to create a Lambda function for handling Alexa Skill requests that:
 *
 * <ul>
 * <li><b>Custom slot type</b>: demonstrates using custom slot types to handle a finite set of known values</li>
 * </ul>
 * <p>
 * <h2>Examples</h2>
 * <p>
 * <b>One-shot model</b>
 * <p>
 * User: "Alexa, ask Minecraft Helper how to make paper."
 * <p>
 * Alexa:"(reads back recipe for paper)."
 */
public class JapanNewsSpeechlet implements SpeechletV2, AudioPlayer {
    private static final Logger log = LoggerFactory.getLogger(JapanNewsSpeechlet.class);

    public static final String audio_url = "https://s3.amazonaws.com/alexademo.ninja/maxi80/jingle.m4a";
    /**
     * The key to get the item from the intent.
     */
    private static final String CATEGORY_ITEM = "category";

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        log.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());

        // Here we are prompting the user for input
        return getWelcomeResponse();
    }

    private SpeechletResponse getWelcomeResponse() {
        // Create the welcome message.
        String speechText =
                "日本ニュースです。どんなニュースを聞きたいですか。";
        String repromptText =
                "どんなニュースですか。";

//        return getSpeechletResponse(speechText, repromptText, true);
         return getAudioResponse(audio_url);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                requestEnvelope.getSession().getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("NewsIntent".equals(intentName)) {

            return getRecipe(intent);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelp();
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("さようなら");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("さようなら");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            String errorSpeech = "これはサポートされていません。 もう一度試してみてください。";
            return newAskResponse(errorSpeech, errorSpeech);
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        log.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());

        // any cleanup logic goes here
    }

    /**
     * Creates a {@code SpeechletResponse} for the RecipeIntent.
     *
     * @param intent
     *            intent for the request
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getRecipe(Intent intent) {
        Slot CategorySlot = intent.getSlot(CATEGORY_ITEM);
        if (CategorySlot != null && CategorySlot.getValue() != null) {
            String categoryName = CategorySlot.getValue();

            String news = null;
            try {
//                news = Exchange.getExchange(Exchange.get(categoryName), Exchange.get(targetName));
                news = News.getNews(categoryName);
            } catch (IOException e) {
                String speechOutput =
                        "申し訳ありません、ニュースができません";
                String repromptSpeech = "あとでいいですか";
                return newAskResponse(speechOutput, repromptSpeech);
            }

            if (news != null) {
                // If we have the news, return it to the user.
                PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
                outputSpeech.setText(news);

                SimpleCard card = new SimpleCard();
                card.setContent(news);

                return SpeechletResponse.newTellResponse(outputSpeech, card);
            } else {
                // We don't have a news, so keep the session open and ask the user for another
                // item.
                String speechOutput =
                        "申し訳ありません、私は現在、" + categoryName + "に為替できません";
                String repromptSpeech = "他に何ができる？";
                return newAskResponse(speechOutput, repromptSpeech);
            }
        } else {
            // There was no item in the intent so return the help prompt.
            String speechOutput =
                    "わかりません";
            String repromptText =
                    "もう一度お願いします";
            return newAskResponse(speechOutput, repromptText);
        }
    }

    /**
     * Creates a {@code SpeechletResponse} for the HelpIntent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelp() {
        String speechOutput =
                "日本ニュースです。どんなニュースを聞きたいですか";
        String repromptText =
                "全部聞きたいですか";
        return newAskResponse(speechOutput, repromptText);
    }

    /**
     * Wrapper for creating the Ask response. The OutputSpeech and {@link Reprompt} objects are
     * created from the input strings.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(stringOutput);

        PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
        repromptOutputSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);

        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
                                                   boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        if (isAskResponse) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }

    private SpeechletResponse getAudioResponse(String url){

        Stream stream = new Stream();
        stream.setUrl(url);
        stream.setOffsetInMilliseconds(0);
//        stream.setExpectedPreviousToken(requestEnvelope.getRequest().getToken());
        stream.setToken(url);

        AudioItem audioItem = new AudioItem();
        audioItem.setStream(stream);

        PlayDirective playDirective = new PlayDirective();
        playDirective.setAudioItem(audioItem);
        playDirective.setPlayBehavior(PlayBehavior.REPLACE_ALL);

        List<Directive> directives = new ArrayList<>();
        directives.add(playDirective);

        SpeechletResponse response = new SpeechletResponse();
        response.setDirectives(directives);

        System.out.println(response);
        return response;
    }

    @Override
    public SpeechletResponse onPlaybackFailed(SpeechletRequestEnvelope<PlaybackFailedRequest> speechletRequestEnvelope) {
        return null;
    }

    @Override
    public SpeechletResponse onPlaybackFinished(SpeechletRequestEnvelope<PlaybackFinishedRequest> speechletRequestEnvelope) {
        return null;
    }

    @Override
    public SpeechletResponse onPlaybackNearlyFinished(SpeechletRequestEnvelope<PlaybackNearlyFinishedRequest> speechletRequestEnvelope) {
        return null;
    }

    @Override
    public SpeechletResponse onPlaybackStarted(SpeechletRequestEnvelope<PlaybackStartedRequest> speechletRequestEnvelope) {
        return null;
    }

    @Override
    public SpeechletResponse onPlaybackStopped(SpeechletRequestEnvelope<PlaybackStoppedRequest> speechletRequestEnvelope) {
        return null;
    }
}