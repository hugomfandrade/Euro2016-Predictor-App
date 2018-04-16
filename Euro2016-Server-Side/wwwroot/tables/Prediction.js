var ValidateToken = require('../authentication_scripts/ValidateToken');
var GetSystemData = require('../utils_scripts/GetSystemData');
var BuildQuery = require("../utils_scripts/BuildQuery");
var queries = require('azure-mobile-apps/src/query');

var table = module.exports = require('azure-mobile-apps').table();

var insertMiddleware = [ValidateToken, function(req, res, next) {
    
    var accountID = req.azureMobile.user.id;
    var prediction = req.body;
    
    if (prediction.UserID !== accountID) {
        return res.status(400).send('Cannot insert with that UserID');
    }
    
    getEnabledMatches(req.azureMobile).then(function(enabledMatches) {
        
        var matchNumber = prediction.MatchNumber;  
        
        if (enabledMatches.indexOf(matchNumber) === -1) {
            return res.status(400).send('Cannot insert prediction for that Match. Match date is past');
        }
        
        return next();
    }).catch(function(error) {
        return res.status(400).send(error);
	});
}];

table.insert.use(insertMiddleware, table.operation);
table.insert(function (context) {
    
    var accountID = context.user.id;
    var prediction = context.req.body;
    var matchNumber = prediction.MatchNumber;
    
    var predictionData = context.req.azureMobile.tables('Prediction');
	var q = queries.create('Prediction').where({ MatchNumber : matchNumber, UserID: accountID});
	
    return predictionData.read(q).then(function(results) {
        if (results.length === 0) {
            
            delete prediction.id;
			return predictionData.insert(prediction);
		}
        
        var oldPrediction = results[0];
        oldPrediction.HomeTeamGoals = prediction.HomeTeamGoals;
        oldPrediction.AwayTeamGoals = prediction.AwayTeamGoals;
        
       return predictionData.update(oldPrediction);
    });
});

table.read.use(ValidateToken, table.operation);
table.read(function (context) {
    console.log(context.req.originalUrl);
    var accountID = context.user.id;
    
    return GetSystemData(context.req.azureMobile).then(function(systemData) {
        
        var query = buildReadQuery(accountID, context.req.query, systemData.SystemDate);
        
        console.log(query);
        
        return context.data.execute({sql: query});
    });
}); 

function buildReadQuery(accountID, query, systemDate) {
    
	var fromQuery =
          'SELECT p.*, ps.Score'
        + ' FROM  Prediction p'
        + ' INNER JOIN Match m ON p.MatchNumber = m.MatchNumber'
        + ' LEFT JOIN PredictionScore ps ON p.id = ps.PredictionID'
        + ' WHERE m.deleted = \'FALSE\' AND'
        +  ' (p.UserID = @UserID OR'
        +  ' (p.UserID != @UserID AND m.DateAndTime < @SystemDate))'
      
	
	var query = BuildQuery(query, fromQuery, null);
    
    var query = 'DECLARE @UserID nvarchar(max) = \'' + accountID + '\''
        + ' DECLARE @SystemDate DATETIMEOFFSET;'
        + ' SET @SystemDate = \'' + systemDate.toISOString() + '\';'
        + ' ' + query;
        
    return query;
}

function getEnabledMatches(azureMobile) {
    
    return GetSystemData(azureMobile).then(function(systemData) {
		
		var systemDate = systemData.SystemDate;
		
	    var matchData = azureMobile.tables('Match');
		var query = queries.create('Match')
				.where('DateAndTime gt ?', systemDate)
				.select('MatchNumber, DateAndTime');
		
		return matchData.read(query).then(function(results) {
			
            var matches = [];
            results.forEach(function(result) {
                matches.push(result.MatchNumber);
            });
			
	        return matches;
	    })
	});
}