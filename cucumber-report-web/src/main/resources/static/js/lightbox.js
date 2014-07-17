
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