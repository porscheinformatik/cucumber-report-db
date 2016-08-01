function() {
  this.report.features.forEach(function(feature) {
    if(feature.scenarios != null) {
        feature.scenarios.forEach(function (scenario) {
            scenario.steps.forEach(function (step) {
                if (step.result.status === "failed") {
                    emit(step.name, 1);
                }
            })
        })
    }
})}