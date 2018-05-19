var Q = require("q");
var GetSystemData = require('../utils_scripts/GetSystemData');
var ComputePredictionScore = require('./ComputePredictionScore');

module.exports = function (azureMobile, match) {
	console.log("--UpdateScoresOfPRedictionsOfMatch-- ");
	console.log(match.MatchNumber);
	
	var defer = Q.defer();		
    
	// Get SystemDate
	GetSystemData(azureMobile).then(function(systemData) {
		
		var matchNumber = match.MatchNumber;
		var query = 'SELECT ps.*, p.HomeTeamGoals, p.AwayTeamGoals, p.id AS OriginalPredictionID'
	         + ' FROM Prediction p'
	         + ' LEFT JOIN PredictionScore ps ON p.id = ps.PredictionID'
	         + ' WHERE p.MatchNumber = \'' + matchNumber + '\'';
		var predictionScoreTable = azureMobile.tables('PredictionScore');
    	
	    azureMobile.data.execute({sql: query}).then(function (predictions) { 
				
			var total = predictions.length;
			var completed = 0;
			
			if (completed === total) {
				return defer.resolve(match);
			}
			else {
				
				predictions.forEach(function(prediction) {
					
					prediction.Score = ComputePredictionScore(match, prediction, systemData);
				
					if (prediction.id === null) {
						// Insert
						var predictionID = prediction.OriginalPredictionID;
						var score = prediction.Score;
						predictionScoreTable.insert({PredictionID: predictionID, 
													 Score: score}).then(function(predictionScore) {
							completed = completed + 1;
							
							if (completed === total) {
	    						return defer.resolve(match);
							}
						}).catch(function(error) {
							completed = completed + 1;
								
							if (completed === total) {
	    						return defer.resolve(match);
							}
						});
					}
					else {
						// Update
						delete prediction.HomeTeamGoals;
						delete prediction.AwayTeamGoals;
						delete prediction.OriginalPredictionID;
						
						predictionScoreTable.update(prediction).then(function(predictionScore) {
							
							completed = completed + 1;
							
							if (completed === total) {
	    						return defer.resolve(match);
							}
						}).catch(function(error) {
							
							completed = completed + 1;
							
							if (completed === total) {
	    						return defer.resolve(match);
							}
						});
					}
				});
			}
		}).catch(function(error) {
            return defer.reject(error);
		});
	}).catch(function(error) {
        return defer.reject(error);
	});
    
	return defer.promise;
};