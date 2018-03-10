
var bytes = 32;
var iterations = 1000;
var crypto = require('crypto');
var queries = require('azure-mobile-apps/src/query');

module.exports = {
    "post": function (req, res, next) {
		var item = req.body;
		var azureMobile = req.azureMobile;
		
		doesEmailExist(azureMobile, item, function(itExists) {
			if (itExists === true) {
				return res.status(400).send("Email already exists");
			}
			
			// Add your own validation - what fields do you require to 
			// add a unique salt to the item
			item.Salt = new Buffer(crypto.randomBytes(bytes)).toString('base64');
			// hash the password
			hash(item.Password, item.Salt, function(err, h) {
				item.Password = h;
				
				var accounts = azureMobile.tables('Account');
				accounts.insert(item).then(function(results) {
                    return res.status(200).send({
						UserID: results.id,
						Email: results.Email
					});
			    }).catch(function(error) {
					return res.status(401)
					   		  .send("Error registering: " + error);
				});
			});
		});
    }
};

function doesEmailExist(azureMobile, item, callback) {
	var accounts = azureMobile.tables('Account');
	var query = queries.create('Account')
					   .where({ Email : item.Email });
	accounts.read(query).then(function(results) {
		return callback(results.length > 0);
	});
};

function hash(text, salt, callback) {
	crypto.pbkdf2(text, salt, iterations, bytes, function(err, derivedKey) {
		if (err) { callback(err); }
		else {
			var h = new Buffer(derivedKey).toString('base64');
			callback(null, h);
		}
	});
}
