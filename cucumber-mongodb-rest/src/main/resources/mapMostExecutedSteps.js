function() {this.report.features.forEach(function(feature) {
    if(feature.scenarios !== undefined) {
        feature.scenarios.forEach(function(scenario) {
            scenario.steps.forEach(function(step) {
                if (step.result.status==="failed" || step.result.status==="passed" ){
                    emit(step.name, 1);
                }
            })
        })
    }
})}