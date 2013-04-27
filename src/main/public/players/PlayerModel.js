function($){
  var Player = Backbone.Model.extend({
    defaults: {
        player: {
          name: "DefaultPlayer",
          wins: 0,
          losses: 0
        }
    }
  });
}

var paragraph = $('<p></p>')

var PlayerView = Backbone.View.extend({

  tagName:  'li',

  // Cache the template function for a single item.
  playerTpl: _.template( "An example template" ),

  events: {
    'dblclick label': 'edit',
    'keypress .edit': 'updateOnEnter',
    'blur .edit':   'close'
  },

  // Re-render the titles of the todo item.
  render: function() {
    this.$el.html( this.playerTpl( this.model.toJSON() ) );
    this.input = this.$('.edit');
    return this;
  },

  edit: function() {
    // executed when todo label is double clicked
  },

  close: function() {
    // executed when todo loses focus
  },

  updateOnEnter: function( e ) {
    // executed on each keypress when in todo edit mode,
    // but we'll wait for enter to get in action
  }
});

var playerView = new PlayerView();

playerView.setElement(paragraph)