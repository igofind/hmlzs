//(function() {

	// 紫云法务
	$(".zyfw ul li").each(function(index) {
		$(this).mouseenter(function() {
			$("ul li:eq(" + index + "), ol li:eq(" + index + ")", ".zyfw").addClass("current").siblings().removeClass("current");
		});
	});

	// gototop
	$jq.scroll($(".gototop"), {
		//duration: "slow",
		offset: 16
	});

//})();

// focus
$(".focus ul").cycle({
	pager: ".focus ol",
	pagerAnchorBuilder: function (idx, slide) {
		return ".focus ol li:eq(" + idx + ") a";
	},
	pagerEvent: "hover",
	pause: true,
	timeout: 1000 * 3
});

// audio
audiojs.events.ready(function () {
	var audio = audiojs.createAll()[0];
	$("#playlist li").click(function (e) {
		$(this).addClass("playing").siblings().removeClass("playing");
		$(".fyxl .name").text($(this).text());
		audio.load($(this).attr("data-src"));
		audio.play();
	});
});

// header
document.getElementsByClassName("datetime")[0].innerText = " " + getCalStr();
// footer
document.getElementById("year").innerText = new Date().getFullYear();