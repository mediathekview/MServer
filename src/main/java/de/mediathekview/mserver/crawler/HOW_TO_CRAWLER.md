# How to create a crawler

This document describes how to create a crawler.

1. Check if the Sender of this new crawler is already in `de.mediathekview.mserver.daten.Sender`

  - If not create an entry for it ;)

2. Create a new package under `de.mediathekview.mserver.crawler` with the name of the Sender for which the crawler is.

  - Example for BR: `de.mediathekview.mserver.crawler.br`

3. Create a crawler class these class has to extend from `de.mediathekview.mserver.crawler.basic.AbstractCrawler`.

    1. Method `getSender` should just return the entry of `de.mediathekview.mserver.daten.Sender` for the Sender which this crawler is for.

    2. `createCrawlerTask` is the method where the "magic" happens. In this method you have to create a `RecursiveTask` which gathers a `Set` of `Film`. For more details see the topic "Best practices for gathering films".

4. Add the new crawler to the crawler map in `de.mediathekview.mserver.crawler.CrawlerManager#initializeCrawler`.

## Best practices for gathering films

The architecture bases on the [ForkJoin framework](https://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html) so the base work of creating `Film` objects out of the respective data should be done recursively in a `RecursiveTask`.

For gathering URLs or something like that you haven't to do it in a `RecursiveTask` which is often a work which can't or shouldn't be done with a recursive way.

The package `de.mediathekview.mserver.crawler.basic` contains a set of abstract tasks which do some needed basic work.

- `de.mediathekview.mserver.crawler.basic.AbstractUrlTask` is an abstract task based on `RecursiveTask` and takes a `ConcurrentLinkedQueue` of `de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO`. It splits the URLs on instances of itself based on the crawler configuration and calls the `processUrl` method for each.

- `de.mediathekview.mserver.crawler.basic.AbstractDocumentTask` is an abstract task based on `de.mediathekview.mserver.crawler.basic.AbstractUrlTask` which takes a `ConcurrentLinkedQueue` of `de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO` and loads the URLs with JSOUP as documents. In the method `processDocument` you have to use the JSOUP document to create an object of the return type.

- `de.mediathekview.mserver.crawler.basic.AbstractRestTask` is an abstract task based on `AbstractUrlTask` which takes a `ConcurrentLinkedQueue` of `de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO` and loads the URL with REST as `WebTarget`.
