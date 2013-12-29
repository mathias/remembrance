# remembrance

Some smarty-pants quote goes here.

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

Migrations will run automatically on the Datomic db.

To ingest from an Instapaper CSV export:

```bash
lein exec -p script/import_from_instapaper.clj /path/to/instapaper-export.csv
```

(Note that the `-p` flag is very important to run the lein-exec in the scope of the project and get its CLASSPATH.)

### Notes:

**Never** run `lein datomic initialize` -- it will destroy data, and we no longer use `lein datomic`'s concept of a schema file!

## License

Copyright © 2013 Matt Gauger

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

Pull Requests accepted, but this is a mostly-personal project for similar reasons as:

> "Am have been badly burned by viral end-user license agreements. Have no desire to experiment with patent shell companies held by Chechen infoterrorists. You are human, you must not worry cereal company repossess your small intestine because digest unlicensed food with it, right? Manfred, you must help me-we. Am wishing to defect."

-- Accelerando by Charlie Stross <http://www.antipope.org/charlie/blog-static/fiction/accelerando/accelerando-intro.html>
