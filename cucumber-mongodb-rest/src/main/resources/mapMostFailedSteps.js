function() {this.features.forEach(function(feature) { 
	feature.scenarios.forEach(function(scenario) {
		scenario.steps.forEach(function(step) {
			if (step.result.status==="failed"){
				emit(step.name,1);
			}
		})
	})
})}