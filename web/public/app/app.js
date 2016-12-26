var mainApp = angular.module('mainApp', [
    'ui.router',
    'ngMaterial'
]);

mainApp.config(function($stateProvider, $urlRouterProvider){

 $urlRouterProvider.otherwise("/main");

 $stateProvider
	 .state('main', {
		 url: "/main",
		 templateUrl: "/app/components/main/main.view.html"
	 })
});
mainApp.constant('AUTH_EVENTS', {
  notAuthenticated: 'auth-not-authenticated'
});
mainApp.constant('API_ENDPOINT', {
  url: 'http://localhost:3001/api'
});
mainApp.run(function ($rootScope, $state) {
});
