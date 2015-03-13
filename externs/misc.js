var source_map_support = {};
source_map_support.install = function() {};

function setImmediate() {};
/** @type {function(new:LevelUp, ...[*])} */                                           
levelup = function() {};      
/** @typedef {function(new:LevelUp, ...)} */
var LevelUp;  
LevelUp.prototype.close = function() {};
LevelUp.prototype.put = function() {};
LevelUp.prototype.get = function() {};
LevelUp.prototype.del = function() {};
LevelUp.prototype.batch = function() {};
/** @return {stream.ReadableStream} */
LevelUp.prototype.createReadStream = function() {};
/** @return {stream.ReadableStream} */
LevelUp.prototype.createKeyStream = function() {};
/** @return {stream.ReadableStream} */
LevelUp.prototype.createValueStream = function() {};
/** @return {stream.WritableStream} */
LevelUp.prototype.createWriteStream = function() {};

ws = function() {};      
/** @type {function(new:WSServer, ...[*])} */                                           
ws.Server = function() {};      
var WSServer;
WSServer.prototype.on = function() {};



Showdown = {};
/** @typedef {function(new:Showdown.converter, ...[*])} */                                           
Showdown.converter = function() {};
Showdown.converter.prototype.makeHtml = function() {};
