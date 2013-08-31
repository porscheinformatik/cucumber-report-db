function openLightbox(id) {
	$.colorbox({
		inline : true,
		width : "75%",
		href : id,
		closeButton : true,
		trapFocus : false
	})
}

function errorLogLightbox(stepId) {
	var stepScope = angular.element(document.getElementById(stepId)).scope();
	var step = stepScope.step;
	var featureUri = stepScope.feature.uri;
	var comments = "";

	if (step.comments) {
		$.each(step.comments, function(index, comment) {
			comments += (comments === "" ? "" : "<br />") + '<dd>'
					+ comment.value + '</dd>';
		});
	}
	$.colorbox({
		html : '<div class="errorLogContent">'
				+ '<h4><strong>Error Log</strong></h4>'
				+ '<button class="btn btn-default btn-xs" type="button" onclick="selectText(\'errorLogCode\')">Select all</button>'
				+ '<pre id="errorLogCode" class="errorLogCode prettyprint lang-java">'
				+ step.result.error_message
				+ '</pre>'
				+ '<dl>'
				+ '<dt>Failed Step:</dt>'
				+ '<dd>'
				+ step.keyword
				+ step.name
				+ '</dd>'
				+ (comments !== "" ? '<br /><dt>Comments:</dt>'
						+ comments : '')
				+ '<br /><dt>Feature File:</dt>' + '<dd>' + featureUri
				+ ":" + step.line + '</dd>' + '</dl>' + '</div>',
		width : "75%",
		trapFocus : false
	});
}

function selectText(containerid) {
	var el = document.getElementById(containerid);
	if (document.selection) {
		document.selection.empty();
		var range = document.body.createTextRange();
		el.focus();
		range.moveToElementText(el);
		range.select();
	} else if (window.getSelection) {
		window.getSelection().removeAllRanges();
		var range = document.createRange();
		range.selectNode(el);
		window.getSelection().addRange(range);
	}
}