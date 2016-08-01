package at.porscheinformatik.cucumber.mongodb.rest.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import at.porscheinformatik.cucumber.mongodb.rest.controller.dto.FeatureDTO;
import at.porscheinformatik.cucumber.mongodb.rest.controller.dto.ReportDTO;
import at.porscheinformatik.cucumber.mongodb.rest.controller.dto.ScenarioDTO;
import at.porscheinformatik.cucumber.mongodb.rest.controller.dto.StepDTO;
import at.porscheinformatik.cucumber.mongodb.rest.db.MongoDB;

@Controller
@RequestMapping("/rest/reports/")
public class ReportController
{
    private static final String NL = System.getProperty("line.separator");

    private static final String DEFAULT_SILK_EXECUTION_ID_NAME = "@SILK_ID_%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private MongoDB mongoDB;

    @RequestMapping(value = "{collection}/{executionId}", method = RequestMethod.GET)
    public ResponseEntity<String> checkResultOfExecution(@PathVariable("collection") String collection,
        @PathVariable("executionId") String executionId,
        @RequestParam("obsoleteLimitInDays") Integer obsoleteLimitInDays, HttpServletRequest httpServletRequest)
    {
        LOGGER.info("Fetch bdd report from last run");

        final String execIdTag =
            String.format(DEFAULT_SILK_EXECUTION_ID_NAME, executionId);

        try
        {
            // FIXME (xbk 23/09/2014): replace with mongotemplate
            ReportDTO report = mongoDB.fetchLast(ReportDTO.class, collection, "features.scenarios.tags.name",
                execIdTag);

            for (FeatureDTO feature : report.getFeatures())
            {
                ScenarioDTO scenario;
                if ((scenario = feature.getScenarioByTag(execIdTag)) == null)
                {
                    continue;
                }

                // check if the last report data are not obsolete
                Calendar cal = report.getCalendar();
                cal.add(Calendar.DATE, obsoleteLimitInDays);

                if (isObsolete(cal))
                {
                    return obsoleteScenarioFound(obsoleteLimitInDays, cal);
                }
                else if (isScenarioFailed(scenario))
                {
                    return failedScenarioFound(report, scenario, execIdTag, collection,
                        getUrlBase(httpServletRequest));
                }
                else
                {
                    return passedScenarioFound(report, scenario, execIdTag);
                }
            }

            // if we get here, no matching scenario was found in the database
            // and we cannot complete the process
            final String warning =
                String.format("[Warning] No scenario with id: \"%s\" found in the database!", execIdTag);
            LOGGER.warn(warning);
            return new ResponseEntity<String>(warning, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (Exception e)
        {
            final String error = String.format("[Error] No db entry found with id: \"%s\"!", execIdTag);
            LOGGER.error(error);
            return new ResponseEntity<String>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private boolean isObsolete(final Calendar cal)
    {
        return cal.getTime().compareTo(new Date()) < 0;
    }

    private ResponseEntity<String> obsoleteScenarioFound(final Integer obsoleteLimitInDays, final Calendar cal)
    {
        cal.add(Calendar.DATE, -obsoleteLimitInDays);

        final String warning =
            String.format("[Warning] The data of the last BDD run are obsolete (reportDate: \"%s\")", cal.getTime());
        LOGGER.warn(warning);
        return new ResponseEntity<String>(warning, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Inform JUnit about success
     *
     * @param report
     * @param scenario
     */
    private ResponseEntity<String> passedScenarioFound(ReportDTO report, ScenarioDTO scenario, String idtentifier)
    {
        System.out.println("HIIH");
        String passedMessage =
            "Test passed: (" + report.getDateDTO().get$date() + ") identifier: \"" + idtentifier + "\" - name: \""
                + scenario.getName()
                + "\" ("
                + scenario.getDescription() + ")";
        LOGGER.info(passedMessage);
        return new ResponseEntity<String>(passedMessage, HttpStatus.OK);
    }

    private ResponseEntity<String> failedScenarioFound(ReportDTO report, ScenarioDTO scenario, String idtentifier,
        final String collection, final String requestURI)
    {
        StringBuilder error = new StringBuilder();

        error.append("Test failed: (" + report.getDateDTO().get$date() + ") identifier: \"" + idtentifier
            + "\" - name: \""
            + scenario.getName()
            + " (" + scenario.getDescription() + ")" + NL + NL);

        error.append("  Scenario: " + scenario.getName() + NL);
        error.append("  {" + NL);

        for (StepDTO step : scenario.getSteps())
        {
            error.append("      StepName: " + step.getName() + NL);
            error.append("          Status: " + step.getResult().getStatus() + NL);
            if (step.getResult().getError_message() != null)
            {
                error.append("          Error-Message: " + step.getResult().getError_message() + NL);
            }
        }
        final String bddReportUrl =
            requestURI + "#/reports/" + collection + "/features/" + report.getDateDTO().get$date() + "/feature/"
                + (scenario.getId().split(";"))[0] + "?searchText=" + scenario.getName();
        error.append("      Url: " + bddReportUrl + NL);
        error.append("  }" + NL);

        LOGGER.error(error.toString());
        return new ResponseEntity<String>(error.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean isScenarioFailed(ScenarioDTO scenario)
    {
        return (scenario.getResult().getFailedStepCount() != null
            || scenario.getResult().getSkippedStepCount() != null);
    }

    private String getUrlBase(HttpServletRequest request)
    {
        URL requestUrl;
        try
        {
            requestUrl = new URL(request.getRequestURL().toString());
            String portString = requestUrl.getPort() == -1 ? "" : ":" + requestUrl.getPort();
            return requestUrl.getProtocol() + "://" + requestUrl.getHost() + portString + request.getContextPath()
                + "/";
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
