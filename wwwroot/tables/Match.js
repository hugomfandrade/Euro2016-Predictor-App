var ValidateToken = require('../authentication_scripts/ValidateToken');
var AuthorizationAdmin = require('../authentication_scripts/AuthorizationAdmin');
var UpdateScoresOfPredictionsOfMatch = require('../processing_scripts/UpdateScoresOfPredictionsOfMatch');
var queries = require('azure-mobile-apps/src/query');

var table = module.exports = require('azure-mobile-apps').table();

var insertMiddleware = [ValidateToken, AuthorizationAdmin, function(req, res, next) {
    
	var azureMobile = req.azureMobile;
    var match = req.body;
    var matchNumber = match.MatchNumber;
    delete match.id;
    
    var matchData = azureMobile.tables('Match');
	var query = queries.create('Match').where({ MatchNumber : matchNumber });
   
	matchData.read(query).then(function(results) {
        if (results.length === 0) {
			matchData.insert(match).then(function(results) {
				return res.status(200).send(results);
		    }).catch(function(error) {
                return res.status(400).send(error);
			});
			return;
		}
        return res.status(400).send("cloud already has this match");
    });
}];

table.insert.use(insertMiddleware, table.operation);
table.insert(function (context) {
    return context.execute();
});

table.delete.use(ValidateToken, AuthorizationAdmin, table.operation);
table.delete(function (context) {
    return context.execute();
});

table.update.use(ValidateToken, AuthorizationAdmin, table.operation);
table.update(function (context) {
    
    var accountID = context.user.id;
    
    return context.execute().then(function(match) {
        
        UpdateScoresOfPredictionsOfMatch(context.req.azureMobile, match);
        
        return match;
    });
});
