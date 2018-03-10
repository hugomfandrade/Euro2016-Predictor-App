var ValidateToken = require('../authentication_scripts/ValidateToken');
var AuthorizationAdmin = require('../authentication_scripts/AuthorizationAdmin');
var UpdateScoresOfPredictionsOfMatch = require('../processing_scripts/UpdateScoresOfPredictionsOfMatch');
var queries = require('azure-mobile-apps/src/query');

var table = module.exports = require('azure-mobile-apps').table();

table.insert.use(ValidateToken, AuthorizationAdmin, table.operation);
table.insert(function (context) {
    
    var match = context.req.body;
    var matchNumber = match.MatchNumber;
    
    var matchData = context.req.azureMobile.tables('Match');
	var q = queries.create('Match').where({ MatchNumber : matchNumber });
	return matchData.read(q).then(function(results) {
        if (results.length === 0) {
            delete match.id;
			return matchData.insert(match);
		}
        
        throw Error("cloud already has this match");
    });
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
