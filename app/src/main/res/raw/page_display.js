window.addEventListener("load", function(event) {
	document.querySelectorAll("a").forEach(function(element) {
		element.addEventListener("click", handle_link_click, { capture: true });
		element.addEventListener("touchend", handle_link_click, { capture: true });
	});
});

function handle_link_click(event) {
	event.stopPropagation();
	event.preventDefault();
	
	console.log(`Link ${event.target.href} clicked`);
	
	if(event.target.classList.contains("redlink")) {
		console.log("Link is red, not navigating");
		App.NotifyRedlink(event.target.innerText);
		return false;
	}
	
	var pageName = depostify(event.target.href.replace(/^[^\?]*\?/, "")).page
	
	App.ChangePage(pageName);
	
}

// Microsnippet #1 - 130 bytes
function depostify(a){return a.split("&").reduce(function(c,d){d=d.split("=").map(decodeURIComponent);c[d[0]]=+d[1]||d[1];return c;},{})}
