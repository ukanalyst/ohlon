var mongoose = require('mongoose');

var Schema = mongoose.Schema,
    ObjectId = Schema.ObjectId;

module.exports = mongoose.model('Server',{
    id: String,
    name: String,
    MAC: String,
    expirationDate: Date,
    lastSync: Date,
    created: {type: Date, default: Date.now},
	account: { type: ObjectId, ref: 'Account' }
});
