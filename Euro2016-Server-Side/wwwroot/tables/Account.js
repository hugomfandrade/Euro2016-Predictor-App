
var ValidateToken = require('../authentication_scripts/ValidateToken');
var BuildQuery = require("../utils_scripts/BuildQuery");

var table = module.exports = require('azure-mobile-apps').table();

table.read.use(ValidateToken, table.operation);
table.read(function (context) {
    
    var query = buildReadQuery(context.req.query);
    
    return context.data.execute({sql: query});
});

function buildReadQuery(query) {
    
	var fromQuery =
           'SELECT a.id, a.Email, SUM(ps.Score) AS Score'
         + ' FROM Account a'
         + ' LEFT JOIN Prediction p ON a.id = p.UserID'
         + ' LEFT JOIN PredictionScore ps ON p.id = ps.PredictionID'
         + ' GROUP BY a.id, a.Email'
      
	return BuildQuery(query, fromQuery, null);
}
