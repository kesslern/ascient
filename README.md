# Ascient

A backend and frontend for storing generic data. Exposes an secure REST API usable by web applications, scripts, IoT devices, or any other HTTP capable device. An example frontend is provided to consume this data.

## Running the Project

Run with docker: `docker-compose up [--detach]`.

### TODO Development documentation

- Starting a postgres docker
- Running the backend and connecting to the postgres docker
- Running the frontend and proxying to the backend

## Users

Log in with admin/password.

## Supported Datatypes

* Booleans

## Planned Datatypes

* Lists
  * Queue manipulation methods
  * Stack manipulation methods
* Numbers
  * Counters
  * Arbitrary math statements
  * Stored math events
* Strings
  
## Features

* User-based data storage
* Store creation time and last modification time
* Authenticate with session or username/password for any endpoint

## Planned Fetaures

* Change data periodically
  * Set or toggle boolean after a time period
  * Set or toggle a boolean at a specified time
* Create new users
* Change admin password on first login
* Ability to delete users as admin
  * Include associated data
* Real-time data updates via websockets (show changes from other users)

## Other Todo

* Change all POSTs to use JSON bodies instead of query parameters

## License

ISC License

Copyright 2018 Nathan Kessler

Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
