var Controller      = require ('./controller');
var express         = require('express');
var passport	      = require('passport');

module.exports = function(app) {

	var apiRoutes = express.Router();
	apiRoutes.post('/search', Controller.search);
	apiRoutes.post('/predict', Controller.predict);
	app.use('/api', apiRoutes);

}
