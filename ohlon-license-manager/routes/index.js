var express = require('express');
var Account = require('../models/account');
var Server = require('../models/server');
var router = express.Router();
var moment = require('moment');
var _ = require('lodash');

var isAuthenticated = function (req, res, next) {
	// if user is authenticated in the session, call the next() to call the next request handler 
	// Passport adds this method to request object. A middleware is allowed to add properties to
	// request and response objects
	if (req.isAuthenticated())
		return next();
	// if the user is not authenticated then redirect him to the login page
	res.redirect('/');
}

module.exports = function(passport){

	/* GET login page. */
	router.get('/', function(req, res) {
    	// Display the Login page with any flash message, if any
		res.render('index', { message: req.flash('message') });
	});

	/* Handle Login POST */
	router.post('/login', passport.authenticate('login', {
		successRedirect: '/home',
		failureRedirect: '/',
		failureFlash : true  
	}));

	/* Handle Registration POST */
	router.post('/signup', passport.authenticate('signup', {
		successRedirect: '/home',
		failureRedirect: '/signup',
		failureFlash : true  
	}));

	/* GET Home Page */
	router.get('/home', isAuthenticated, function(req, res){
		Account.find({}, function(err, accounts) {
			if (err)
				throw err;
			res.render('home', { user: req.user, accounts: accounts });
		});
	});

	/* GET all accounts */
	router.get('/account/all', isAuthenticated, function(req, res){
		Account.find({ }).sort('name').exec(function(err, accounts) {
			if (err)
				throw err;
			
			var data = [];
			_.each(accounts, function(account) {
				data.push({
					value: account._id,
					text: account.name
				});
			});
			
			res.json(data);
		});
	});

	/* GET account servers */
	router.get('/server/list/:accountId', isAuthenticated, function(req, res){
		Server.find({ account : req.params.accountId })
			.populate('account')
			.exec(function(err, servers) {
				if (err)
					throw err;
			
				res.render('serverList', { servers: servers });
			});
	});

	/* GET orphan servers */
	router.get('/server/orphan', isAuthenticated, function(req, res){
		Server.find({ account: { $exists: false } })
			.populate('account')
			.exec(function(err, servers) {
				if (err)
					throw err;
			
				res.render('serverList', { servers: servers });
			});
	});
	
	/* POST update server property */
	router.post('/server/set', isAuthenticated, function(req, res){
		Server.findOne({ _id: req.body.pk }, function(err, server) {
			if (err)
				throw err;
			server[req.body.name] = req.body.value;
			server.save(function (err) {
				if (err)
					throw err;
				res.json(server);
			});
		});
	});
	
	/* POST check license */
	router.post('/license/check', function(req, res){
		console.log(req.body);
		Server.findOne({
			MAC: req.body.mac
		}, function(err, server) {
			if (err)
				throw err;
			if (server == null) {
				// Create the server
				var newServer = new Server({
				    MAC: req.body.mac,
				    expirationDate: moment().add(15, 'days').toDate(),
				    lastSync: new Date()
				});
				
				newServer.save(function (err) {
					if (err)
						throw err;
					res.json({
						valid: true,
						message: "The license will expire on " + newServer.expirationDate
					});
				});
				
			} else {
				// Check if the license expired
				if (moment(server.expirationDate).isBefore(moment())) {
					res.json({
						valid: false,
						message: "The license is expired."
					});
				} else {
					server.lastSync = new Date();
					
					server.save(function (err) {
						if (err)
							throw err;
						res.json({
							valid: true,
							message: "The license will expire on " + server.expirationDate 
						});
					});
				}

			}
		});
	});

	/* Handle Logout */
	router.get('/signout', function(req, res) {
		req.logout();
		res.redirect('/');
	});

	return router;
}





