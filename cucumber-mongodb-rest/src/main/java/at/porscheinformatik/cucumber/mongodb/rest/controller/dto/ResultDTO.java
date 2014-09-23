package at.porscheinformatik.cucumber.mongodb.rest.controller.dto;

import java.math.BigInteger;

public class ResultDTO
{
    private BigInteger duration;
    private Integer stepCount;
    private Integer scenarioCount;
    private Integer passedStepCount;
    private Integer skippedStepCount;
    private Integer failedStepCount;

    public BigInteger getDuration()
    {
        return duration;
    }

    public void setDuration(BigInteger duration)
    {
        this.duration = duration;
    }

    public Integer getStepCount()
    {
        return stepCount;
    }

    public void setStepCount(Integer stepCount)
    {
        this.stepCount = stepCount;
    }

    public Integer getScenarioCount()
    {
        return scenarioCount;
    }

    public void setScenarioCount(Integer scenarioCount)
    {
        this.scenarioCount = scenarioCount;
    }

    public Integer getPassedStepCount()
    {
        return passedStepCount;
    }

    public void setPassedStepCount(Integer passedStepCount)
    {
        this.passedStepCount = passedStepCount;
    }

    public Integer getSkippedStepCount()
    {
        return skippedStepCount;
    }

    public void setSkippedStepCount(Integer skippedStepCount)
    {
        this.skippedStepCount = skippedStepCount;
    }

    public Integer getFailedStepCount()
    {
        return failedStepCount;
    }

    public void setFailedStepCount(Integer failedStepCount)
    {
        this.failedStepCount = failedStepCount;
    }

}
