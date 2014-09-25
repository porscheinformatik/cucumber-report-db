package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Johannes Probst
 */
@Controller
@RequestMapping("/rest/statistics/rankings")
public class StatisticsController
{
    @Autowired
    private MongoOperations mongodb;
    
    private static final String MAP_STEPS_DURATIONS ="classpath:mapStepsDurations.js";
    private static final String MAP_MOST_FAILED_STEPS ="classpath:mapMostFailedSteps.js";
    private static final String MAP_MOST_EXECUTED_STEPS ="classpath:mapMostExecutedSteps.js";

    private static final String REDUCE_HIGHEST_SINGLE_STEP_DURATIONS ="classpath:reduceHighestSingleStepDurations.js";
    private static final String REDUCE_CUMULATED_STEP_DURATIONS ="classpath:reduceCumulatedStepDurations.js";

    @RequestMapping(value = "/{collection}/CumulatedStepDurationRanking", method = RequestMethod.GET)
    @ResponseBody
    public void findHighestCumulatedStepDuration(
        HttpServletRequest request,
        @PathVariable(value = "collection") String collection,
        HttpServletResponse response) throws IOException
    {
    	mapReduce(collection, MAP_STEPS_DURATIONS, REDUCE_CUMULATED_STEP_DURATIONS, response);
    }


    @RequestMapping(value = "/{collection}/highestSingleStepDurationRanking", method = RequestMethod.GET)
    @ResponseBody
    public void findHighestSingleStepDuration(
        HttpServletRequest request,
        @PathVariable(value = "collection") String collection,
        HttpServletResponse response) throws IOException
    {
    	mapReduce(collection, MAP_STEPS_DURATIONS, REDUCE_HIGHEST_SINGLE_STEP_DURATIONS, response);
    }
    
    @RequestMapping(value = "/{collection}/mostFailedStepsRanking", method = RequestMethod.GET)
    @ResponseBody
    public void findMostFailed(
        HttpServletRequest request,
        @PathVariable(value = "collection") String collection,
        HttpServletResponse response) throws IOException
    {
    	mapReduce(collection, MAP_MOST_FAILED_STEPS, REDUCE_CUMULATED_STEP_DURATIONS, response);
    }
    
    @RequestMapping(value = "/{collection}/mostExecutedStepsRanking", method = RequestMethod.GET)
    @ResponseBody
    public void findMostExecuted(
        HttpServletRequest request,
        @PathVariable(value = "collection") String collection,
        HttpServletResponse response) throws IOException
    {
    	mapReduce(collection, MAP_MOST_EXECUTED_STEPS, REDUCE_CUMULATED_STEP_DURATIONS, response);
    }
    
    private void mapReduce(String collection, String mapFunction, String reduceFunction, HttpServletResponse response)
    		throws IOException {
    	MapReduceResults<ValueObject> allCollectionSteps=mongodb.mapReduce(collection, mapFunction, reduceFunction, ValueObject.class);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(allCollectionSteps.getRawResults().toString());
    }
}
