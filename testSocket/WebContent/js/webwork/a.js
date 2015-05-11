define(["jquery"], function(_$) {
	_$("#log").text("a is loading....");
	var w;
	//开始计时
	_$("#startBtn").click(function() {
		if (window.Worker) {
			if (w != "undefined") {
				w = new Worker("../../js/webwork/w.js");
			}
			w.onmessage=function(event){
				_$("#log").text(event.data);
			}
		}
	});
	//移除worker
	_$("#stopBtn").click(function(){
		w.terminate();
	});

})