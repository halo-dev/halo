const ShortUniqueId = require('short-unique-id');

const uid = new ShortUniqueId();

console.log(uid(6));
console.log(uid.seq());

const uidWithTimestamp = uid.stamp(32);
console.log(uidWithTimestamp);
console.log(uid.parseStamp(uidWithTimestamp));

console.log(uid.version);
