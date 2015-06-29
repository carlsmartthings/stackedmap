# stackedmap

When using maps as contexts, a frequent use case is a temporary "local" context for a plugin or processing step that needs to be layered or stacked on top of one or more other contexts that live longer, say, across the workflow of processing steps, or with global settings.

JSPs have this with the page / request / session / application stacked scopes. 

SpringMVC and Webflow add even more...

This basically provides set and map versions of that: you can name the collections as you push them so you can access them directory (as you can with JSP), but a basic get will descend the stack and return the first context key/value mapping it finds. 
note that size() and other normally-cheap operations become more expensive since a O(n) scan of all the keys of all the maps is necessary, and possibly worse big-O. 

Those are maintained for operability with the Map and Set interfaces, especially with regards to various groovy collections operators, such as equality. 

There is no caching of the intermediate results. keySet() and values() are computed from the underlying stacked collections each time. 

Multithreading is a big huge unknown currently. Some synchronized keywords are used, but it could get dicey. Typically the "local execution" map will not be concurrently accessed, while the stacked global scopes would be, so the global scopes should be the appropriate threadsafe collection from Google Collections, Commons collections, and what have you
