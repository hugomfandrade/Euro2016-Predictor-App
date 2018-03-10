
module.exports = function (field, arr) {
    
	if (arr.length !== 0) {
        var s = '';
        var it = 0;
        arr.forEach(function(id) {
            if (it !== 0)
                s = s + ' or ';
            s = s + field + ' eq \'' + id + '\'';
            it = it + 1;
        });
        return s;
    }
    return null;
};