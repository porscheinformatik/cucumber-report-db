package at.porscheinformatik.cucumber.formatter;

import java.io.File;
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
import gherkin.formatter.model.Feature;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Scenario;
import gherkin.formatter.model.ScenarioOutline;
import gherkin.formatter.model.Step;

public abstract class AbstractJsonFormatter implements Formatter, Reporter
{
    protected static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    protected static final Map<String, String> MIME_TYPES_EXTENSIONS = new HashMap<String, String>()
    {
        private static final long serialVersionUID = -3542309561998685099L;
        {
            put("image/bmp", "bmp");
            put("image/gif", "gif");
            put("image/jpeg", "jpg");
            put("image/png", "png");
            put("video/mp4", "mp4");
        }
    };

    protected final List<Map<String, Object>> allFeatures = new ArrayList<Map<String, Object>>();
    protected final NiceAppendable out;

    protected Map<String, Object> currentFeature;
    protected Map<String, Object> currentScenario;
    protected List<Map<String, Object>> currentSteps;
    protected int embeddedIndex = 1;
    protected int currentStepResultIndex;

    protected Date date;

    protected abstract String doEmbedding(String extension, byte[] data);

    protected abstract Appendable jsOut(File htmlReportDir);

    public AbstractJsonFormatter(File htmlReportDir)
    {
        this.out = new NiceAppendable(jsOut(htmlReportDir));
        this.date = new Date();
    }

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
        System.out.println(background);
        newScenario(scenario);
    }

    @Override
    public void scenario(Scenario scenario)
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
    public void scenarioOutline(ScenarioOutline scenarioOutline)
    {
        throw new IllegalArgumentException("There should be no outline");
    }

    @Override
    public void examples(Examples paramExamples)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void step(Step step)
    {
        currentSteps.add(step.toMap());
        addToResultValue(currentScenario, "stepCount", 1);
        addToResultValue(currentFeature, "stepCount", 1);
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
        out.append(new GsonBuilder().setDateFormat(dateFormat).create().toJson(report));
    }

    @Override
    public void close()
    {
        out.close();
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
        // Creating a file instead of using data urls to not clutter the js file
        String extension = MIME_TYPES_EXTENSIONS.get(mimeType);
        if (extension != null)
        {
            String fileName = doEmbedding(extension, data);

            Map<String, String> embedding = new HashMap<String, String>();
            embedding.put("mime_type", mimeType);
            embedding.put("url", fileName);
            getEmbeddings().add(embedding);
        }
    }

    @Override
    public void write(String paramString)
    {
    }

    private void newScenario(Map<String, Object> scenario)
    {
        currentScenario = scenario;
        List<Map<String, Object>> scenarios = (List) currentFeature.get("scenarios");
        if (scenarios == null)
        {
            scenarios = new ArrayList<Map<String, Object>>();
            currentFeature.put("scenarios", scenarios);
        }
        scenarios.add(currentScenario);
        addToResultValue(currentFeature, "scenarioCount", 1);

        currentSteps = new ArrayList<Map<String, Object>>();
        currentStepResultIndex = 0;
        currentScenario.put("steps", currentSteps);
    }

    protected List<Map<String, String>> getEmbeddings()
    {
        Map<String, Object> currentStep = currentSteps.get(currentSteps.size() - 1);
        List<Map<String, String>> embeddings = (List) currentStep.get("embeddings");
        if (embeddings == null)
        {
            embeddings = new ArrayList<Map<String, String>>();
            currentStep.put("embeddings", embeddings);
        }
        return embeddings;
    }

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
}
