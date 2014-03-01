# remembrance

> “Remembrance of things past is not necessarily the remembrance of things as they were.”

&mdash; Marcel Proust

## Usage

You will need Datomic Pro free edition first. The recommended local storage is either the `dev` or `sql` adapter running on Postgres. How to get a Clojure webapp up and running with Datomic (and how to run Datomic) is beyond the scope of this; since I don't really intend for anyone to actually run this but me.

Install deps:

```bash
lein deps
```

Configure your storage, drop your key into the `.properties` file, etc.

You should now be able to run the webapp with:

```bash
lein ring server
```

Migrations will run automatically on the Datomic db. (See [database.clj](https://github.com/mathias/remembrance/blob/fa798b24783688b5568b2cb78c80854e3ae8cdbf/src/clj/remembrance/database.clj) for migration system built on top of https://github.com/rkneufeld/conformity

To ingest from an Instapaper CSV export:

```bash
lein run -m remembrance.scripts.import_from_instapaper /path/to/instapaper-export.csv
```

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

## Features, TODOs, wishful thinking.

- [x] Need to remove the Redis-backed worker queue. Eventually, I see that work being done by https://github.com/mathias/herman and enqueued by https://github.com/mathias/renfield
- [x] Finish rewriting to use Liberator.
- [ ] Add https://github.com/cemerick/friend/ for auth.
- [ ] Lots more that I'm probably not thinking of right now.

Future features:

* Automatically pull in each webpage read with a Chrome extension, to be indexed and searchable (with a lower "importance" score than manually added articles.)
* Move all of my RSS feed reading activities to this app, using a cron-like feed updater.
* Index emails sent/received for later searching and cross-referencing, with lower "importance"
* Features utilizing topic modeling + TDF/IF scores to find related content and suggest related content. (See: http://infolab.stanford.edu/~ullman/mmds.html )

Someday:

* A ClojureScript-based mobile-first UI that provides as good or better experience as reading content in Instapaper native iOS app. One can dream.
* Full instrumentation about time taken to read, etc.
* Suggest related content, possibly web search / web crawling built in to crawl for other related content not yet in the data store.
* Features that otherwise help this app be a research assistant.

Related but maybe not living in this app:

* Quantified self style datastore for personal analytics: Fitbit steps, Strava bike rides, Lift habit goals, RescueTime-like application usage tracking, and any other data I generate day-to-day. Since this won't be lots of text to be indexed/search, it probably doesn't make sense to store it in this app.

## License

Copyright © 2013 Matt Gauger

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

Pull Requests accepted, but this is a mostly-personal project for similar reasons as:

> "Am have been badly burned by viral end-user license agreements. Have no desire to experiment with patent shell companies held by Chechen infoterrorists. You are human, you must not worry cereal company repossess your small intestine because digest unlicensed food with it, right? Manfred, you must help me-we. Am wishing to defect."

-- Accelerando by Charlie Stross <http://www.antipope.org/charlie/blog-static/fiction/accelerando/accelerando-intro.html>
