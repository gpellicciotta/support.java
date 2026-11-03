# Project Planning
An overview of all tasks and their planning.

---

## Next Milestone

---

### Backlog
- [ ] T0004 Stop appending error text to an already started chunked body when a handler throws mid-response.
- [ ] T0003 Answer a handler that declares a content length but writes nothing, which now drops the connection.
- [ ] T0002 Pad the day in StaticFileHandler Last-Modified headers, which RFC_1123_DATE_TIME leaves unpadded.
- [x] T0001 Send the StaticFileHandler 304 response, which now closes the connection without an answer when If-Modified-Since matches.


