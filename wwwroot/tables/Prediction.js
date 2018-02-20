var ValidateToken = require('../authentication_scripts/ValidateToken');
var GetSystemData = require('../utils_scripts/GetSystemData');
var queries = require('azure-mobile-apps/src/query');
var Q = require("q");

var table = module.exports = require('azure-mobile-apps').table();

var insertMiddleware = [ValidateToken, function(req, res, next) {
    
	var azureMobile = req.azureMobile;
    var accountID = req.azureMobile.user.id;
    
    var prediction = req.body;
    delete prediction.id;
    
    if (prediction.UserID !== accountID) {
        return res.status(400).send('Cannot insert with that UserID');
    }
    
    getEnabledMatches(azureMobile).then(function(enabledMatches) {
        
        var matchNumber = prediction.MatchNumber;  
        var userID = prediction.UserID;  
        
        
        if (enabledMatches.indexOf(matchNumber) === -1) {
            return res.status(400).send('Cannot insert prediction for that Match. Match date is past');
        }
    
        var predictionData = azureMobile.tables('Prediction');
    	var query = queries.create('Prediction').where({ MatchNumber : matchNumber, UserID: userID});
               
    	predictionData.read(query).then(function(results) {
            if (results.length === 0) {
                
    			predictionData.insert(prediction).then(function(results) {
    				return res.status(200).send(results);
    		    }).catch(function(error) {
                    return res.status(400).send(error);
    			});
                return;
    		}
            
            
            var oldPrediction = results[0];
            oldPrediction.HomeTeamGoals = prediction.HomeTeamGoals;
            oldPrediction.AwayTeamGoals = prediction.AwayTeamGoals;
            
            predictionData.update(oldPrediction).then(function(results) {
    				return res.status(200).send(results);
    		    }).catch(function(error) {
                    return res.status(400).send(error);
    			});
        });
    }).catch(function(error) {
        return res.status(400).send(error);
	});
}];

table.insert.use(insertMiddleware, table.operation);
table.insert(function (context) {
   return context.execute();
});


table.read.use(ValidateToken, table.operation);
table.read(function (context) {
    var accountID = context.user.id;
    var azureMobile = context.req.azureMobile;
    
    return context.execute().then(function(predictions) {
        
        return getEnabledMatches(azureMobile).then(function(enabledMatches) {
            
            for (var i = predictions.length - 1; i >= 0; i--) {
                if (predictions[i].UserID !== accountID) {
                    if (enabledMatches.indexOf(predictions[i].MatchNumber) === -1) {
                        predictions = predictions.splice(i, 1);
                    }
                }
            }
            
            return getPredictionScores(context, predictions).then(function(predictions) {
                
    		    return predictions;
                
            }).catch(function(error) {
                return error;
        	});
        }).catch(function(error) {
            return error;
    	});
    }).catch(function(error) {
        return error;
	});
}); 

function getEnabledMatches(azureMobile) {
	var defer = Q.defer();
    
	GetSystemData(azureMobile).then(function(systemData) {
		
		var systemDate = systemData.SystemDate;
		
	    var matchData = azureMobile.tables('Match');
		var query = queries.create('Match')
				.where('DateAndTime gt ?', systemDate)
				//.where('DateAndTime lt ?', systemDate)
				.select('MatchNumber, DateAndTime');
		
		matchData.read(query).then(function(results) {
			
            var matches = [];
            results.forEach(function(result) {
                matches.push(result.MatchNumber);
            });
			
	        return defer.resolve(matches);
	    }).catch(function(error) {
            return defer.reject(error);
		});
	}).catch(function(error) {
        return defer.reject(error);
	});
    
	return defer.promise;
}


function getPredictionScores(context, predictions) {
    var defer = Q.defer();
    
    query = 'SELECT p.id, ps.Score'
         + ' FROM  Prediction p'
         + ' INNER JOIN PredictionScore ps ON p.id = ps.PredictionID'
         + ' WHERE ' + buildWhereClause(predictions)
    
    context.data.execute({sql: query})
        .then(function (results) {
            
            predictions.forEach(function(prediction) {
                prediction.Score = 0;
                
                results.forEach(function(result) {
                    if (prediction.id === result.id)
                        prediction.Score = result.Score;
                });
            });
            defer.resolve(predictions);
        }).catch(function(error) {
            defer.reject(error);
    	});
    
	return defer.promise;
}

function buildWhereClause(predictions) {
    
    var whereClause = '';
    predictions.forEach(function(prediction) {
        if (whereClause !== '')
            whereClause = whereClause + ' OR ';
        whereClause = whereClause + 'p.id = \'' + prediction.id + '\''
    });
    return whereClause
}