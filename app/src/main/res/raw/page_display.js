window.addEventListener("load", function(event) {
	document.querySelectorAll("a").forEach(function(element) {
		element.addEventListener("click", handle_link_click);
	});
});

function handle_link_click(event) {
	event.stopPropagation();
	event.preventDefault();
	
	console.log(`Link ${event.target.href} clicked`);
}
