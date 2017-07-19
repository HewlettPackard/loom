import _ = require('lodash');
import Thread = require('weft/models/Thread');
import thread_interfaces = require('plugins/common/weft/thread_interfaces');

import IThreadSeenByQueryEditor = thread_interfaces.IThreadSeenByQueryEditor;
import IAttributeProperties = thread_interfaces.IAttributeProperties;

/**
 * This class allow to manipulate a set of thread, as if there was
 * only one thread being changed.
 *
 * In the case of the query editor, it allows the query editor to change
 * the query of multiple threads without the query editor knowing about it.
 *
 * Note: With an object of that type you can't listen for reset:elements,
 *       no such event will be generated.
 */
class ProxyThread implements IThreadSeenByQueryEditor {

  private threadList: Array<Thread>;
  private commonQuery: Query;

  constructor(threadList: Array<Thread>, commonQuery: Query) {
    this.threadList = threadList;
    this.commonQuery = commonQuery;
  }

  set(key: string, value: any): void {
    if (key === 'query') {
      this.commonQuery = value;
      _.forEach(this.threadList, (thread) => {
        thread.set('query', value);
      });
    } else {
      throw new Error("Unreachable code.");
    }
  }

  get(key: string): any {
    if (key === 'query') {
      return this.commonQuery;
    } else {
      throw new Error("Unreachable code.");
    }
  }

  getAttributesForOperation(operator: string): Array<string> {
    return this.threadList[0].getAttributesForOperation(operator);
  }

  getAttribute(attributeId: string): IAttributeProperties {
    return this.threadList[0].getAttribute(attributeId);
  }
}

export = ProxyThread;
