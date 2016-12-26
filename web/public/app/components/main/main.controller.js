angular.module('mainApp').controller('mainController',
['$scope','$http', '$state','API_ENDPOINT','$q',function mainController($scope, $http, $state, API_ENDPOINT,$q) {

$scope.equipos = [
	{id: 1, abbr: "BOS", team: "Boston Celtics"},
	{id: 2, abbr: "NJN", team: "Brooklyn (New Jersey) Nets"},
	{id: 3, abbr: "NYK", team: "New York Knicks"},
	{id: 4, abbr: "PHI", team: "Philadelphia 76ers"},
	{id: 5, abbr: "TOR", team: "Toronto Raptors "},
	{id: 6, abbr: "CHI", team: "Chicago Bulls"},
	{id: 7, abbr: "CLE", team: "Cleveland Cavaliers"},
	{id: 8, abbr: "DET", team: "Detroit Pistons"},
	{id: 9, abbr: "IND", team: "Indiana Pacers "},
	{id: 10, abbr: "MIL", team: "Milwaukee Bucks"},
	{id: 11, abbr: "ATL", team: "Atlanta Hawks"},
	{id: 12, abbr: "CHA", team: "Charlotte Hornets"},
	{id: 13, abbr: "MIA", team: "Miami Heat"},
	{id: 14, abbr: "ORL", team: "Orlando Magic"},
	{id: 15, abbr: "DEN", team: "Denver Nuggets"},
	{id: 16, abbr: "WAS", team: "Washington Wizards"},
	{id: 17, abbr: "MIN", team: "Minnesota Timberwolves"},
	{id: 18, abbr: "POR", team: "Portland Trail Blazers"},
	{id: 19, abbr: "OKC", team: "Oklahoma City Thunder"},
	{id: 20, abbr: "UTA", team: "Utah Jazz"},
	{id: 21, abbr: "GSW", team: "Golden State Warriors"},
	{id: 22, abbr: "LAC", team: "Los Angeles Clippers"},
	{id: 23, abbr: "LAL", team: "Los Angeles Lakers"},
	{id: 24, abbr: "PHO", team: "Phoenix Suns"},
	{id: 25, abbr: "SAC", team: "Sacramento Kings"},
	{id: 26, abbr: "DAL", team: "Dallas Mavericks"},
	{id: 27, abbr: "HOU", team: "Houston Rockets"},
	{id: 28, abbr: "MEM", team: "Memphis Grizzlies"},
	{id: 29, abbr: "NOH", team: "New Orleans Pelicans"},
	{id: 30, abbr: "SAS", team: "San Antonio Spurs"}
];
$scope.temporadas = [
	{name: "Temporada 12/13", value: "2012-2013"},
	{name: "Temporada 11/12", value: "2011-2012"},
	{name: "Temporada 10/11", value: "2010-2011"},
	{name: "Temporada 09/10", value: "2009-2010"},
	{name: "Temporada 08/09", value: "2008-2009"},
	{name: "Temporada 07/08", value: "2007-2008"},
	{name: "Temporada 06/07", value: "2006-2007"},
	{name: "Temporada 05/06", value: "2005-2006"},
	{name: "Temporada 04/05", value: "2004-2005"},
	{name: "Temporada 03/04", value: "2003-2004"},
	{name: "Temporada 02/03", value: "2002-2003"},
	{name: "Temporada 01/02", value: "2001-2002"},
	{name: "Temporada 00/01", value: "2000-2001"},
	{name: "Temporada 99/00", value: "1999-2000"},
	{name: "Temporada 98/99", value: "1998-1999"},
	{name: "Temporada 97/98", value: "1997-1998"},
	{name: "Temporada 96/97", value: "1996-1997"},
	{name: "Temporada 95/96", value: "1995-1996"},
	{name: "Temporada 94/95", value: "1994-1995"},
	{name: "Temporada 93/94", value: "1993-1994"},
	{name: "Temporada 92/93", value: "1992-1993"},
	{name: "Temporada 91/92", value: "1991-1992"},
	{name: "Temporada 90/91", value: "1990-1991"},
	{name: "Temporada 89/90", value: "1989-1990"},
	{name: "Temporada 88/89", value: "1988-1989"},
	{name: "Temporada 87/88", value: "1987-1988"},
	{name: "Temporada 86/87", value: "1986-1987"},
	{name: "Temporada 85/86", value: "1985-1986"},
	{name: "Temporada 84/85", value: "1984-1985"}
]
$scope.selectedTeamL = "BOS";
$scope.selectedTeamV = "NYK";
$scope.localR = "Locales";
$scope.visitR = "Visitantes";
$scope.dateModel ="2012-2013";
$scope.buttonPred = true;
$scope.fullGames = [];
$scope.selectedGame ={};
$scope.hideGame=true;
$scope.locales = [];
$scope.visitantes = [];
$scope.resL="";
$scope.resV="";
$scope.predLocal = "";
$scope.predVisit = "";
$scope.predL="";
$scope.predV="";
$scope.isLoading=false;
var localPlayers = new Array();
var visitPlayers = new Array();
var localTeam = "";
var visitTeam = "";
var locales = [];
var visitantes = [];


$scope.onSearchClick = function(){
	$scope.locales = [];
	$scope.visitantes = [];
	$scope.predL="";
	$scope.predV="";
	$scope.hideGame=false;
	$scope.fullGames=[];
	var date = $scope.dateModel.split("-");
	var initDate = new Date(date[0], 9, 1);
	var finDate = new Date(date[1], 3, 30);
	var post = {
		local: $scope.selectedTeamL,
		visit: $scope.selectedTeamV,
		inDate: initDate,
		fiDate: finDate
	}
	console.log("IU - Mandando datos de búsqueda a la API");
	return $q(function(resolve,reject){
	$http.post(API_ENDPOINT.url + '/search', post).then(function(result) {
			if (result.data.success) {
				//console.log(result.data.games);
				$scope.fullGames=result.data.games;
				resolve(result.data.msg);
			} else {
				console.log("IU - Fail search");
				reject(result.data.msg);
			}
		});
	});
};
$scope.onChange = function(){
	$scope.buttonPred = false;
	localPlayers = [];
	visitPlayers = [];
	localTeam = "";
	visitTeam = "";
	locales = [];
	visitantes = [];
	$scope.predLocal = [];
	$scope.predVisit = [];
	$scope.predL="";
	$scope.predV="";
	var game = $scope.selectedGame;
	if(game.teams[0].home == false){
		localTeam=game.teams[1].abbreviation;
		visitTeam=game.teams[0].abbreviation;
		$scope.resL = game.teams[1].score;
		$scope.resV = game.teams[0].score;
		for(var i=0; i<game.box[0].players.length; i++){
			visitPlayers.push(game.box[0].players[i].player);
			visitantes.push({player: game.box[0].players[i].player, puntos:game.box[0].players[i].pts})
		}
		for(var i=0; i<game.box[1].players.length; i++){
			localPlayers.push(game.box[1].players[i].player);
			locales.push({player: game.box[1].players[i].player, puntos:game.box[1].players[i].pts})
		}
	}else{
		localTeam=game.teams[0].abbreviation;
		visitTeam=game.teams[1].abbreviation;
		$scope.resL = game.teams[0].score;
		$scope.resV = game.teams[1].score;
		for(var i=0; i<game.box[0].players.length; i++){
			localPlayers.push(game.box[0].players[i].player);
			locales.push({player: game.box[0].players[i].player, puntos:game.box[0].players[i].pts});
		}
		for(var i=0; i<game.box[1].players.length; i++){
			visitPlayers.push(game.box[1].players[i].player);
			visitantes.push({player: game.box[1].players[i].player, puntos:game.box[1].players[i].pts});
		}
	}
	console.log("IU - Cargando datos de partido real");
	$scope.locales = locales.sortBy('player');
	$scope.visitantes = visitantes.sortBy('player');
	$scope.localR = localTeam;
	$scope.visitR = visitTeam;
};
$scope.onPredict = function(){
	//console.log("onPredict");
	//console.log($scope.selectedGame);
	console.log("IU - Mandando datos de predicción a la API");
	$scope.isLoading = true;
	var game = $scope.selectedGame;
	var fecha = new Date(game.date);
	var fechaPost = fecha.getFullYear() + "-" + fecha.getMonth() + "-" + fecha.getDate()
	var post = {
		localP : localPlayers,
		visitP : visitPlayers,
		localA : localTeam,
		visitA : visitTeam,
		fecha  : fechaPost
	}
	return $q(function(resolve,reject){
		var config = {
			timeout: 12000
		}
	$http.post(API_ENDPOINT.url + '/predict', post,config).then(function(result) {
			if (result.data.success) {
				//console.log(result.data.msg);
				//console.log(result.data.local);
				//console.log(result.data.visit);
				$scope.isLoading = false;
				$scope.predLocal = result.data.local.sortBy('player');
				$scope.predVisit = result.data.visit.sortBy('player');
				$scope.predL=result.data.local.sum('pts');
				$scope.predV=result.data.visit.sum('pts');
				console.log("IU - Predicción recibida, cargando datos");
			} else {
				console.log("IU - Predict fail");
				reject(result.data.msg);
			}
		});
	});
}
Array.prototype.sortBy = function(p) {
  return this.slice(0).sort(function(a,b) {
    return (a[p] > b[p]) ? 1 : (a[p] < b[p]) ? -1 : 0;
  });
}
Array.prototype.sum = function (prop) {
    var total = 0
    for ( var i = 0, _len = this.length; i < _len; i++ ) {
        total += this[i][prop]
    }
    return total
}
}]);
