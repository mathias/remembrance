# remembrance

A Clojure webapp.

## Usage

```
$ lein deps
$ lein ring server
```

To ingest from an Instapaper CSV export:
```
$ lein exec -p script/import_from_instapaper.clj /path/to/instapaper-export.csv
```

(Note that the `-p` flag is very important to run the lein-exec in the scope of the project and get its CLASSPATH.)

## License

Copyright © 2013 Matt Gauger

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
