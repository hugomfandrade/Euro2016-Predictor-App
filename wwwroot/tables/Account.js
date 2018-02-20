var ValidateToken = require('../authentication_scripts/ValidateToken');
var Q = require("q");

var table = module.exports = require('azure-mobile-apps').table();

table.read(function (context) {
    return context.execute().then(function(accounts) {
        
        var azureMobile = context.req.azureMobile;
        
        return getScoresOfUsers(context, accounts).then(function(accounts) {
            
            return accounts;
            
        }).catch(function(error) {
            return error;
    	});
    }).catch(function(error) {
        return error;
	});
});

table.read.use(ValidateToken, table.operation);

function getScoresOfUsers(context, accounts) {
    var defer = Q.defer();
    
    query = 'SELECT p.UserID, SUM(ps.Score) AS Score'
         + ' FROM Account a'
         + ' INNER JOIN Prediction p ON a.id = p.UserID'
         + ' INNER JOIN PredictionScore ps ON p.id = ps.PredictionID'
         + ' WHERE ' + buildWhereClause(accounts)
         + ' GROUP BY p.UserID'
    
    context.data.execute({sql: query})
        .then(function (results) {
            
            accounts.forEach(function(account) {
                account.Score = 0;
                
                results.forEach(function(result) {
                    if (account.id === result.UserID)
                        account.Score = result.Score;
                });
                delete account.Password;
                delete account.Salt;
            });
            defer.resolve(accounts);
        }).catch(function(error) {
            defer.reject(error);
    	});
    
	return defer.promise;
}

function buildWhereClause(accounts) {
    
    var whereClause = '';
    accounts.forEach(function(account) {
        if (whereClause !== '')
            whereClause = whereClause + ' OR ';
        whereClause = whereClause + 'p.UserID = \'' + account.id + '\''
    });
    return whereClause
}
