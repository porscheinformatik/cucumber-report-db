function getFailedScenarioCount(feature) {
	var failedScenarios = 0;
    if(feature.scenarios !== undefined) {
        feature.scenarios.forEach(function(scenario) {
            if(scenario.result.failedStepCount){
                failedScenarios++;
            }
        });
    }
    return failedScenarios;
}


function getUnknownScenarioCount(feature) {
	var unknownScenarios = 0;
    if(feature.scenarios !== undefined) {
        feature.scenarios.forEach(function (scenario) {
            if (scenario.result.unknownStepCount && !scenario.result.failedStepCount) {
                unknownScenarios++;
            }
        });
    }
	return unknownScenarios;
}

function getPassedScenarioCount(feature) {
	var passedScenarios = 0;
    if(feature.scenarios !== undefined) {
        feature.scenarios.forEach(function (scenario) {
            if (scenario.result.passedStepCount && !scenario.result.failedStepCount) {
                passedScenarios++;
            }
        });
    }
	return passedScenarios;
}



function getResults(reportData){
	var results = [];
	results.push(['Date', 'Passed', 'Unknown', 'Failed']);
	$.each( reportData, function( index, report ) {
		var row=[];
		var date = new Date(report.date.$date);
		
		var failedScenariosSum = 0;
		var unknownScenariosSum = 0;
		var passedScenariosSum = 0;
		
		$.each( report.features, function( index, feature) {
			failedScenariosSum += getFailedScenarioCount(feature);
			unknownScenariosSum += getUnknownScenarioCount(feature);
			passedScenariosSum += getPassedScenarioCount(feature);
		});
		
		row.push(date.toString('dd.MM.yyyy HH:mm:ss'));
		row.push();
		row.push(passedScenariosSum);
		row.push(unknownScenariosSum);
		row.push(failedScenariosSum);
		results.push(row);
	});
	
	return results;
}