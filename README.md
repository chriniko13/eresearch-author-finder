# Eresearch Author Finder Service #

### Description
The purpose of this service is to consume the info which is provided from the
following elsevier api: http://api.elsevier.com/documentation/AUTHORSearchAPI.wadl


The search view (result from the consuming of the service we get) 
is the following: http://api.elsevier.com/documentation/search/AuthorSearchViews.htm

See also:
*  http://api.elsevier.com/documentation/search/AUTHORSearchTips.htm
*  http://api.elsevier.com/content/search/fields/author

The search queries we use are the following:
* query=authlast(Christidis) and authfirst(Nikolaos)
* query=authlast(Christidis) or authfirst(Nikolaos)

Keep in mind that authfirst can accept also initials.
There is existing business logic in the service which support this.
* query=authlast(Christidis) and authfirst(Nikolaos T.)
* query=authlast(Christidis) and authfirst(Nikolaos Thomas)

Keep in mind that are application properties to search with OR,AND or BOTH(AND && OR).


### External Dependencies needed in order to run service

* Academic VPN in order to fetch results from Elsevier API (https://dev.elsevier.com/documentation/ScienceDirectSearchAPI.wadl#d1e166)

* ActiveMQ
    * Execute: `docker-compose up`
    * Execute: `docker-compose down`
    

### Integration Tests (run docker-compose first)

* Execute: `mvn clean verify`


### Create Docker Image
* Execute: `mvn clean install -DskipITs=true`
* Execute: `docker build -t chriniko/eresearch-author-finder:1.0 .` in order to build docker image.

* Fast: `mvn clean install -DskipITs=true && docker build -t chriniko/eresearch-author-finder:1.0 .`


### How to run service (not dockerized)
* Execute: `docker-compose up`

* Two options:
    * Execute: 
        * `mvn clean install -DskipITs=true`
        * `java -jar -Dspring.profiles.active=dev target/eresearch-author-finder-1.0-boot.jar`
                
    * Execute:
        * `mvn spring-boot:run -Dspring.profiles.active=dev`

* (Optional) When you finish: `docker-compose down`


### How to run service (dockerized)
* Uncomment the section in `docker-compose.yml` file for service: `eresearch-author-finder:`

* Execute: `mvn clean install -DskipITs=true`

* Execute: `docker-compose build`

* Execute: `docker-compose up`

* (Optional) When you finish: `docker-compose down`


### Example Request

```json
{
	"author-name":{
		"firstname":"Nikolaos",
		"initials":"",
		"surname":"Christidis"
	}
}

```



### Example Response (i use only AND search operator in service configuration for short response)

```json
{
    "operation-result": true,
    "process-finished-date": "2019-04-17T11:12:56.852Z",
    "requested-author-finder-dto": {
        "author-name": {
            "firstname": "Nikolaos",
            "initials": "",
            "surname": "Christidis"
        }
    },
    "fetched-results-size": 1,
    "fetched-results": [
        {
            "search-results": {
                "opensearch:totalResults": "2",
                "opensearch:startIndex": "0",
                "opensearch:itemsPerPage": "2",
                "opensearch:Query": {
                    "@role": "request",
                    "@searchTerms": "authlast(Christidis) and authfirst(Nikolaos )",
                    "@startPage": "0"
                },
                "link": [
                    {
                        "@_fa": "true",
                        "@href": "https://api.elsevier.com/content/search/author?start=0&count=25&query=authlast%28Christidis%29+and+authfirst%28Nikolaos+%29",
                        "@ref": "self",
                        "@type": "application/json"
                    },
                    {
                        "@_fa": "true",
                        "@href": "https://api.elsevier.com/content/search/author?start=0&count=25&query=authlast%28Christidis%29+and+authfirst%28Nikolaos+%29",
                        "@ref": "first",
                        "@type": "application/json"
                    }
                ],
                "entry": [
                    {
                        "@force-array": null,
                        "error": null,
                        "@_fa": "true",
                        "link": [
                            {
                                "@_fa": "true",
                                "@href": "https://api.elsevier.com/content/author/author_id/23007591800",
                                "@ref": "self",
                                "@type": null
                            },
                            {
                                "@_fa": "true",
                                "@href": "https://api.elsevier.com/content/search/author?query=au-id%2823007591800%29",
                                "@ref": "search",
                                "@type": null
                            },
                            {
                                "@_fa": "true",
                                "@href": "https://www.scopus.com/author/citedby.uri?partnerID=HzOxMe3b&citedAuthorId=23007591800&origin=inward",
                                "@ref": "scopus-citedby",
                                "@type": null
                            },
                            {
                                "@_fa": "true",
                                "@href": "https://www.scopus.com/authid/detail.uri?partnerID=HzOxMe3b&authorId=23007591800&origin=inward",
                                "@ref": "scopus-author",
                                "@type": null
                            }
                        ],
                        "prism:url": "https://api.elsevier.com/content/author/author_id/23007591800",
                        "dc:identifier": "AUTHOR_ID:23007591800",
                        "eid": "9-s2.0-23007591800",
                        "orcid": null,
                        "preferred-name": {
                            "surname": "Christidis",
                            "given-name": "Nikolaos",
                            "initials": "N."
                        },
                        "name-variant": [
                            {
                                "@_fa": "true",
                                "surname": "Christidis",
                                "given-name": "Nikos",
                                "initials": "N."
                            },
                            {
                                "@_fa": "true",
                                "surname": "Christidis",
                                "given-name": "N.",
                                "initials": "N."
                            }
                        ],
                        "document-count": "42",
                        "subject-area": [
                            {
                                "@abbrev": "EART",
                                "@frequency": "52",
                                "$": "Earth and Planetary Sciences (all)"
                            },
                            {
                                "@abbrev": "ENVI",
                                "@frequency": "11",
                                "$": "Environmental Science (all)"
                            },
                            {
                                "@abbrev": "SOCI",
                                "@frequency": "7",
                                "$": "Social Sciences (all)"
                            }
                        ],
                        "affiliation-current": {
                            "affiliation-url": "https://api.elsevier.com/content/affiliation/affiliation_id/60018258",
                            "affiliation-id": "60018258",
                            "affiliation-name": "Met Office",
                            "affiliation-city": "Exeter",
                            "affiliation-country": "United Kingdom"
                        }
                    },
                    {
                        "@force-array": null,
                        "error": null,
                        "@_fa": "true",
                        "link": [
                            {
                                "@_fa": "true",
                                "@href": "https://api.elsevier.com/content/author/author_id/55025830900",
                                "@ref": "self",
                                "@type": null
                            },
                            {
                                "@_fa": "true",
                                "@href": "https://api.elsevier.com/content/search/author?query=au-id%2855025830900%29",
                                "@ref": "search",
                                "@type": null
                            },
                            {
                                "@_fa": "true",
                                "@href": "https://www.scopus.com/author/citedby.uri?partnerID=HzOxMe3b&citedAuthorId=55025830900&origin=inward",
                                "@ref": "scopus-citedby",
                                "@type": null
                            },
                            {
                                "@_fa": "true",
                                "@href": "https://www.scopus.com/authid/detail.uri?partnerID=HzOxMe3b&authorId=55025830900&origin=inward",
                                "@ref": "scopus-author",
                                "@type": null
                            }
                        ],
                        "prism:url": "https://api.elsevier.com/content/author/author_id/55025830900",
                        "dc:identifier": "AUTHOR_ID:55025830900",
                        "eid": "9-s2.0-55025830900",
                        "orcid": null,
                        "preferred-name": {
                            "surname": "Christidis",
                            "given-name": "Nikolaos",
                            "initials": "N."
                        },
                        "name-variant": [
                            {
                                "@_fa": "true",
                                "surname": "Christidis",
                                "given-name": "Nikolaus",
                                "initials": "N."
                            },
                            {
                                "@_fa": "true",
                                "surname": "Christidis",
                                "given-name": "N.",
                                "initials": "N."
                            }
                        ],
                        "document-count": "25",
                        "subject-area": [
                            {
                                "@abbrev": "MEDI",
                                "@frequency": "27",
                                "$": "Medicine (all)"
                            },
                            {
                                "@abbrev": "DENT",
                                "@frequency": "8",
                                "$": "Dentistry (all)"
                            },
                            {
                                "@abbrev": "NEUR",
                                "@frequency": "4",
                                "$": "Neuroscience (all)"
                            }
                        ],
                        "affiliation-current": {
                            "affiliation-url": "https://api.elsevier.com/content/affiliation/affiliation_id/115400936",
                            "affiliation-id": "115400936",
                            "affiliation-name": "Scandinavian Center for Orofacial Neurosciences (SCON)",
                            "affiliation-city": "Huddinge",
                            "affiliation-country": "Sweden"
                        }
                    }
                ]
            }
        }
    ]
}

```