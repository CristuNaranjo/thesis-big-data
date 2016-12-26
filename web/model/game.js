var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var GameSchema = new Schema({
  box:[{
    players:[{
      ast: {
        type: Number
      },
      blk:{
        type: Number
      },
      drb: {
        type: Number
      },
      fg: {
        type: Number
      },
      fg3: {
        type: Number
      },
      fg3_pct:{
        type: String
      },
      fg3a: {
        type: Number
      },
      fg_pct: {
        type: String
      },
      fga: {
        type: Number
      },
      ft: {
        type: Number
      },
      ft_pct: {
        type: String
      },
      fta: {
        type: Number
      },
      mp: {
        type: String
      },
      orb:{
        type: Number
      },
      pf:{
        type: Number
      },
      player:{
        type: String
      },
      pts:{
        type: Number
      },
      stl: {
        type: Number
      },
      tov: {
        type: Number
      },
      trb:{
        type: Number
      }
    }],
    team: {
      ast: {
        type: Number
      },
      blk:{
        type: Number
      },
      drb: {
        type: Number
      },
      fg: {
        type: Number
      },
      fg3: {
        type: Number
      },
      fg3_pct:{
        type: String
      },
      fg3a: {
        type: Number
      },
      fg_pct: {
        type: String
      },
      fga: {
        type: Number
      },
      ft: {
        type: Number
      },
      ft_pct: {
        type: String
      },
      fta: {
        type: Number
      },
      mp: {
        type: String
      },
      orb:{
        type: Number
      },
      pf:{
        type: Number
      },
      pts:{
        type: Number
      },
      stl: {
        type: Number
      },
      tov: {
        type: Number
      },
      trb:{
        type: Number
      }
    },
    won: {
      type: Number
    }
  }],
  date: {
    type: Date
  },
  teams:[{
    name:{
      type: String
    },
    abbreviation:{
      type: String
    },
    score:{
      type: Number
    },
    home:{
      type: Boolean
    },
    won: {
      type: Number
    }
  }]
});
module.exports = mongoose.model('Game', GameSchema);
