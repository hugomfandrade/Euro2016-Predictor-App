var Q = require("q");
var UpdateScoresOfPredictionOfMatch = require('./UpdateScoresOfPredictionsOfMatch');

module.exports = function (azureMobile) {
	
	var defer = Q.defer();	
	
	var matchData = azureMobile.tables('Match');
	matchData.read().then(function(matches) {
			
		var total = matches.length;
		var completed = 0;
        matches.forEach(function(match) {
			UpdateScoresOfPredictionOfMatch(azureMobile, match).then(function(result) {
				completed = completed + 1;
				console.log("--completed-- " + completed);
				console.log("--total-- " + total);
				
				if (completed === total) {
					defer.resolve();
				}
			}).catch(function(error) {
				completed = completed + 1;
				console.log("--completed (error)-- " + completed);
				console.log("--total-- " + total);
				
				if (completed === total) {
					defer.resolve();
				}
			});
		});
    
	}).catch(function(error) {
        return defer.reject(error);
	});
    
	return defer.promise;
};