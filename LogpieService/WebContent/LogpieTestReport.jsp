<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import ="com.logpie.service.test.*" %>
<%@ page import ="java.util.ArrayList" %>
    
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Logpie EndPoint Test Report</title>
<script src="./Chart.js-master/Chart.js"></script>
</head>
<body>
<h3>Logpie Endpoint Test - Authentication Service-Register Test Result Report</h3>
		<div style="width:100%">
			<div>
				<canvas id="testResult" height="400" width="100%"></canvas>
			</div>
		</div>
		</br>
<h3>Logpie Endpoint Test - Authentication Service-Register Test Latency Report</h3>
		<div style="width:100%">
			<div>
				<canvas id="testLatency" height="400" width="100%"></canvas>
			</div>
		</div>
			<script>
		var randomScalingFactor = function(){ return Math.round(Math.random()*100)};
		<% TestResultReader testResultReader = new TestResultReader(); ArrayList<String> resultData = testResultReader.getTestResult(); %>
		var lineChartData = {
			labels : [<%=resultData.get(0) %>],
			datasets : [{
						label: "Logpie Test Result",
						fillColor : "rgba(220,220,220,0.2)",
						strokeColor : "rgba(220,220,220,1)",
						pointColor : "rgba(220,220,220,1)",
						pointStrokeColor : "#fff",
						pointHighlightFill : "#fff",
						pointHighlightStroke : "rgba(220,220,220,1)",
						data : [<%=resultData.get(1)%>]
					},]
		}
		var lineChartData2 = {
				labels : [<%=resultData.get(0)%>],
				datasets : [
					{
						label: "Logpie Test Latency",
						fillColor: "rgba(151,187,205,0.2)",
			            strokeColor: "rgba(151,187,205,1)",
			            pointColor: "rgba(151,187,205,1)",
			            pointStrokeColor : "#fff",
						pointHighlightFill : "#fff",
						pointHighlightStroke : "rgba(220,220,220,1)",
				        labelFontFamily : "Arial",
				        labelFontStyle : "normal",
				        labelFontSize : 24,
				        labelFontColor : "#666",
						data : [<%=resultData.get(2) %>]
					},
				]
			}

	window.onload = function(){
		var ctx = document.getElementById("testResult").getContext("2d");
		ctx.canvas.width = 1500;
		ctx.canvas.height = 400;
		window.myLine = new Chart(ctx).Line(lineChartData, {
			bezierCurve: false,
			responsive: true,
		});
		window.myLine.generateLegend();
		
		var ctx2 = document.getElementById("testLatency").getContext("2d");
		ctx2.canvas.width = 1500;
		ctx2.canvas.height = 400;
		window.myLine = new Chart(ctx2).Line(lineChartData2, {
			responsive: true
		});
	}
	</script>
</body>
</html>