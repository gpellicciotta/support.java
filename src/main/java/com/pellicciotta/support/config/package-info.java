// Copyright (c) Giovanni Pellicciotta

/**
 *  Configuration API and SPI.
 *  <p>
 *  The {@link com.pellicciotta.support.config.SettingsFactory} is the front-end API to define a
 *  and initialize a {@link com.pellicciotta.support.config.Config}, which itself consists of sub-{@link com.pellicciotta.support.config.SettingsGroup}
 *  and individual {@link com.pellicciotta.support.config.Setting}s.</p>
 *  <p>
 *  A {@link com.pellicciotta.support.config.Config} can be loaded programmatically, from environment variables,
 *  from command-line syntax and/or config files with properties, XML or JSON format.</p>
 *  <p>
 *  A distinction can be made between the API elements for consumption by most code and the SPI elements
 *  that enable configuring and creating actual settings.</p>
 *  <pre>
 *    SPI                                               API
 *    ------------------------------------------------+-----------------------------------------------------
 *    SettingsFactory
 *    ConfigElement                                     Config
 *    StandaloneSetting                                 *- Setting
 *    AttachedSetting                                   *- SettingsGroup
 *                                                         *- Setting
 *  </pre>
 */
package com.pellicciotta.support.config;

/*
   Design notes
   ============
   Settings are named values that are structured in a hierarchy.

   They basically wrap a value but also have:
     - meta-data like a description of their meaning and a default value
     - functions to (de)serialize their value (from)to JSON/XML/Map.

   Special settings called 'config' are really a grouping of other configs and elementary settings.

   Choices
   -------
   Actual implementation effort is mostly spent in:
      - needing to support serializing/deserializing to/from various formats.
      - needing to support 'a hierarachy of sources', e.g. command-line values override env. vars, which in turn override config file values.

   So the SPI should focus on making the above as easy as possible.

   Typical workflow
   ----------------
   1. Define expected/needed settings structure/hierarchy
   2. Load from sources: multiple sources, last one wins
   3. Use settings in application: further transformation needed?
   4. Optionally change settings in application UI
   5. Optionally save changed settings

 */
