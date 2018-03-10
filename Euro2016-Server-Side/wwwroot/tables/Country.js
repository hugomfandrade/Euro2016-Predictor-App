var ValidateToken = require('../authentication_scripts/ValidateToken');
var AuthorizationAdmin = require('../authentication_scripts/AuthorizationAdmin');
var queries = require('azure-mobile-apps/src/query');

var table = module.exports = require('azure-mobile-apps').table();

table.insert.use(ValidateToken, AuthorizationAdmin, table.operation);
table.insert(function (context) {
    
    var country = context.req.body;
    var countryName = country.Name;
    
    var countryData = context.req.azureMobile.tables('Country');
	var q = queries.create('Country').where({ Name : countryName });
	return countryData.read(q).then(function(results) {
        if (results.length === 0) {
            delete country.id;
			return countryData.insert(country);
		}
        
        throw Error("cloud already has this country");
    });
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

