import QueryEditor = require('weaver/views/QueryEditor');

var originalGenerateQueryFromPipeline = QueryEditor.prototype.generateQueryFromPipeline;
var originalInitialize = QueryEditor.prototype.initialize;

QueryEditor.prototype.generateQueryFromPipeline = function () {

  var query = originalGenerateQueryFromPipeline.apply(this, arguments);

  query = this.queryValidator.validateQuery(query);

  return query;
};

QueryEditor.prototype.initialize = function (options) {

  originalInitialize.apply(this, arguments);

  this.queryValidator = options.queryValidator;
};

var decorate = () => {};
export = decorate;
