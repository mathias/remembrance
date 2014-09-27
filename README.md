# remembrance

>"A moment." Manfred tries to remember what address to ping. It's useless, and painfully frustrating. "It would help if I could remember where I keep the rest of my mind," he complains. "It used to be at – oh, there."

>An elephantine semantic network sits down on his spectacles as soon as he asks for the site, crushing his surroundings into blocky pixilated monochrome that jerks as he looks around. "This is going to take some time," he warns his hosts as a goodly chunk of his metacortex tries to handshake with his brain over a wireless network connection that was really only designed for web browsing. The download consists of the part of his consciousness that isn't security-critical – public access actors and vague opinionated rants – but it clears down a huge memory castle, sketching in the outline of a map of miracles and wonders onto the whitewashed walls of the room.

-- Accelerando by Charlie Stross <http://www.antipope.org/charlie/blog-static/fiction/accelerando/accelerando-intro.html>


An experiment in building a system of augmented memory, targeting the cloud and (evenutually) wearable computing. Lots of academic papers and scifi novels inspired this project.

## Usage

You will need the free Datomic Pro Starter Edition first. The recommended local storage is either the `dev` or `sql` adapter running on Postgres with `memcached`. How to get a Clojure webapp up and running with Datomic (and how to run Datomic) is beyond the scope of this; since I don't really intend for anyone to actually run this but me.

Configure your storage, drop your key into the `.properties` file, etc.

Copy `resources/config.edn.example` to `resources/config.edn` and edit as appropriate with your values.

You should now be able to run the webapp with:

```bash
lein ring server-headless
```

Migrations will run automatically on the Datomic data store. (See [database.clj](https://github.com/mathias/remembrance/blob/13cb60472df2d48e3c536520c4c5573a16237849/src/clj/remembrance/database.clj) for migration system built on top of https://github.com/rkneufeld/conformity

### Development conventions:

#### Functions that use Datomic

`-q` functions contain a Datomic query (`d/q etc`) and return a set of entity IDs, as a plain `d/q` does.

The corresponding function without the `-q` in the name is the "public-facing" function that consumers will call. It usually maps across the return set of entity IDs to realize entities (for queries that return more than one entity), or gets the first entity ID and realizes it as an entity (for queries that return one entity ID.)

All functions that talk to Datomic must take a `db` (for queries) or `conn` (for transactions) param, both for testing and for composability. See [Datomic Antipatterns: Conn as a Value](http://www.rkn.io/2014/02/10/datomic-antipatterns-connnnn/) for more notes on "connection as value."

Transactions all have a separate `-txn` function that performs the actual transaction. The main, "public-facing" function handles things like ensuring that we can create or update the thing requested, and returns the updated version of the entity that was just created or updated. (Mostly because we need the created/updated entity immediately in Liberator resources to be able to respond appropriately.)

#### Model functions and Liberator resources

Liberator makes our request-response cycle much more controllable and understandable. There are a few conventions in play:

* The root route (`/`) serves the index (all items) and create (POST a new item) requests. The index will respond with `handle-ok` if the resource defaults and constraints are met.
* The id route (`/:guid`) serves the show (individual item) and update (PUT to update item) requests. Both first check the `exists?` function, and `exists?` is responsible for putting the found item onto the context for the next function. If nothing is found, we send back a not-found JSON response.

### Notes:

**Never** run `lein datomic initialize` -- it will destroy data, and we no longer use `lein datomic`'s concept of a schema file!

## Features, TODOs, wishful thinking, etc.

- [x] Need to remove the Redis-backed worker queue. Eventually, I see that work being done by https://github.com/mathias/herman
- [x] Finish rewriting to use Liberator.
- [x] Migrations system on top of [conformity](https://github.com/rkneufeld/conformity)
- [ ] Add https://github.com/cemerick/friend/ for auth.
- [ ] Lots more that I'm probably not thinking of right now.

Future features:

## License

Copyright © 2013 Matt Gauger

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

Pull Requests Accepted, but:

> "Am have been badly burned by viral end-user license agreements. Have no desire to experiment with patent shell companies held by Chechen infoterrorists. You are human, you must not worry cereal company repossess your small intestine because digest unlicensed food with it, right? Manfred, you must help me-we. Am wishing to defect."

-- Accelerando by Charlie Stross <http://www.antipope.org/charlie/blog-static/fiction/accelerando/accelerando-intro.html>
