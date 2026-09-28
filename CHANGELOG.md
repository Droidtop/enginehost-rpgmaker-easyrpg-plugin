# Changelog

All notable changes to this plugin are documented in this file. The format
follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- Runs EasyRPG-playable RPG Maker 2000/2003 games, proven on hardware with
  the EasyRPG TestGame (release, testing channel, 2026-09-06).
- Signed bundle releases on unstable and testing, verified file by file
  against this repository's pinned key at install time; a
  `PLATFORMS_DISPATCH_TOKEN` push tells the plugins index about a new
  release within a minute instead of waiting for its six-hourly schedule.

### Changed

- Moved signing into Enginehost's pinned `sign-engine-bundle.yml` job
  instead of running the signing script beside the build: the previous
  android job held the signing key while it ran EasyRPG's own buildscript
  dependency downloads, Gradle and third-party actions pulled by a moving
  tag, any of which could have taken the key.
