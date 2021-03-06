# ![project logo](TuubesCore_logo.png)

[![](https://img.shields.io/badge/next%20version-0.6-green.svg)](https://github.com/mcphoton/Photon-Server/milestone/2)
[![](https://img.shields.io/badge/discord-join%20chat!-7289DA.svg)](https://discord.gg/vWYembz)

This repository contains the essential bricks of the [Tuubes project](http://tuubes.org), which aims to create an open-source scalable server for voxel games.

TuubesCore is independent from any game. Game-dependent content like block types and creatures are/will be implemented in other repositories.

## How to contribute
Contributors are always welcome. But please note that the [foundations](https://github.com/tuubes/TuubesCore/projects/4) aren't complete yet, therefore a lot of things are currently changing.

To contribute to the code, fork the repository, modify what you want, and send a [pull request](https://help.github.com/articles/about-pull-requests/). New ideas and issues can be reported as [github issues](https://github.com/mcphoton/Photon-Server/issues).

For more information please read [the contributing guidelines](CONTRIBUTING.md).

## Project structure
### Branches
- **master**: stable releases only
- **develop**: base branch for unstable development
- **scala-rewrite**: contains the ongoing work to rewrite the project in [the Scala programming language](http://docs.scala-lang.org) and to follow the [Actor Model](https://en.wikipedia.org/wiki/Actor_model) for easier concurrency.

### Modules
- **core**: most of the source code
- **metaprog**: scala macros used by the core

## Docker

We supply a basic docker image, simply clone and cd to this repo and perform in your terminal :
```bash
$> docker build -t tuubes-minecraft .
$> docker run -P tuubes-minecraft
```
-P publish all the exposed ports (default 25565), you can supply your own port with ``-p 25564:25565``.

The default jvm properties are ``-Xmx1024M -Xms1024M``, you can also change them with the -e tag :

```bash
$> docker run -P -e JVM_OPTS='-Xmx1024M -Xms1024M' tuubes-minecraft
```

## Current state
* Next milestone: [Awesome Architecture](https://github.com/tuubes/TuubesCore/milestone/3)
* Ongoing github project: [Foundations](https://github.com/tuubes/TuubesCore/projects/4)
* Screenshot of the last release (0.5-alpha):

![ingame screenshot](ingame-screenshot.png)