package at.porscheinformatik.cucumber.formatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.formatter.Formatter;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Background;
import gherkin.formatter.model.Examples;
import gherkin.formatter.model.ExamplesTableRow;
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.TagStatement;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

public abstract class AbstractJsonFormatter implements Formatter, Reporter
{
    protected static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    protected final List<Map<String, Object>> allFeatures = new ArrayList<Map<String, Object>>();

    protected Map<String, Object> currentFeature;
    protected Map<String, Object> currentScenario;
    protected List<Map<String, Object>> currentSteps;
    protected int embeddedIndex = 1;
    protected int currentStepResultIndex;

    protected Date date = new LocalDateTime(DateTimeZone.UTC).toDate();

    protected abstract String doEmbedding(String extension, final String mimeType, byte[] data);

    protected abstract NiceAppendable jsOut();

    @Override
    public void uri(String paramString)
    {
        currentFeature = new HashMap<String, Object>();
        currentFeature.put("uri", paramString);
    }

    @Override
    public void feature(Feature feature)
    {
        this.currentFeature.putAll(feature.toMap());
    }

    @Override
    public void background(Background background)
    {
        Map<String, Object> scenario = new HashMap<String, Object>();
        scenario.put("background", background.toMap());
        newScenario(scenario);
    }

    @Override
    public void scenario(Scenario scenario)
    {
        setupScenario(scenario);
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline)
    {
        setupScenario(scenarioOutline);
    }

    private void setupScenario(TagStatement scenario)
    {
        if (currentScenario != null && !currentScenario.containsKey("name"))
        {
            currentScenario.putAll(scenario.toMap());
        }
        else
        {
            newScenario(scenario.toMap());
        }
    }

    @Override
    public void examples(Examples paramExamples)
    {
        if (paramExamples != null)
        {
            List<List<String>> examples = new ArrayList<List<String>>();
            for (ExamplesTableRow example : paramExamples.getRows())
            {
                if (example != null)
                {
                    examples.add(example.getCells());
                }
            }
            currentScenario.put("examples", examples);
        }
    }

    @Override
    public void step(Step step)
    {
        if (step != null)
        {
            currentSteps.add(step.toMap());
            addToResultValue(currentScenario, "stepCount", 1);
            addToResultValue(currentFeature, "stepCount", 1);
        }
    }

    /**
     * End of Feature
     */
    @Override
    public void eof()
    {
        allFeatures.add(currentFeature);
        currentFeature = null;
    }

    @Override
    public void done()
    {
        Map<String, Object> report = new HashMap<String, Object>();
        report.put("features", allFeatures);
        report.put("date", date);
        jsOut().append(new GsonBuilder().setDateFormat(dateFormat).create().toJson(report));
    }

    @Override
    public void close()
    {
        jsOut().close();
    }

    @Override
    public void syntaxError(String paramString1, String paramString2, List<String> paramList, String paramString3,
            Integer paramInteger)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void before(Match paramMatch, Result paramResult)
    {
    }

    @Override
    public void result(Result result)
    {
        currentSteps.get(currentStepResultIndex).put("result", result.toMap());
        currentStepResultIndex += 1;
        if (result.getDuration() != null)
        {
            addToResultValue(currentScenario, "duration", result.getDuration());
            addToResultValue(currentFeature, "duration", result.getDuration());
        }

        if (Result.PASSED.equals(result.getStatus()))
        {
            addToResultValue(currentScenario, "passedStepCount", 1);
            addToResultValue(currentFeature, "passedStepCount", 1);
        }
        else if (Result.FAILED.equals(result.getStatus()))
        {
            addToResultValue(currentScenario, "failedStepCount", 1);
            addToResultValue(currentFeature, "failedStepCount", 1);
        }
        else if (Result.SKIPPED.getStatus().equals(result.getStatus()))
        {
            addToResultValue(currentScenario, "skippedStepCount", 1);
            addToResultValue(currentFeature, "skippedStepCount", 1);
        }
        else
        {
            addToResultValue(currentScenario, "unknownStepCount", 1);
            addToResultValue(currentFeature, "unknownStepCount", 1);
        }
    }

    @Override
    public void after(Match paramMatch, Result paramResult)
    {
    }

    @Override
    public void match(Match paramMatch)
    {
    }

    @Override
    public void embedding(String mimeType, byte[] data)
    {
        String extension = MimeTypeToExtensionsUtil.getExtension(mimeType);
        String fileName = doEmbedding(extension,mimeType,data);

        Map<String, String> embedding = new HashMap<String, String>();
        embedding.put("mime_type", mimeType);
        embedding.put("url", fileName);
        getEmbeddings().add(embedding);
    }

    @Override
    public void write(String paramString)
    {
    }

    @SuppressWarnings("unchecked")
    private void newScenario(Map<String, Object> scenario)
    {
        currentScenario = scenario;
        List<Map<String, Object>> scenarios = (List<Map<String, Object>>) currentFeature.get("scenarios");
        List<Map<String, Object>> scenarioOutlines = (List<Map<String, Object>>) currentFeature.get("scenarioOutlines");
        if (scenarios == null && scenarioOutlines == null)
        {
            scenarios = new ArrayList<Map<String, Object>>();
            scenarioOutlines = new ArrayList<Map<String, Object>>();
            currentFeature.put("scenarios", scenarios);
            currentFeature.put("scenarioOutlines", scenarioOutlines);
        }

        if ("scenario_outline".equals(currentScenario.get("type")))
        {
            scenarioOutlines.add(currentScenario);
        }
        else
        {
            scenarios.add(currentScenario);
            addToResultValue(currentFeature, "scenarioCount", 1);
        }

        currentSteps = new ArrayList<Map<String, Object>>();
        currentStepResultIndex = 0;
        currentScenario.put("steps", currentSteps);
    }

    @SuppressWarnings("unchecked")
    protected List<Map<String, String>> getEmbeddings()
    {
        Map<String, Object> currentStep = currentSteps.get(currentSteps.size() - 1);
        List<Map<String, String>> embeddings = (List<Map<String, String>>) currentStep.get("embeddings");
        if (embeddings == null)
        {
            embeddings = new ArrayList<Map<String, String>>();
            currentStep.put("embeddings", embeddings);
        }
        return embeddings;
    }

    @SuppressWarnings("unchecked")
    private static void addToResultValue(Map<String, Object> featureOrScenario, String key, long i)
    {
        Map<String, Object> result = (Map<String, Object>) featureOrScenario.get("result");
        if (result == null)
        {
            result = new HashMap<String, Object>();
            featureOrScenario.put("result", result);
        }

        Long value = (Long) result.get(key);
        if (value == null)
        {
            value = Long.valueOf(i);
            result.put(key, value);
        }
        else
        {
            result.put(key, value + i);
        }
    }

    protected String getFormattedDate()
    {
        return new SimpleDateFormat(dateFormat).format(date);
    }

    @Override
    public void startOfScenarioLifeCycle(final Scenario scenario)
    {

    }

    @Override
    public void endOfScenarioLifeCycle(final Scenario scenario)
    {

    }
}
