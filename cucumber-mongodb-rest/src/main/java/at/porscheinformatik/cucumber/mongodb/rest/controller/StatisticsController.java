package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
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
    
    private String mapStepsDurations="classpath:mapStepsDurations.js";
    private String reduceCumulatedStepDurations="classpath:reduceCumulatedStepDurations.js";
    private String reduceHighestSingleStepDurations="classpath:reduceHighestSingleStepDurations.js";
    private String mapMostFailedSteps="classpath:mapMostFailedSteps.js";
    private String mapMostExecutedSteps="classpath:mapMostExecutedSteps.js";
    
    @RequestMapping(value = "/{collection}/CumulatedStepDurationRanking", method = RequestMethod.GET)
    @ResponseBody
    public void findHighestCumulatedStepDuration(
        HttpServletRequest request,
        @PathVariable(value = "collection") String collection,
        HttpServletResponse response) throws IOException
    {
    	mapReduce(collection, mapStepsDurations, reduceCumulatedStepDurations, response);
    }


    @RequestMapping(value = "/{collection}/highestSingleStepDurationRanking", method = RequestMethod.GET)
    @ResponseBody
    public void findHighestSingleStepDuration(
        HttpServletRequest request,
        @PathVariable(value = "collection") String collection,
        HttpServletResponse response) throws IOException
    {
    	mapReduce(collection, mapStepsDurations, reduceHighestSingleStepDurations, response);
    }
    
    @RequestMapping(value = "/{collection}/mostFailedStepsRanking", method = RequestMethod.GET)
    @ResponseBody
    public void findMostFailed(
        HttpServletRequest request,
        @PathVariable(value = "collection") String collection,
        HttpServletResponse response) throws IOException
    {
    	mapReduce(collection, mapMostFailedSteps, reduceCumulatedStepDurations, response);
    }
    
    @RequestMapping(value = "/{collection}/mostExecutedStepsRanking", method = RequestMethod.GET)
    @ResponseBody
    public void findMostExecuted(
        HttpServletRequest request,
        @PathVariable(value = "collection") String collection,
        HttpServletResponse response) throws IOException
    {
    	mapReduce(collection, mapMostExecutedSteps, reduceCumulatedStepDurations, response);
    }
    
    private void mapReduce(String collection, String mapFunction, String reduceFunction, HttpServletResponse response)
    		throws IOException {
    	MapReduceResults<ValueObject> allCollectionSteps=mongodb.mapReduce(collection, mapFunction, reduceFunction, ValueObject.class);
		response.setContentType("application/json");
		response.getWriter().write(allCollectionSteps.getRawResults().toString());
    }
}
