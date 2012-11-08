$(document).ready(function(){
	$('#txtPostcodeFO, #txtPostcodeUU, #txtPostcodeMB').keypress(function (e) {
        if (enterPressed(e)) {
            $(this).next().trigger('click');
        }
    });
});

$('#ButtonFO').click(function(e){
	e.preventDefault();
	var url = 'http://www.guardiandigitalcomparison.co.uk/results.aspx?' +'BB=1&PO=' + $(this).prev().val() + '&source=widget_premiumbb_search';
	window.open(url);
});
$('#ButtonUU').click(function(e){
	e.preventDefault();
	var service = $(this).attr('id').replace('Button','');
	var url = 'http://www.guardiandigitalcomparison.co.uk/results.aspx?'+ 'BB=1&PO=' + $(this).prev().val() + '&source=widget_premiumtv_search';
	window.open(url);
});
$('#ButtonMB').click(function(e){
	e.preventDefault();
	var service = $(this).attr('id').replace('Button','');
	var url = 'http://www.guardiandigitalcomparison.co.uk/results.aspx?'+ 'BB=1&PO=' + $(this).prev().val() + '&source=widget_premiumtv_search';
	window.open(url);
});
function enterPressed(e) {
    var keycode;
    if (window.event) keycode = window.event.keyCode;
    else if (e) keycode = e.which;
    else return false;
    return (keycode == 13);
}