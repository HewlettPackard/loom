
import Thread = require('weft/models/Thread');
import ProxyThread = require('./ProxyThread');

class TreeThread {

  rootThread: Thread;
  virtualChildThread: ProxyThread;

  constructor(rootThread: Thread) {
    this.rootThread = rootThread;
  }
}
