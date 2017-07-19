
import BraidingController = require('weft/models/Tapestry/BraidingController');

var ref = { value: 45 };

BraidingController.prototype.updateThreadBraiding = function (thread) {
  thread.trigger('optimalBraiding', this.get('braiding'));
};

BraidingController.prototype.initialize = function () {
  this.listenTo(this, 'change:braiding', () => {
    ref.value = this.get('braiding');
  });
  this.listenTo(this, 'change:braiding', this.updateAllThreadsBraiding);
  this.listenTo(this.get('threads'), 'add', this.updateThreadBraiding);
};

var optimalBraiding = {
  getValue: function() {
    return ref.value;
  }
};
export = optimalBraiding;
