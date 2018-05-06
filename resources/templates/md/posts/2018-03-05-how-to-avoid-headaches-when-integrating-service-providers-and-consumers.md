{:title  "How to avoid headaches when integrating service providers and consumers?"
 :layout :post
 :toc    true
 :date   "2018-03-05"
 :tags   ["technology" "clojure"]
 :abstract "Integrating service providers into consumers is a messy business that's often overlooked. Coupling issues are not trivial. Establishing and maintaining contracts alone can be a great deal of stress: who owns the contract? The provider or the consumer? What happens when contracts need to change? How do we manage breakage? Etc.

This article shows a particular stack, discusses the attributes that make the stack useful, and an accompaining workflow. Together, these tackle most of these integration challenges."}

Integrating service providers into consumers is a messy business that's often overlooked. Coupling issues are not trivial. Establishing and maintaining contracts alone can be a great deal of stress: who owns the contract? The provider or the consumer? What happens when contracts need to change? How do we manage breakage? Etc.

This article shows a particular stack, discusses the attributes that make the stack useful, and an accompaining workflow. Together, these tackle most of these integration challenges.


## The scenario

Let's consider a very common scenario. An app team is ready to kick off a project for building this amazing new app. A separate team, the service team, is ready to kick off this project for an amazing new API that will be used by the app.

The work ahead of the service team is not trivial at all. There are lots of systems to be integrated with, data to be fetched from multiple sources, race conditions to be sorted out, and performance challenges to be tackled.

The work ahead of the app team is equally dauting. There are platform limitations to be sorted, screen sizes to be tackled, async and uptade strategies to be implemented, and a potentially very complex state machine for the UI.

Meanwhile, both app team and service team need to agree on the contract of the API. That tiny little piece of bureocracy that keeps the two sides glued together.

![](setup.png)

Defining, agreeing, and evolving this contract is considerable work. Very often the app team and the service team are not even in the same country. More often than not they are even from different companies. Still, they need to come to terms with that one touch point or everything falls apart.


## Where is the value, really?

Before diving any further, there's something to consider: where is the value in the relationship between the app and the service layer?

There is a tendency to see value as delivered from the service tier to the consumer. In our hypothetical case the service team would deliver value to the app team.

This is a valid point of view because there's an intrinsic value in integrating and transforming data to be consumed by other components.

However, there are some interesting ideas in the [Consumer-Driven Contract pattern](https://www.martinfowler.com/articles/consumerDrivenContracts.html) that would suggest otherwise. The proposed pardigm is that the consumer defines the value of the contract - and not the other way around.

![](value.png)

In practice, the value of a service is in proportion to it been consumed. Facebook is valuable because people use it. MySpace lost its value because people didn't use it. A service provider sitting idly somewhere - not being used by anyone - does not have any value at all.


## What is a contract?

Any interface between systems is a contract. Some people avoid the word _contract_ in this kind of scenario. They prefer using _API_ instead - which is understandable. A _contract_ is such serious business! This is the very reason why I personally like using it. An established interface is serious business!

Despite being serious business, we often don't dedicate the energy to consider this touch point seriously. One way to prove it is when we stumble with a REST endpoint like this:

```bash
$ curl -v http://domain.com/api/v1/employees
```

Consider this endpoint for a second and answer to yourself: what do you expect to get as a response? A list of employees? Probably. We are not sure. In which format? JSON? Maybe. Why not TOML, XML, or anything else? Let's say JSON. But then, how are the employees organized? How the data is structured?

Here's where documentation comes in. Hopefuly there will be a Swagger, API Blueprint, RAML, or a similar spec somewhere. It will describe the format and shape of the data and how to use it.

More often than we would like to confess, there's no documentation at all or the documentation is out of sync with the implementation. This is a sad but very common predicment.

If the documentation is to be a contract but it's not up-to-date or is non-existant, what are we supposed to code against then?

## Welcome GraphQL

GraphQL has many great attributes but the one I want to highlight here is how the schema acts as a flexible and evolving contract.

GraphQL's approach to API design is that all requests and response cycles hit the same endpoint. The payload of the requests is a query, mutation or subscription that defines respectively what data the client needs, wishes to change or wants to listen to.

The API in a GraphQL universe follows a schema which is fully queriable by clients. In fact, several clients leverage this fact one way or another to improve developer experience (i.e. to enable self-discovery tools such as the wonderful GraphiQL - notice the "i" there).

![](graphql-primer.png)

## Modelling your GraphQL with Umlaut

Modelling contracts is such a serioys business that we have written our own modelling tool called [Umlaut](https://github.com/workco/umlaut) and released it as open source.

With Umlaut, you can describe say, a simple employee model, like this:

```text
type Employee {
  id: ID
  firstName: String
  lastName: String
  department: Deparment
  region: Region
}

type Deparment {
  id: ID
  name: String
}

enum Region {
  EUROPE
  NORTH_AMERICA
  LATAM
  ASIA
}
```

All source code for this article is [here](https://github.com/luchiniatwork/employees).

With a simple command line (`$ lein umlaut dot resources/ resources/`), Umlaut can generate a diagram of the above that will look like this:

![](employees-simple.png)

GraphQL has the concept of a query root which is the node where all the queries start. In Umlaut we define an entry node the same way any type would be defined:

```text
type QueryRoot {
  employees: Employee[0..n]
}
```

With another simple command line (`$ lein umlaut graphql resources/ schema.graphql`), Umlaut will generate your GraphQL schema in the `schema.graphql` file:

```text
type Deparment {
  id: ID!
  name: String!
}

type Employee {
  id: ID!
  firstName: String!
  lastName: String!
  department: Deparment!
  region: Region!
}

type QueryRoot {
  employees: [Employee!]!
}

enum Region {
  EUROPE
  NORTH_AMERICA
  LATAM
  ASIA
}
```

Or, if you prefer a more visual representation, simply regenerate the diagrams with Umlaut and you'll notice the extra `QueryRoot`:

![](employees-query.png)

## Specing the data



TBD: clojure.spec

## How do we know the spec is followed?

TBD: some discussion on mismatches - i.e. nullable/queriable, etc

## A proxing strategy

TBD: the proxy in-between

## Reporting inconsistencies up

TBD: to the testers/users
