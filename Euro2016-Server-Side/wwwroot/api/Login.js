
var crypto = require('crypto');
var iterations = 1000;
var bytes = 32;
var aud = "Custom";
var queries = require('azure-mobile-apps/src/query');

module.exports = {
	
    "post": function (req, res, next) {
		var item = req.body;
		var azureMobile = req.azureMobile;
		
		getAccount(azureMobile, item.Email, function(isOk, account) {
			if (isOk === false) {
				return res.status(401).send("Incorrect email");
			}
			
			hash(item.Password, account.Salt, function(err, h) {
				var incoming = h;
				if (slowEquals(incoming, account.Password) === false) {
					return res.status(401).send("Incorrect password");
				}
				
				var userID = account.id;
				var expiry = new Date(new Date().setTime(new Date().getTime() + 1000 * 60 *
					60
				));// 60minute
				var mKey = process.env['MS_MasterKey'];
				var token = zumoJwt(expiry, aud, userID, mKey);
		
				saveToken(azureMobile, userID, expiry, token, function(isOk) {
					if (isOk === false) {
						return res.status(401).send("Internal authentication error");
					}
					res.status(200).send({
						UserID: userID,
						Token: token,
	                    Email: account.Email
					});
				});
			});
		});
    }
};

function getAccount(azureMobile, email, callback) {
	var accountDatas = azureMobile.tables('Account');
	var query = queries.create('Account')
					   .where({ Email : email });
	accountDatas.read(query).then(function(results) {
		if (results.length === 0) {
			return callback(false, null);
		}
		return callback(true, results[0]);
	});
}

function saveToken(azureMobile, userID, expiry, token, callback) {
	var authDatas = azureMobile.tables('AuthData');
	var query = queries.create('AuthData')
					   .where({ UserID : userID });
	authDatas.read(query).then(function(results) {
		// Insert if none, otherwise update 
		if (results.length == 0) {
			authDatas.insert({
				UserID : userID,
				ExpiryDate: expiry,
				Token: token
			}).then(function(results) {
				callback(true);
		    }).catch(function(error) {
				callback(false);
			});
			return;
		}
		var authData = results[0];
		authData.UserID = userID;
		authData.ExpiryDate = expiry;
		authData.Token = token;
		
		authDatas.update(authData).then(function(results) {
			callback(true);
	    }).catch(function(error) {
			callback(false);
		});
	});
}

function hash(text, salt, callback) {
	crypto.pbkdf2(text, salt, iterations, bytes, function(err, derivedKey) {
		if (err) { callback(err); }
		else {
			var h = new Buffer(derivedKey).toString('base64');
			callback(null, h);
		}
	});
}
 
function slowEquals(a, b) {
	var diff = a.length ^ b.length;
    for (var i = 0; i < a.length && i < b.length; i++) {
        diff |= (a[i] ^ b[i]);
	}
    return diff === 0;
}
 
function zumoJwt(expiryDate, aud, userId, masterKey) {
	var crypto = require('crypto');
 
	function base64(input) {
		return new Buffer(input, 'utf8').toString('base64');
	}
 
	function urlFriendly(b64) {
		return b64.replace(/\+/g, '-').replace(/\//g, '_').replace(new RegExp("=", "g"), '');
	}
 
	function signature(input) {
		var key = crypto.createHash('sha256').update(masterKey + "JWTSig").digest('binary');
		var str = crypto.createHmac('sha256', key).update(input).digest('base64');
		return urlFriendly(str);
	}
 
	var s1 = '{"alg":"HS256","typ":"JWT","kid":"0"}';
	var j2 = {
		"exp": expiryDate,
		"iss":"urn:microsoft:windows-azure:zumo",
		"ver":2,
		"aud":aud,
		"uid":userId ,
		"sub":userId 
	};
	var s2 = JSON.stringify(j2);
	var b1 = urlFriendly(base64(s1));
	var b2 = urlFriendly(base64(s2));
	var b3 = signature(b1 + "." + b2);
	return [b1,b2,b3].join(".");
}