
var mongoose = require('mongoose');

module.exports = mongoose.model('Account',{
	id: String,
	name: String,
	address: String,
	zip: String,
	city: String,
	country: String,
	email: String
});
