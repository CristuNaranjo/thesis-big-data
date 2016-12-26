// var User1      = require('./model/user1');
// var User       = require ('./model/user');
var Game       = require('./model/game')
var jwt        = require('jwt-simple');
var config     = require('./config/database');
var passport   = require('passport');
var fs         = require('fs');



exports.search = function(req, res){
  var local = req.body.local;
  var visit = req.body.visit;
  var initDate = req.body.inDate;
  var finDate = req.body.fiDate;
  var query = "{teams: {$all: [ {$elemMatch: {abbreviation:'"+local+"',home:true}}, {$elemMatch: {abbreviation:'"+visit+"',home:false}}]}, date: {$gte: "+initDate+",$lt:"+finDate+"}}";
  console.log("API - Buscando partidos...")
  Game.find({teams: {$all: [ {$elemMatch: {abbreviation: local, home:true}}, {$elemMatch:{abbreviation: visit,home:false}}]},date:{$gte:initDate,$lt:finDate}},null,{limit:20},function(err,game){
    if(err) throw err;
    if(!game){
      res.send({success: false, msg: 'No games found.'})
    }else{
      console.log("API - Partidos encontrados...")
      res.json({success: true, games: game, msg:'Sent games'});
    }
  });
}
exports.predict = function(req,res){
  var localPlayers = req.body.localP;
  var visitPlayers = req.body.visitP;
  var localTeam = req.body.localA;
  var visitTeam = req.body.visitA;
  var fecha = req.body.fecha;
  var cont = 0;
  function toInt(n){ return Math.round(Number(n)); };
  var localParsed = localPlayers.toString().replace("'","\\'");
  var visitParsed = visitPlayers.toString().replace("'","\\'");
  console.log("API - Leyendo datos para realizar la predicción...")
  var stream = fs.createWriteStream("predict.txt");
  stream.once('open', function(fd) {
    stream.write(localTeam + "\n" + visitTeam + "\n" + localParsed + "\n" + visitParsed + "\n" + fecha);
    stream.end();
  });
  var child = require('child_process').spawn(
    'java', ['-jar', 'core.jar']
  );
  console.log("API - Loading core.jar");
  child.stdout.on('data', function(data) {
    console.log(data.toString());
  });
  child.stderr.on('data', function (data) {
      console.log(data.toString());
  });
  var watcher = fs.watch("predictions.txt",function(eventType, filename){
    cont++;
    if(cont > 1){
      console.log("API - Leyendo fichero con predicciones");
      fs.readFile(filename, function(err,data){
        var lines = String(data).split("\n");
        var localPredictions = [];
        var visitPredictions = [];
        for(var i=0; i<localPlayers.length;i++){
          var data = lines[i].split("/");
          var player = data[0].toString().replace("\\'","'");
          var object = {
            player: player,
            pts: toInt(data[1])
          }
          localPredictions.push(object);
        }
        for(var x=localPlayers.length; x<visitPlayers.length + localPlayers.length;x++){
          var data = lines[x].split("/");
          var player = data[0].toString().replace("\\'","'");
          var object = {
            player: player,
            pts: toInt(data[1])
          }
          visitPredictions.push(object);
        }
        console.log("API - Mandando datos...")
        res.json({success: true, msg:'Okey', local:localPredictions, visit:visitPredictions});
      });
      watcher.close();
    }
  });
}
