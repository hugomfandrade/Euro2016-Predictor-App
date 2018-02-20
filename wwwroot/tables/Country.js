var ValidateToken = require('../authentication_scripts/ValidateToken');
var AuthorizationAdmin = require('../authentication_scripts/AuthorizationAdmin');
var queries = require('azure-mobile-apps/src/query');

var table = module.exports = require('azure-mobile-apps').table();

var insertMiddleware = [ValidateToken, AuthorizationAdmin, function(req, res, next) {
    
	var azureMobile = req.azureMobile;
    var country = req.body;
    var name = country.Name;  
    delete country.id;
    
    var countryData = azureMobile.tables('Country');
	var query = queries.create('Country').where({ Name : name });
   
	countryData.read(query).then(function(results) {
        if (results.length === 0) {
			countryData.insert(country).then(function(results) {
				return res.status(200).send(results);
		    }).catch(function(error) {
                return res.status(400).send(error);
			});
			return;
		}
        return res.status(400).send("cloud already has this country");
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
   return context.execute();
});

table.read.use(ValidateToken, table.operation);
table.read(function (context) {
   return context.execute();
});

