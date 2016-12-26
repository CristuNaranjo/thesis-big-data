/**
 * Main application file
 */

 // Inicialización
 var express         = require('express');
 var serveStatic     = require('serve-static');
 var morgan          = require('morgan');
 var fs              = require('fs');
 var bodyParser      = require('body-parser');
 var methodOverride  = require('method-override');
 var mongoose        = require('mongoose'); 				// mongoose para mongodb
 var config          = require('./config/database'); // get db config file
 var Game            = require('./model/game'); // get the mongoose model
 var passport	       = require('passport');
 var port  	         = process.env.PORT || 3001; 			// Cogemos el puerto 3001

 var app             = express(); 					// Utilizamos express
 var accessLogStream = fs.createWriteStream(__dirname + '/log/access.log', {flags: 'a'});

 // Configuracion
 mongoose.connect(config.database);

 var db = mongoose.connection;
 db.on('error', console.error.bind(console, 'connection error:'));


 app.use(serveStatic(__dirname + '/public'));
 app.use(serveStatic(__dirname + '/node_modules'));
 app.use(morgan('combined', {stream: accessLogStream}));
 app.use(bodyParser.urlencoded({extended:false}));
 app.use(bodyParser.json());
 app.use(methodOverride('X-HTTP-Method'));
 app.use(methodOverride('X-HTTP-Method-Override'));
 app.use(methodOverride('X-Method-Override'));


 // Cargamos los endpoints
 require('./routes.js')(app);

 // Cogemos el puerto para escuchar
 app.listen(port);
 console.log("APP por el puerto " + port);
