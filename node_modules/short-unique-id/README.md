# Short Unique ID (UUID) Generating Library
![Tests](https://github.com/jeanlescure/short-unique-id/workflows/tests/badge.svg)
[![Try short-unique-id on RunKit](https://badge.runkitcdn.com/short-unique-id.svg)](https://npm.runkit.com/short-unique-id)
[![NPM Downloads](https://img.shields.io/npm/dt/short-unique-id.svg?maxAge=2592000)](https://npmjs.com/package/short-unique-id)
[![JsDelivr Hits](https://data.jsdelivr.com/v1/package/npm/short-unique-id/badge?style=rounded)](https://www.jsdelivr.com/package/npm/short-unique-id)

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![9 Contributors](https://img.shields.io/badge/all_contributors-8-purple.svg)](#contributors)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

---

Tiny (4.6kB minified) no-dependency library for generating random or sequential UUID of any length
with exceptionally minuscule probabilies of duplicate IDs.

```ts
const uid = new ShortUniqueId({ length: 10 });
uid(); // p0ZoB1FwH6
uid(); // mSjGCTfn8w
uid(); // yt4Xx5nHMB
// ...
```

For example, using the default dictionary of numbers and letters (lower and upper case):

```ts
  0,1,2,3,4,5,6,7,8,9,
  a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,
  A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z
```

- if you generate a unique ID of 16 characters (half of the standard UUID of 32 characters)
- generating 100 unique IDs **per second**

#### It would take **~10 thousand years** to have a 1% probability of at least one collision!

To put this into perspective:

- 73 years is the (global) average life expectancy of a human being
- 120 years ago no human ever had set foot on either of the Earth's poles
- 480 years ago Nicolaus Copernicus was still working on his theory of the Earth revolving around the Sun
- 1000 years ago there was no such thing as government-issued paper money (and wouldn't be for about a century)
- 5000 years ago the global population of humans was under 50 million (right now Mexico has a population of 127 million)

You can calculate duplicate/collision probabilities using the included functions:

- [availableUUIDs()](https://shortunique.id/classes/default.html#availableuuids)
- [approxMaxBeforeCollision()](https://shortunique.id/classes/default.html#approxmaxbeforecollision)
- [collisionProbability()](https://shortunique.id/classes/default.html#collisionprobability)

_NOTE: 👆 On these links you will also find explanations for the math used within the functions._

---

## Open source notice

This project is open to updates by its users, I ensure that PRs are relevant to the community.
In other words, if you find a bug or want a new feature, please help us by becoming one of the
[contributors](#contributors-) ✌️ ! See the [contributing section](#contributing).

## Like this module? ❤

Please consider:

- [Buying me a coffee](https://www.buymeacoffee.com/jeanlescure) ☕
- Supporting me on [Patreon](https://www.patreon.com/jeanlescure) 🏆
- Starring this repo on [Github](https://github.com/jeanlescure/short-unique-id) 🌟

## 📣 v4 Notice

### New Features 🥳

The ability to generate UUIDs that contain a timestamp which can be extracted:

```js
// js/ts

const uid = new ShortUniqueId();

const uidWithTimestamp = uid.stamp(32);
console.log(uidWithTimestamp);
// GDa608f973aRCHLXQYPTbKDbjDeVsSb3

const recoveredTimestamp = uid.parseStamp(uidWithTimestamp);
console.log(recoveredTimestamp);
// 2021-05-03T06:24:58.000Z
```

```bash
# cli

$ suid -s -l 42

  lW611f30a2ky4276g3l8N7nBHI5AQ5rCiwYzU47HP2

$ suid -p lW611f30a2ky4276g3l8N7nBHI5AQ5rCiwYzU47HP2

  2021-08-20T04:33:38.000Z
```

Default dictionaries (generated on the spot to reduce memory footprint and
avoid dictionary injection vulnerabilities):

- number
- alpha
- alpha_lower
- alpha_upper
- **alphanum** _(default when no dictionary is provided to `new ShortUniqueId()`)_
- alphanum_lower
- alphanum_upper
- hex

```js
// instantiate using one of the default dictionary strings
const uid = new ShortUniqueId({
  dictionary: 'hex', // the default
});

console.log(uid.dict.join());
// 0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f

// or change the dictionary after instantiation
uid.setDictionary('alpha_upper');

console.log(uid.dict.join());
// A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z
```

### Use in CLI

```sh
$ npm install -g short-unique-id

$ suid -h

# Usage:
#   node short-unique-id [OPTION]

# Options:
#   -l, --length=ARG         character length of the uid to generate.
#   -s, --stamp              include timestamp in uid (must be used with --length (-l) of 10 or more).
#   -p, --parse=ARG          extract timestamp from stamped uid (ARG).
#   -d, --dictionaryJson=ARG json file with dictionary array.
#   -h, --help               display this help
```

### Use as module

Add to your project:

```js
// Deno (web module) Import
import ShortUniqueId from 'https://esm.sh/short-unique-id';

// ES6 / TypeScript Import
import ShortUniqueId from 'short-unique-id';

// Node.js require
const ShortUniqueId = require('short-unique-id');
```

Instantiate and use:

```js
//Instantiate
const uid = new ShortUniqueId();

// Random UUID
console.log(uid());

// Sequential UUID
console.log(uid.seq());
```

### Use in browser

```html
<!-- Add source (minified 4.6kB) -->
<script src="https://cdn.jsdelivr.net/npm/short-unique-id@latest/dist/short-unique-id.min.js"></script>

<!-- Usage -->
<script>
  // Instantiate
  var uid = new ShortUniqueId();

  // Random UUID
  document.write(uid());

  // Sequential UUID
  document.write(uid.seq());
</script>
```

### Options

Options can be passed when instantiating `uid`:

```js
const options = { ... };

const uid = new ShortUniqueId(options);
```

For more information take a look at the [docs](https://shortunique.id/interfaces/shortuniqueidoptions.html).

## Available for

- [Node.js (npm)](https://www.npmjs.com/package/short-unique-id)
- [Deno](https://esm.sh/short-unique-id)
- [Browsers](https://www.jsdelivr.com/package/npm/short-unique-id?path=dist)

## Documentation with Online Short UUID Generator

You can find the docs and online generator at:

<a target="_blank" href="https://shortunique.id">https://shortunique.id</a>

## What is the probability of generating the same id again?

This largely depends on the given dictionary and the selected UUID length.

Out of the box this library provides a shuffled dictionary of digits from
0 to 9, as well as the alphabet from a to z both in UPPER and lower case,
with a default UUID length of 6. That gives you a total of 56,800,235,584
possible UUIDs.

So, given the previous values, the probability of generating a duplicate
in 1,000,000 rounds is ~0.00000002, or about 1 in 50,000,000.

If you change the dictionary and/or the UUID length then we have provided
the function `collisionProbability()` function to calculate the probability
of hitting a duplicate in a given number of rounds (a collision) and the
function `uniqueness()` which provides a score (from 0 to 1) to rate the 
"quality" of the combination of given dictionary and UUID length (the closer
to 1, higher the uniqueness and thus better the quality).

To find out more about the math behind these functions please refer to the
<a target="_blank" href="https://shortunique.id/classes/default.html#collisionprobability">API Reference</a>.

## Acknowledgement and platform support

This repo and npm package started as a straight up manual transpilation to ES6 of the [short-uid](https://github.com/serendipious/nodejs-short-uid) npm package by [Ankit Kuwadekar](https://github.com/serendipious/).

![image depicting over 12000 weekly npm downloads](https://raw.githubusercontent.com/jeanlescure/short-unique-id/main/assets/weekly-downloads.png)
![image depicting over 100000 weekly cdn hits](https://raw.githubusercontent.com/jeanlescure/short-unique-id/main/assets/weekly-cdn-hits.png)

Since this package is now reporting 12k+ npm weekly downloads and 100k+ weekly cdn hits,
we've gone ahead and re-written the whole of it in TypeScript and made sure to package
dist modules compatible with Deno, Node.js and all major Browsers.

## Development

Clone this repo:

```sh
# SSH
git clone git@github.com:jeanlescure/short-unique-id.git

# HTTPS
git clone https://github.com/jeanlescure/short-unique-id.git
```

Tests run using:

```
pnpm test
```

## Build

In order to publish the latest changes you must build the distribution files:

```
pnpm build
```

Then commit all changes and run the release script:

```
pnpm release
```

## Contributing

Yes, thank you! This plugin is community-driven, most of its features are from different authors.
Please update the docs and tests and add your name to the `package.json` file.

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://github.com/serendipious"><img src="https://shortunique.id/assets/contributors/serendipious.svg" /></a><table><tbody><tr><td width="150" align="center"><a href="https://github.com/jeanlescure/short-unique-id/commits?author=serendipious" title="Code">💻</a></td></tr></tbody></table></td>
    <td align="center"><a href="https://jeanlescure.cr"><img src="https://shortunique.id/assets/contributors/jeanlescure.svg" /></a><table><tbody><tr><td width="150" align="center"><a href="#maintenance-jeanlescure" title="Maintenance">🚧</a> <a href="https://github.com/jeanlescure/short-unique-id/commits?author=jeanlescure" title="Code">💻</a> <a href="https://github.com/jeanlescure/short-unique-id/commits?author=jeanlescure" title="Documentation">📖</a> <a href="https://github.com/jeanlescure/short-unique-id/commits?author=jeanlescure" title="Tests">⚠️</a></td></tr></tbody></table></td>
    <td align="center"><a href="https://dianalu.design"><img src="https://shortunique.id/assets/contributors/dilescure.svg" /></a><table><tbody><tr><td width="150" align="center"><a href="https://github.com/jeanlescure/short_uuid/commits?author=DiLescure" title="Code">💻</a></td></tr></tbody></table></td>
    <td align="center"><a href="https://github.com/EmerLM"><img src="https://shortunique.id/assets/contributors/emerlm.svg" /></a><table><tbody><tr><td width="150" align="center"><a href="https://github.com/jeanlescure/short_uuid/commits?author=EmerLM" title="Code">💻</a></td></tr></tbody></table></td>
  </tr>
  <tr>
    <td align="center"><a href="https://github.com/angelnath26"><img src="https://shortunique.id/assets/contributors/angelnath26.svg" /></a><table><tbody><tr><td width="150" align="center"><a href="https://github.com/jeanlescure/short_uuid/commits?author=angelnath26" title="Code">💻</a> <a href="https://github.com/jeanlescure/short_uuid/pulls?q=is%3Apr+reviewed-by%3Aangelnath26" title="Reviewed Pull Requests">👀</a></td></tr></tbody></table></td>
    <td align="center"><a href="https://twitter.com/jeffturcotte"><img src="https://shortunique.id/assets/contributors/jeffturcotte.svg" /></a><table><tbody><tr><td width="150" align="center"><a href="https://github.com/jeanlescure/short-unique-id/commits?author=jeffturcotte" title="Code">💻</a></td></tr></tbody></table></td>
    <td align="center"><a href="https://github.com/neversun"><img src="https://shortunique.id/assets/contributors/neversun.svg" /></a><table><tbody><tr><td width="150" align="center"><a href="https://github.com/jeanlescure/short-unique-id/commits?author=neversun" title="Code">💻</a></td></tr></tbody></table></td>
    <td align="center"><a href="https://github.com/ekelvin"><img src="https://shortunique.id/assets/contributors/ekelvin.svg" /></a><table><tbody><tr><td width="150" align="center"><a href="https://github.com/jeanlescure/short-unique-id/issues/19" title="Ideas, Planning, & Feedback">🤔</a></td></tr></tbody></table></td>
  </tr>
  <tr>
    <td align="center"><a href="https://github.com/anthony-arnold"><img src="https://shortunique.id/assets/contributors/anthony-arnold.svg" /></a><table><tbody><tr><td width="150" align="center"><a href="https://github.com/jeanlescure/short-unique-id/issues/35" title="Security">🛡️</a></td></tr></tbody></table></td>
    <td align="center"><a href="https://github.com/ColdtQ"><img src="https://shortunique.id/assets/contributors/coldtq.svg" /></a><table><tbody><tr><td width="150" align="center"><a href="https://github.com/jeanlescure/short-unique-id/pull/46" title="Code">💻</a></td></tr></tbody></table></td>
    <td align="center"><a href="https://github.com/char0n"><img src="https://shortunique.id/assets/contributors/char0n.svg" /></a><table><tbody><tr><td width="150" align="center"><a href="https://github.com/jeanlescure/short-unique-id/pull/48" title="Code">💻</a></td></tr></tbody></table></td>
    <td align="center"><a href="https://github.com/mybuddymichael"><img src="https://shortunique.id/assets/contributors/mybuddymichael.svg" /></a><table><tbody><tr><td width="150" align="center"><a href="https://github.com/jeanlescure/short-unique-id/issues/47" title="Documentation">📖</a></td></tr></tbody></table></td>
  </tr>
</table>

<!-- markdownlint-enable -->
<!-- prettier-ignore-end -->
<!-- ALL-CONTRIBUTORS-LIST:END -->

## License

Copyright (c) 2018-2021 [Short Unique ID Contributors](https://github.com/jeanlescure/short-unique-id/#contributors-).<br/>
Licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
